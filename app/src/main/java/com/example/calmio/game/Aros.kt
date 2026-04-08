package com.example.calmio.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.calmio.R

class JuegoArosView(context: Context) : JuegoAguaBase(context) {
    private var juegoTerminado = false

    private val paintAro = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 15f
    }

    private val paintPoste = Paint().apply { color = Color.parseColor("#FFEB3B") }

    private var posteIzqX = 0f
    private var posteCentroX = 0f
    private var posteDerX = 0f
    private var altoPoste = 0f
    private var techoPosteY = 0f

    private val coloresAros = listOf(
        Color.parseColor("#FF0000"), Color.parseColor("#FF5252"), Color.parseColor("#FF4081"),
        Color.parseColor("#E040FB"), Color.parseColor("#AA00FF"), Color.parseColor("#651FFF"),
        Color.parseColor("#3D5AFE"), Color.parseColor("#2979FF"), Color.parseColor("#00B0FF"),
        Color.parseColor("#00E5FF"), Color.parseColor("#1DE9B6"), Color.parseColor("#00E676"),
        Color.parseColor("#76FF03"), Color.parseColor("#C6FF00"), Color.parseColor("#FFEA00"),
        Color.parseColor("#FFC400"), Color.parseColor("#FF9100"), Color.parseColor("#FF3D00")
    )

    init {
        paintBase.color = Color.parseColor("#4CAF50")
        idMusicaFondo = R.raw.musica_aros
    }

    override fun inicializarNivel(ancho: Int, alto: Int) {
        posteIzqX = ancho * 0.20f
        posteCentroX = ancho * 0.5f
        posteDerX = ancho * 0.80f
        altoPoste = alto * 0.35f
        techoPosteY = (alto * 0.8f) - altoPoste

        objetosFlotantes.clear()
        puntuacion = 0

        for (i in 0 until 18) {
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
                radio = 55f,
                color = coloresAros.random()
            )
        )
    }

    override fun dibujarJuego(canvas: Canvas) {
        canvas.drawRect(posteIzqX - 10f, techoPosteY, posteIzqX + 10f, height * 0.8f, paintPoste)
        canvas.drawRect(posteCentroX - 10f, techoPosteY, posteCentroX + 10f, height * 0.8f, paintPoste)
        canvas.drawRect(posteDerX - 10f, techoPosteY, posteDerX + 10f, height * 0.8f, paintPoste)

        for (p in objetosFlotantes) {
            paintAro.color = p.color
            if (p.atrapado) {
                canvas.drawOval(p.x - p.radio, p.y - 10f, p.x + p.radio, p.y + 10f, paintAro)
            } else {
                canvas.drawCircle(p.x, p.y, p.radio, paintAro)
            }
        }
    }

    override fun comprobarLogicaEspecifica() {
        val apilados = mutableMapOf<Float, Int>()
        apilados[posteIzqX] = 0
        apilados[posteCentroX] = 0
        apilados[posteDerX] = 0

        val ordenados = objetosFlotantes.sortedByDescending { it.y }

        for (p in ordenados) {
            if (p.atrapado) {
                p.vx = 0f
                val cantApilada = apilados[p.x] ?: 0
                val sueloObjetivo = (height * 0.8f) - 10f - (cantApilada * 20f)

                if (p.y >= sueloObjetivo) {
                    p.y = sueloObjetivo
                    p.vy = 0f
                    apilados[p.x] = cantApilada + 1
                } else {
                    p.vy += 1f
                    apilados[p.x] = cantApilada + 1
                }
            } else {
                if (p.vy > 0 && p.y + p.radio > techoPosteY - 15f && p.y < techoPosteY + 55f) {
                    if (Math.abs(p.x - posteIzqX) < p.radio * 0.8f) {
                        p.x = posteIzqX; p.atrapado = true
                    } else if (Math.abs(p.x - posteCentroX) < p.radio * 0.8f) {
                        p.x = posteCentroX; p.atrapado = true
                    } else if (Math.abs(p.x - posteDerX) < p.radio * 0.8f) {
                        p.x = posteDerX; p.atrapado = true
                    }
                } else if (p.y > techoPosteY) {
                    if (Math.abs(p.x - posteIzqX) < p.radio) {
                        p.x = if (p.x < posteIzqX) posteIzqX - p.radio else posteIzqX + p.radio
                        p.vx *= -0.5f
                    } else if (Math.abs(p.x - posteCentroX) < p.radio) {
                        p.x = if (p.x < posteCentroX) posteCentroX - p.radio else posteCentroX + p.radio
                        p.vx *= -0.5f
                    } else if (Math.abs(p.x - posteDerX) < p.radio) {
                        p.x = if (p.x < posteDerX) posteDerX - p.radio else posteDerX + p.radio
                        p.vx *= -0.5f
                    }
                }
            }
        }

        // Si se han encestado 50 aros, terminamos el juego
        if (puntuacion >= 50) {
            onVolverAlMenu?.invoke()
        }
    }
}