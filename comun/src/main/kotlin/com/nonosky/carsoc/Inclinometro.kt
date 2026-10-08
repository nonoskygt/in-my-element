package com.nonosky.carsoc

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot

/**
 * EL INCLINOMETRO: cuanto esta inclinada la camioneta, para parquearla a
 * nivel y dormir derecho.
 *
 * Lee la gravedad con el sensor del radio —el de gravedad si lo tiene, que ya
 * descuenta las aceleraciones; si no, el acelerometro— y la convierte en dos
 * angulos:
 * - [adelante]: positivo con la nariz MAS ALTA que la cola.
 * - [lado]: positivo con el lado DERECHO mas bajo.
 *
 * ## El montaje
 *
 * Se supone el radio en el tablero, de pie, con la pantalla mirando hacia
 * atras: el eje X del sensor va de izquierda a derecha y el Z sale de la
 * pantalla hacia la cola. Como ningun radio va perfectamente derecho, el
 * cero se CALIBRA con la camioneta en plano ([ponerACero]). Y como algunos
 * radios montan el sensor girado, cada eje se puede invertir en Ajustes.
 *
 * ## El gasto
 *
 * Solo escucha mientras alguien mira: el tablero o los Ajustes lo encienden
 * al verse y lo apagan al irse ([encender] / [apagar], con cuenta). Con la
 * pantalla en otra cosa, el sensor no gasta nada.
 */
object Inclinometro : SensorEventListener {

    private const val PREFS = "inclinometro"
    private const val K_CERO_ADELANTE = "cero_adelante"
    private const val K_CERO_LADO = "cero_lado"
    private const val K_INV_ADELANTE = "invertir_adelante"
    private const val K_INV_LADO = "invertir_lado"

    /** Suavizado exponencial: el motor en marcha hace vibrar el radio. */
    private const val SUAVE = 0.12f

    /** Una lectura mas vieja que esto ya no se enseña. */
    const val VIGENCIA_MS = 2_500L

    @Volatile var adelante: Float? = null; private set
    @Volatile var lado: Float? = null; private set
    @Volatile var medidoMs: Long = 0L; private set

    /** null = todavia no se ha mirado; false = este radio no tiene sensor. */
    @Volatile var haySensor: Boolean? = null; private set

    private var crudoAdelante = Float.NaN
    private var crudoLado = Float.NaN
    private var ceroAdelante = 0f
    private var ceroLado = 0f
    private var invAdelante = false
    private var invLado = false

    private var usuarios = 0
    private var gestor: SensorManager? = null

    /** Lo enciende quien se pone a mirar. Con cuenta: el ultimo en irse lo apaga. */
    @Synchronized
    fun encender(context: Context) {
        val app = context.applicationContext
        cargar(app)
        usuarios++
        if (usuarios > 1) return
        val sm = app.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val sensor = sm?.getDefaultSensor(Sensor.TYPE_GRAVITY)
            ?: sm?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        haySensor = sensor != null
        if (sm == null || sensor == null) return
        gestor = sm
        crudoAdelante = Float.NaN
        crudoLado = Float.NaN
        sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    @Synchronized
    fun apagar() {
        if (usuarios == 0) return
        usuarios--
        if (usuarios > 0) return
        runCatching { gestor?.unregisterListener(this) }
        gestor = null
    }

    override fun onSensorChanged(e: SensorEvent) {
        if (e.values.size < 3) return
        val x = e.values[0]
        val y = e.values[1]
        val z = e.values[2]
        // Con la hipotenusa de los otros dos ejes, un angulo no se contamina
        // con el otro cuando la camioneta esta inclinada hacia los dos lados.
        val ad = Math.toDegrees(atan2(-z, hypot(y, x)).toDouble()).toFloat()
        val la = Math.toDegrees(atan2(-x, hypot(y, z)).toDouble()).toFloat()
        crudoAdelante = if (crudoAdelante.isNaN()) ad else crudoAdelante + SUAVE * (ad - crudoAdelante)
        crudoLado = if (crudoLado.isNaN()) la else crudoLado + SUAVE * (la - crudoLado)
        adelante = (crudoAdelante - ceroAdelante) * (if (invAdelante) -1f else 1f)
        lado = (crudoLado - ceroLado) * (if (invLado) -1f else 1f)
        medidoMs = System.currentTimeMillis()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    /** ¿Hay una lectura de ahora mismo? */
    fun vigente(ahoraMs: Long = System.currentTimeMillis()): Boolean =
        medidoMs > 0 && ahoraMs - medidoMs < VIGENCIA_MS

    /**
     * Lo que se lee AHORA pasa a ser el cero. Se hace una vez, con la
     * camioneta en un suelo plano de verdad. Devuelve false si no hay lectura.
     */
    fun ponerACero(context: Context): Boolean {
        if (crudoAdelante.isNaN() || crudoLado.isNaN()) return false
        ceroAdelante = crudoAdelante
        ceroLado = crudoLado
        prefs(context).edit()
            .putFloat(K_CERO_ADELANTE, ceroAdelante)
            .putFloat(K_CERO_LADO, ceroLado)
            .apply()
        return true
    }

    fun invertidoAdelante(context: Context): Boolean = prefs(context).getBoolean(K_INV_ADELANTE, false)
    fun invertidoLado(context: Context): Boolean = prefs(context).getBoolean(K_INV_LADO, false)

    fun invertirAdelante(context: Context, si: Boolean) {
        prefs(context).edit().putBoolean(K_INV_ADELANTE, si).apply()
        invAdelante = si
    }

    fun invertirLado(context: Context, si: Boolean) {
        prefs(context).edit().putBoolean(K_INV_LADO, si).apply()
        invLado = si
    }

    /** ¿Ya se calibro alguna vez? Sin calibrar, el cero es el del montaje. */
    fun calibrado(context: Context): Boolean = prefs(context).contains(K_CERO_ADELANTE)

    /** La lectura para el tablero, con un decimal; null si no la hay. */
    fun redondo(v: Float?): Float? = v?.let { Math.round(it * 10f) / 10f }

    /** Para enseñar en Ajustes: "1.4° nariz arriba · 0.3° derecha abajo". */
    fun resumen(): String {
        val a = adelante
        val l = lado
        if (a == null || l == null || !vigente()) return "sin lectura"
        fun g(v: Float) = "%.1f°".format(abs(v))
        val ta = if (abs(a) < 0.05f) "adelante a nivel" else g(a) + if (a > 0) " nariz arriba" else " nariz abajo"
        val tl = if (abs(l) < 0.05f) "lados a nivel" else g(l) + if (l > 0) " derecha abajo" else " izquierda abajo"
        return "$ta · $tl"
    }

    private fun cargar(context: Context) {
        val p = prefs(context)
        ceroAdelante = p.getFloat(K_CERO_ADELANTE, 0f)
        ceroLado = p.getFloat(K_CERO_LADO, 0f)
        invAdelante = p.getBoolean(K_INV_ADELANTE, false)
        invLado = p.getBoolean(K_INV_LADO, false)
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
