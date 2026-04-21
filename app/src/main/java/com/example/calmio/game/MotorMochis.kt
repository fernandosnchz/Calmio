package com.example.calmio.game

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import kotlin.math.abs
import kotlin.random.Random

/**
 * Representa un elemento interactivo que flota en la pantalla (globo, burbuja...).
 *
 * @property x Coordenada horizontal actual en la pantalla.
 * @property y Coordenada vertical actual en la pantalla.
 * @property velocidadY Aceleración acumulada en el eje vertical.
 * @property velocidadX Velocidad constante en el eje horizontal.
 * @property radio Tamaño del área interactiva (hitbox) del elemento.
 * @property emoji Símbolo visual que se mostrará en pantalla.
 * @property tiempoExplotado Marca de tiempo exacta de cuándo fue tocado (útil para animaciones).
 * @property explotado Estado booleano: 'true' si el usuario ya lo ha tocado, 'false' si sigue vivo.
 */
data class Mochi(
    var x: Float,
    var y: Float,
    var velocidadY: Float = 0f,
    var velocidadX: Float = 0f,
    val radio: Float = 70f,
    var emoji: String,
    var tiempoExplotado: Long = 0L,
    var explotado: Boolean = false
) {
    /**
     * Comprueba si las coordenadas del toque impactan dentro de este elemento.
     *
     * @param xDelDedo Coordenada horizontal exacta del toque del usuario.
     * @param yDelDedo Coordenada vertical exacta del toque del usuario.
     * @return `true` si se acertó en el elemento, `false` si el toque fue fuera.
     */
    fun fueTocado(xDelDedo: Float, yDelDedo: Float): Boolean {
        val distanciaX = xDelDedo - x
        val distanciaY = yDelDedo - y

        val distanciaAlCuadrado = (distanciaX * distanciaX) + (distanciaY * distanciaY)
        val radioAlCuadrado = radio * radio

        return distanciaAlCuadrado <= (radioAlCuadrado * 3f)
    }
}

/**
 * Gestor principal que controla la generación, movimiento y colisiones del juego.
 */
class MotorMochis {
    companion object {
        const val MAX_MOCHIS = 1000 // Límite máximo de elementos en memoria
    }

    // Sistema de puntuación del jugador
    var puntuacion: Int = 0

    // En este juego, la "gravedad" actúa como flotabilidad (aceleración hacia arriba)
    private val gravedad = 0.01f

    var mochis = mutableStateListOf<Mochi>()

    val emojisDisponibles = listOf("🎈", "🫧", "🌸", "🦋")

    var onExplotar: (() -> Unit)? = null

    /**
     * Procesa la interacción del usuario con la pantalla.
     * En lugar de moverlos (como en el motor anterior), aquí los "destruye" o marca como explotados.
     *
     * @param xToque Coordenada horizontal donde pulsó el jugador.
     * @param yToque Coordenada vertical donde pulsó el jugador.
     */
    fun tocar(xToque: Float, yToque: Float) {
        // Recorremos de atrás hacia adelante para detectar el que está dibujado "encima"
        for (mochi in mochis.reversed()) {
            if (mochi.fueTocado(xToque, yToque)) {
                mochi.explotado = true
                mochi.tiempoExplotado = System.currentTimeMillis()
                puntuacion++
                onExplotar?.invoke()
                break // Solo podemos explotar uno por cada toque, así que salimos del bucle
            }
        }
    }

    /**
     * Función auxiliar para generar un nuevo elemento fuera de la pantalla.
     *
     * @param anchoPantalla Ancho del área de juego para calcular posiciones X aleatorias.
     * @param altoPantalla Alto del área de juego para hacerlo aparecer desde el fondo.
     */
    fun crearNuevoEmoji(anchoPantalla: Float, altoPantalla: Float) {
        val nuevoMochi = Mochi(
            x = Random.nextFloat() * anchoPantalla,
            y = altoPantalla + 150f,
            velocidadY = 0f,
            velocidadX = (Random.nextFloat() * 2f) - 1f,
            emoji = emojisDisponibles.random()
        )
        mochis.add(nuevoMochi)

        // Medida de seguridad: borrar los más antiguos si superamos el límite
        if (mochis.size > MAX_MOCHIS) {
            mochis.removeFirstOrNull()
        }
    }

    /**
     * Motor principal de físicas y lógica de juego.
     * Actualiza el estado de todos los elementos frame a frame.
     *
     * @param anchoPantalla La anchura actual de la pantalla del dispositivo.
     * @param altoPantalla La altura actual de la pantalla del dispositivo.
     */
    fun actualizarFisicas(anchoPantalla: Float, altoPantalla: Float) {
        // Control de seguridad por si la pantalla aún no se ha medido
        if (anchoPantalla == 0f || altoPantalla == 0f) return

        for (mochi in mochis) {
            // FÍSICAS CONDICIONALES: Solo movemos los que NO han sido explotados
            if (!mochi.explotado) {
                // Aumentamos su velocidad
                mochi.velocidadY += gravedad
                // RESTAMOS la velocidad en Y. Como el punto 0 de Y está arriba,
                // restar significa que el objeto sube (flota) por la pantalla.
                mochi.y -= mochi.velocidadY
            }
        }

        // LIMPIEZA DE MEMORIA:
        // Eliminamos automáticamente cualquier mochi que haya subido tanto que
        // ya no se ve en pantalla (y <= -250f). Esto evita que el móvil se colapse
        // intentando recordar elementos que ya están fuera del juego.
        mochis.removeAll( { it.y <= -250f } )

        // GENERACIÓN PROCEDIMENTAL:
        // En cada fotograma, hay un 5% de probabilidades (0.05) de que aparezca
        // un nuevo globo/burbuja desde el fondo de la pantalla.
        if (Random.nextFloat() < 0.05) {
            crearNuevoEmoji(anchoPantalla, altoPantalla)
        }
    }

    fun limpiarPantalla() {
        mochis.clear()
    }
}