package com.nonosky.inmyelement

import com.nonosky.carsoc.diag.TablaSrs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** La tabla REAL de la bolsa de aire del Element, la que viaja en el APK. */
class TablaSrsElementTest {

    private val tabla = TablaSrs.parsear(
        java.io.File("src/main/res/raw/srs.txt").readLines().asSequence(),
    )

    @Test
    fun traeElProcedimientoYLasAdvertencias() {
        assertTrue(tabla.pasos.size >= 5)
        assertTrue(tabla.advertencias.isNotEmpty())
    }

    @Test
    fun losCodigosSeEncuentranComoLosCuentaElDueno() {
        assertEquals("Hebilla del cinturón del conductor", tabla.buscar("9-3").single().componente)
        assertEquals("Hebilla del cinturón del conductor", tabla.buscar("93").single().componente)
        // El 7-2 vive en la fila compartida de la unidad SRS.
        assertTrue(tabla.buscar("7-2").single().componente.startsWith("Unidad SRS"))
        // "11" puede ser el 1-1 o un 11-x: salen los dos, no se adivina.
        val once = tabla.buscar("11").map { it.componente }.toSet()
        assertTrue(once.any { it.startsWith("Bolsa del volante") })
        assertTrue(once.any { it.startsWith("Bolsa lateral del conductor") })
    }

    @Test
    fun todasLasFilasExplicanYDicenQueRevisar() {
        assertTrue(tabla.codigos.all { it.explicacion.isNotBlank() && it.queRevisar.isNotBlank() })
    }
}
