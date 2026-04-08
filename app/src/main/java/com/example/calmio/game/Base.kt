package com.example.calmio.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.calmio.R
import kotlin.math.sin

data class ObjetoFlotante(
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var radio: Float = 30f,
    var color: Int = Color.RED,
    var atrapado: Boolean = false,
    var contabilizado: Boolean = false
)

abstract class JuegoAguaBase(context: Context) : SurfaceView(context), SurfaceHolder.Callback, Runnable, SensorEventListener {

    // Callback para volver al menú — así no dependemos de ningún MainActivity concreto
    var onVolverAlMenu: (() -> Unit)? = null

    protected val objetosFlotantes = mutableListOf<ObjetoFlotante>()
    protected var puntuacion = 0

    protected val paintAgua = Paint().apply { isDither = true }
    protected val paintBase = Paint().apply { color = Color.parseColor("#4DB6AC") }
    protected val paintBoton = Paint()
    protected val paintTextoBoton = Paint().apply { textSize = 100f }
    private val paintFondoUI = Paint().apply { color = Color.parseColor("#B3B3E5FC") }
    protected val paintMarcador = Paint().apply {
        color = Color.BLACK
        textSize = 70f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }

    protected var colorBotonIzquierdoNormal: Int = Color.parseColor("#FF9800")
    protected var colorBotonDerechoNormal: Int = Color.parseColor("#FF9800")

    private val rectFondoMarcador = RectF()
    private val arrayHsvTemporal = FloatArray(3)
    private var textoPuntuacionCache = "0"
    private var puntuacionAnterior = -1
    private var coloresBotonesCalculados = false
    private var colorIzqOscuro = 0
    private var colorDerOscuro = 0
    private val matrixDegradado = Matrix()
    private val pathCuerpoAgua = Path()

    private val gravedad = 0.8f
    private val flotabilidad = -0.5f
    private val friccionAgua = 0.92f
    private val velocidadMaxima = 14f
    private val nivelAguaCentroBase = 80f

    private var tiempoOlas = 0f
    private var energiaAgua = 0f

    private var botonIzqPulsado = false
    private var botonDerPulsado = false

    @Volatile private var jugando = false
    private var hiloJuego: Thread? = null

    private var reproductorMusica: MediaPlayer? = null
    private var motorEfectos: SoundPool? = null
    private var idSonidoBurbuja: Int = 0
    protected var idMusicaFondo: Int = 0

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val acelerometro: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var inclinacionX = 0f
    private var inclinacionY = 0f

    init {
        holder.addCallback(this)

        val atributosAudio = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        motorEfectos = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(atributosAudio)
            .build()

        try {
            idSonidoBurbuja = motorEfectos?.load(context, R.raw.sonido_boton, 1) ?: 0
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    abstract fun inicializarNivel(ancho: Int, alto: Int)
    abstract fun dibujarJuego(canvas: Canvas)
    abstract fun comprobarLogicaEspecifica()
    abstract fun generarNuevoObjeto()

    override fun surfaceCreated(holder: SurfaceHolder) {
        paintAgua.shader = LinearGradient(
            0f, 0f, 0f, height * 0.8f,
            Color.parseColor("#80B3E5FC"),
            Color.parseColor("#E64FC3F7"),
            Shader.TileMode.CLAMP
        )

        inicializarNivel(width, height)
        jugando = true

        if (idMusicaFondo != 0) {
            try {
                reproductorMusica = MediaPlayer.create(context, idMusicaFondo)
                reproductorMusica?.isLooping = true
                reproductorMusica?.setVolume(0.9f, 0.9f)
                reproductorMusica?.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        acelerometro?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }

        hiloJuego = Thread(this)
        hiloJuego?.start()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        jugando = false
        hiloJuego?.join()
        reproductorMusica?.stop()
        reproductorMusica?.release()
        reproductorMusica = null
        sensorManager.unregisterListener(this)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        motorEfectos?.release()
        motorEfectos = null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            inclinacionX = -event.values[0]
            inclinacionY = event.values[1]
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun run() {
        while (jugando) {
            if (!holder.surface.isValid) continue
            actualizarFisicas()
            val canvas = holder.lockCanvas()
            if (canvas != null) {
                canvas.drawColor(Color.parseColor("#FFFFFF"))
                dibujarAguaDinamica(canvas)
                dibujarJuego(canvas)
                dibujarBotonesYBase(canvas)
                holder.unlockCanvasAndPost(canvas)
            }
        }
    }

    private fun dibujarAguaDinamica(canvas: Canvas) {
        tiempoOlas += 0.05f + (energiaAgua * 0.005f)
        energiaAgua *= 0.96f

        val diferenciaAlturaTilt = inclinacionX * -12f
        val limiteSuelo = height * 0.8f

        val anguloRad = kotlin.math.atan2((diferenciaAlturaTilt * 2f).toDouble(), width.toDouble())
        val anguloGrados = Math.toDegrees(anguloRad).toFloat()

        matrixDegradado.reset()
        matrixDegradado.setRotate(anguloGrados, width / 2f, nivelAguaCentroBase)
        paintAgua.shader.setLocalMatrix(matrixDegradado)

        pathCuerpoAgua.reset()
        var x = 0f
        val pasoX = width / 10f
        val yIzquierda = nivelAguaCentroBase - diferenciaAlturaTilt
        pathCuerpoAgua.moveTo(0f, yIzquierda)

        while (x <= width + pasoX) {
            val ratio = x / width
            val tiltLocal = -diferenciaAlturaTilt + (ratio * diferenciaAlturaTilt * 2f)
            val anguloBase = (x * 0.015f) + tiempoOlas
            val amplitud = 5f + (energiaAgua * 0.4f)
            val yOnda = sin(anguloBase) * amplitud
            val y = nivelAguaCentroBase + tiltLocal + yOnda
            pathCuerpoAgua.lineTo(x, y)
            x += pasoX
        }

        pathCuerpoAgua.lineTo(width.toFloat(), limiteSuelo)
        pathCuerpoAgua.lineTo(0f, limiteSuelo)
        pathCuerpoAgua.close()
        canvas.drawPath(pathCuerpoAgua, paintAgua)
    }

    private fun actualizarFisicas() {
        if (botonIzqPulsado) aplicarChorro(width * 0.25f, height * 0.8f)
        if (botonDerPulsado) aplicarChorro(width * 0.75f, height * 0.8f)

        val limiteSuelo = height * 0.8f
        val limiteParedDer = width.toFloat()
        val diferenciaAlturaTilt = inclinacionX * -12f

        for (i in 0 until objetosFlotantes.size) {
            val p = objetosFlotantes[i]
            val ratioX = p.x / width
            val tiltLocal = -diferenciaAlturaTilt + (ratioX * diferenciaAlturaTilt * 2f)
            val nivelAguaLocal = nivelAguaCentroBase + tiltLocal

            if (p.y > nivelAguaLocal) {
                p.vy += (gravedad + flotabilidad)
                if (!p.atrapado) {
                    p.vx += inclinacionX * 0.3f
                    p.vy += inclinacionY * 0.3f
                }
                p.vx *= friccionAgua
                p.vy *= friccionAgua
            } else {
                p.vy += gravedad
                if (!p.atrapado) p.vx += inclinacionX * 0.15f
                p.vx *= 0.99f
                p.vy *= 0.99f
                if (p.y + p.vy > nivelAguaLocal && p.vy > 5f) {
                    energiaAgua += p.vy * 0.5f
                    p.vy *= 0.4f
                }
            }

            if (p.vx > velocidadMaxima) p.vx = velocidadMaxima else if (p.vx < -velocidadMaxima) p.vx = -velocidadMaxima
            if (p.vy > velocidadMaxima) p.vy = velocidadMaxima else if (p.vy < -velocidadMaxima) p.vy = -velocidadMaxima

            p.x += p.vx
            p.y += p.vy

            if (p.x - p.radio < 0) { p.x = p.radio; p.vx *= -0.4f }
            if (p.x + p.radio > limiteParedDer) { p.x = limiteParedDer - p.radio; p.vx *= -0.4f }
            if (p.y - p.radio < 0) { p.y = p.radio; p.vy *= -0.5f }
            if (p.y + p.radio > limiteSuelo && !p.atrapado) {
                p.y = limiteSuelo - p.radio
                p.vy *= -0.4f
                if (Math.abs(p.vx) < 0.5f && Math.abs(p.vy) < 0.5f) {
                    p.vx += (Math.random().toFloat() - 0.5f) * 3f
                    p.vy -= Math.random().toFloat() * 2f
                }
            }
        }

        comprobarLogicaEspecifica()

        for (i in 0 until objetosFlotantes.size) {
            val p = objetosFlotantes[i]
            if (!p.atrapado && p.vy < 0f && p.vy > -2.0f) p.vy = 0f
        }

        var nuevosAGenerar = 0
        for (i in 0 until objetosFlotantes.size) {
            val p = objetosFlotantes[i]
            if (p.atrapado && !p.contabilizado) {
                p.contabilizado = true
                puntuacion++
                nuevosAGenerar++
            }
        }
        for (i in 0 until nuevosAGenerar) generarNuevoObjeto()
    }

    private fun aplicarChorro(xChorro: Float, yChorro: Float) {
        val radioChorro = width * 0.4f
        energiaAgua += 3f
        if (energiaAgua > 40f) energiaAgua = 40f

        for (i in 0 until objetosFlotantes.size) {
            val p = objetosFlotantes[i]
            if (p.atrapado) continue
            val dx = Math.abs(p.x - xChorro)
            if (dx < radioChorro) {
                val atenuacion = 1f - (dx / radioChorro)
                val fuerzaVertical = (5f + Math.random().toFloat() * 8f) * atenuacion
                p.vy -= fuerzaVertical
                p.vx += (Math.random().toFloat() - 0.5f) * 12f * atenuacion
            }
        }
    }

    private fun dibujarBotonesYBase(canvas: Canvas) {
        canvas.drawRect(0f, height * 0.8f, width.toFloat(), height.toFloat(), paintBase)

        if (!coloresBotonesCalculados) {
            colorIzqOscuro = oscurecerColor(colorBotonIzquierdoNormal)
            colorDerOscuro = oscurecerColor(colorBotonDerechoNormal)
            coloresBotonesCalculados = true
        }

        paintBoton.color = if (botonIzqPulsado) colorIzqOscuro else colorBotonIzquierdoNormal
        canvas.drawCircle(width * 0.25f, height * 0.9f, width * 0.1f, paintBoton)

        paintBoton.color = if (botonDerPulsado) colorDerOscuro else colorBotonDerechoNormal
        canvas.drawCircle(width * 0.75f, height * 0.9f, width * 0.1f, paintBoton)

        canvas.drawText("🔙", 45f, 180f, paintTextoBoton)

        if (puntuacion != puntuacionAnterior) {
            textoPuntuacionCache = "$puntuacion"
            puntuacionAnterior = puntuacion
        }

        val anchoTexto = paintMarcador.measureText(textoPuntuacionCache)
        rectFondoMarcador.set(
            width / 2f - anchoTexto / 2f - 40f, 30f,
            width / 2f + anchoTexto / 2f + 40f, 150f
        )
        canvas.drawRoundRect(rectFondoMarcador, 25f, 25f, paintFondoUI)
        canvas.drawText(textoPuntuacionCache, width / 2f, 110f, paintMarcador)
    }

    protected fun oscurecerColor(colorBase: Int): Int {
        Color.colorToHSV(colorBase, arrayHsvTemporal)
        arrayHsvTemporal[2] *= 0.6f
        return Color.HSVToColor(arrayHsvTemporal)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var pulsandoIzq = false
        var pulsandoDer = false

        val accion = event.actionMasked
        if (accion == MotionEvent.ACTION_DOWN || accion == MotionEvent.ACTION_POINTER_DOWN) {
            val indice = event.actionIndex
            val toqueY = event.getY(indice)
            if (toqueY > height * 0.8f) {
                motorEfectos?.play(idSonidoBurbuja, 0.8f, 0.8f, 0, 0, 1f)
            }
        }

        for (i in 0 until event.pointerCount) {
            val x = event.getX(i)
            val y = event.getY(i)

            // Botón volver — ahora usa el callback en lugar de depender de MainActivity
            if (event.actionMasked == MotionEvent.ACTION_DOWN && y < 250f && x < 250f) {
                onVolverAlMenu?.invoke()
                return true
            }

            if (y > height * 0.8f) {
                if (x < width / 2f) pulsandoIzq = true
                else pulsandoDer = true
            }
        }

        if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) {
            botonIzqPulsado = false
            botonDerPulsado = false
        } else {
            botonIzqPulsado = pulsandoIzq
            botonDerPulsado = pulsandoDer
        }

        return true
    }
}