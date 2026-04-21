package com.example.calmio.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.example.calmio.R

class JuegoPescarView(context: Context) : JuegoAguaBase(context) {

    /** Pincel principal para el cuerpo y la cola del pez. */
    private val paintPez = Paint()

    /** Pincel blanco para el ojo del pez. */
    private val paintOjo = Paint().apply {
        color = Color.WHITE
        isAntiAlias = true
    }

    /** Pincel negro para la pupila del pez. */
    private val paintPupila = Paint().apply {
        color = Color.BLACK
        isAntiAlias = true
    }

    /** Pincel para los hilos y anzuelos. */
    private val paintGancho = Paint().apply {
        color = Color.DKGRAY
        style = Paint.Style.STROKE
        strokeWidth = 12f
    }

    /** Coordenadas X de los tres anzuelos. */
    private val ganchosX = mutableListOf<Float>()

    /** Altura Y donde terminan los hilos y empieza el gancho. */
    private var ganchosY = 0f

    /** Paleta de colores tema Calmio. */
    private val coloresPeces = listOf(
        Color.parseColor("#4DB6AC"), // Verde menta
        Color.parseColor("#80CBC4"), // Verde salvia claro
        Color.parseColor("#A5D6A7"), // Verde suave
        Color.parseColor("#FFB74D"), // Naranja cálido
        Color.parseColor("#F48FB1"), // Rosa
        Color.parseColor("#CE93D8"), // Lila
        Color.parseColor("#81D4FA"), // Azul cielo
        Color.parseColor("#FFCC80"), // Melocotón
        Color.parseColor("#EF9A9A")  // Rojo suave
    )

    init {
        // Fondo verde menta tema Calmio
        paintBase.color = Color.parseColor("#4DB6AC")

        // Música de fondo
        idMusicaFondo = R.raw.musica_pesca
    }

    override fun inicializarNivel(ancho: Int, alto: Int) {
        ganchosY = alto * 0.25f
        ganchosX.clear()
        ganchosX.add(ancho * 0.25f)
        ganchosX.add(ancho * 0.5f)
        ganchosX.add(ancho * 0.75f)

        objetosFlotantes.clear()
        puntuacion = 0

        for (i in 0 until 12) {
            generarNuevoObjeto()
        }
    }

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

    override fun dibujarJuego(canvas: Canvas) {
        // Dibujamos los hilos y anzuelos
        for (gx in ganchosX) {
            canvas.drawLine(gx, 0f, gx, ganchosY, paintGancho)
            canvas.drawArc(gx - 20f, ganchosY, gx + 20f, ganchosY + 40f, 0f, 180f, false, paintGancho)
        }

        // Dibujamos cada pez
        for (p in objetosFlotantes) {
            paintPez.color = p.color
            paintPez.isAntiAlias = true

            // Cuerpo ovalado
            canvas.drawOval(
                p.x - p.radio * 1.5f, p.y - p.radio,
                p.x + p.radio * 1.5f, p.y + p.radio,
                paintPez
            )

            // Cola triangular
            val pathCola = Path()
            pathCola.moveTo(p.x - p.radio * 1.2f, p.y)
            pathCola.lineTo(p.x - p.radio * 2.8f, p.y - p.radio * 1.2f)
            pathCola.lineTo(p.x - p.radio * 2.8f, p.y + p.radio * 1.2f)
            pathCola.close()
            canvas.drawPath(pathCola, paintPez)

            // Ojo blanco
            canvas.drawCircle(
                p.x + p.radio * 0.8f,
                p.y - p.radio * 0.3f,
                p.radio * 0.25f,
                paintOjo
            )
            // Pupila negra
            canvas.drawCircle(
                p.x + p.radio * 0.9f,
                p.y - p.radio * 0.3f,
                p.radio * 0.12f,
                paintPupila
            )
        }
    }

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
                val dx = p.x - gx
                val dy = p.y - (ganchosY + 20f)
                val distancia = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()

                if (distancia < radioCaptura) {
                    p.x = gx
                    p.y = ganchosY + 20f
                    p.vx = 0f
                    p.vy = 0f
                    p.atrapado = true
                    break
                }
            }
        }
    }
}