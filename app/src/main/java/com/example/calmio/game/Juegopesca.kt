/**
 * =========================================================================================
 * ARCHIVO: JuegoPesca.kt
 * Proyecto: Calmio
 * =========================================================================================
 * Adaptado del original Pesca.kt del profesor para integrarse en com.example.calmio.
 *
 * CAMBIOS RESPECTO AL ORIGINAL:
 * - Package cambiado de `com.example.myapplication` a `com.example.calmio`
 * - El resto del código es idéntico al original del profesor
 * =========================================================================================
 */
package com.example.calmio.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.example.calmio.R

/**
 * Entorno simulado de pesca lúdica y captura automática basado en áreas de influencia radiales.
 *
 * @param context El ecosistema nativo general.
 */
class JuegoPescarView(context: Context) : JuegoAguaBase(context) {

    /** Pincel dinámico reservado íntegramente a perfilar y pigmentar los mamíferos y escamas acuáticas. */
    private val paintPez = Paint()

    /** Pincel macizo oscuro orientado a la forja geométrica visual de los palos de caña y ganchos finales en herradura. */
    private val paintGancho = Paint().apply {
        color = Color.DKGRAY
        style = Paint.Style.STROKE
        strokeWidth = 12f
    }

    /** Repositorio de coordenadas precalculadas conteniendo las ubicaciones estáticas X de la maquinaria pescadora. */
    private val ganchosX = mutableListOf<Float>()

    /** Límite Y donde terminan las cuerdas y nace la trampa herradural (Gancho final de pesca). */
    private var ganchosY = 0f

    /** Estructura de Listado que provee una inmensa gama de vivos colores coralinos. */
    private val coloresPeces = listOf(
        Color.parseColor("#D50000"), Color.parseColor("#C51162"), Color.parseColor("#AA00FF"),
        Color.parseColor("#6200EA"), Color.parseColor("#304FFE"), Color.parseColor("#2962FF"),
        Color.parseColor("#00B8D4"), Color.parseColor("#00BFA5"), Color.parseColor("#00C853"),
        Color.parseColor("#64DD17"), Color.parseColor("#AEEA00"), Color.parseColor("#FFD600"),
        Color.parseColor("#FFAB00"), Color.parseColor("#FF6D00"), Color.parseColor("#DD2C00")
    )

    init {
        // Redefinimos un tono inferior cristalino (Azul turquesa océano lúdico).
        paintBase.color = Color.parseColor("#D53985")

        // Nombre del archivo a reproducir como música de fondo
        idMusicaFondo = R.raw.musica_pesca
    }

    /**
     * Motor de escalado y siembra posicional basándose en los anchos concretos dictados por la pantalla real del dispositivo.
     */
    override fun inicializarNivel(ancho: Int, alto: Int) {
        ganchosY = alto * 0.25f
        ganchosX.clear()

        // Distribuimos equitativamente los 3 anzuelos a lo largo del eje horizontal en cuotas perfectas de 25%.
        ganchosX.add(ancho * 0.25f)
        ganchosX.add(ancho * 0.5f)
        ganchosX.add(ancho * 0.75f)

        objetosFlotantes.clear()
        puntuacion = 0

        // Invocamos un cardumen activo surtido de 12 peces diversos.
        for (i in 0 until 12) {
            generarNuevoObjeto()
        }
    }

    /**
     * Fábrica de biodiversidad. Instancia aleatoriamente cada elemento con una corpulencia (radio) imprevisible y peculiar.
     */
    override fun generarNuevoObjeto() {
        val posX = (Math.random() * (width - 100) + 50).toFloat()
        val posY = (Math.random() * (height * 0.3f) + height * 0.5f).toFloat()

        objetosFlotantes.add(
            ObjetoFlotante(
                x = posX,
                y = posY,
                radio = (35..55).random().toFloat(),
                color = coloresPeces.random()
            )
        )
    }

    /**
     * Motor Gráfico de renders encadenados para elementos vivos mutables y maquinaria inamovible (Ganchos).
     */
    override fun dibujarJuego(canvas: Canvas) {
        // Línea vertical que desciende como hilo grueso pescador unida a un barrido cóncavo (Arco de 180º).
        for (gx in ganchosX) {
            canvas.drawLine(gx, 0f, gx, ganchosY, paintGancho)
            canvas.drawArc(gx - 20f, ganchosY, gx + 20f, ganchosY + 40f, 0f, 180f, false, paintGancho)
        }

        for (p in objetosFlotantes) {
            paintPez.color = p.color

            // Óvalo ensanchado que compone la zona abultada vital principal del cuerpo o espina dorsal.
            canvas.drawOval(p.x - p.radio * 1.5f, p.y - p.radio, p.x + p.radio * 1.5f, p.y + p.radio, paintPez)

            // Creación instantánea por fotograma de un diseño algorítmico poligonal para aleta caudal.
            val pathCola = Path()
            pathCola.moveTo(p.x - p.radio * 1.2f, p.y)
            pathCola.lineTo(p.x - p.radio * 2.8f, p.y - p.radio * 1.2f)
            pathCola.lineTo(p.x - p.radio * 2.8f, p.y + p.radio * 1.2f)
            pathCola.close()

            canvas.drawPath(pathCola, paintPez)
        }
    }

    /**
     * Bucle analizador de atracción invisible y engarzado físico estricto.
     */
    override fun comprobarLogicaEspecifica() {
        for (p in objetosFlotantes) {

            if (p.atrapado) {
                p.vy = 0f
                p.vx = 0f
                p.y = ganchosY + 20f
                val ganchoCercano = ganchosX.minByOrNull { Math.abs(it - p.x) } ?: p.x
                p.x = ganchoCercano
                continue
            }

            val radioCaptura = p.radio + 35f

            for (gx in ganchosX) {
                val centroAnzueloX = gx
                val centroAnzueloY = ganchosY + 20f

                val dx = p.x - centroAnzueloX
                val dy = p.y - centroAnzueloY
                val distancia = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()

                if (distancia < radioCaptura) {
                    p.x = centroAnzueloX
                    p.y = centroAnzueloY
                    p.vx = 0f
                    p.vy = 0f
                    p.atrapado = true
                    break
                }
            }
        }
    }
}