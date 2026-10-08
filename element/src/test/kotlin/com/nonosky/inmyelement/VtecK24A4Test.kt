package com.nonosky.inmyelement

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * El VTEC del K24A4, con SUS numeros.
 *
 * Vive en la app porque afirma cifras de ESTE motor: el codigo del VTEC es
 * de la libreria y lo que cambia de un motor a otro son las constantes.
 *
 * ⚠️ Estos umbrales estan SIN CALIBRAR contra el carro. Salen de datalogs de
 * un K24A4 de Accord —mismo motor, otro vehiculo— leyendo el solenoide con
 * scan tool: enganche a 2.200-2.345 rpm con ~91 % de carga, desenganche a
 * 2.108 con 71 %. Cuando se pueda registrar rpm y carga en el Element, estos
 * numeros se ajustan y estas pruebas se mueven con ellos.
 */
class VtecK24A4Test {

    @Test
    fun `engancha abajo, pero con el pedal pisado`() {
        assertEquals(2_300, MotorK24A4.rpmVtec)
        assertEquals(90, MotorK24A4.vtecCargaEnganche)
        assertTrue(MotorK24A4.vtecActive(rpm = 2_300, loadPct = 90))
        assertFalse("sin las vueltas no entra", MotorK24A4.vtecActive(rpm = 2_299, loadPct = 100))
    }

    @Test
    fun `con carga media NO engancha aunque haya vueltas`() {
        // Esto es lo que se corrigio: con la guarda de 70 tambien para
        // entrar, el tablero lo cantaba antes de que el motor cambiara.
        assertFalse(MotorK24A4.vtecActive(rpm = 3_000, loadPct = 80))
        assertFalse(MotorK24A4.vtecActive(rpm = 5_000, loadPct = 15))
        assertTrue(MotorK24A4.vtecActive(rpm = 5_000, loadPct = 92))
    }

    @Test
    fun `ya enganchado, aguanta con menos vueltas y menos carga`() {
        // Entre 2.100 y 2.300, y entre 70 % y 90 %, el resultado DEPENDE de
        // si ya venia enganchado. Sin esta histeresis la lampara parpadearia
        // sin parar en ciudad, que en este motor es la situacion normal.
        assertFalse(
            "a 2200 y 75 % sin venir de enganchado, no engancha",
            MotorK24A4.vtecActive(rpm = 2_200, loadPct = 75, enganchadoAntes = false),
        )
        assertTrue(
            "a 2200 y 75 % viniendo de enganchado, sigue",
            MotorK24A4.vtecActive(rpm = 2_200, loadPct = 75, enganchadoAntes = true),
        )
        assertFalse(
            "a 2099 se suelta aunque viniera enganchado",
            MotorK24A4.vtecActive(rpm = 2_099, loadPct = 90, enganchadoAntes = true),
        )
        assertFalse(
            "con 69 % se suelta aunque viniera enganchado",
            MotorK24A4.vtecActive(rpm = 3_000, loadPct = 69, enganchadoAntes = true),
        )
    }

    @Test
    fun `el perfil dice que este carro SI tiene AFR real`() {
        assertTrue(PerfilElement.tieneAfrReal)
        assertFalse(PerfilElement.vtecEsAcontecimiento)
        assertTrue("es casa rodante: manda el litio", PerfilElement.esCasaRodante)
        assertTrue("lleva dos bancos", PerfilElement.tieneBancoVivienda)
    }

    @Test
    fun `los umbrales del K24A4 mantienen su orden`() {
        // Si alguien ajusta una cifra, esto evita dejar la caratula en un
        // estado imposible (zona roja antes del ambar, etc).
        assertTrue(MotorK24A4.rpmVtec < MotorK24A4.rpmShiftAmber)
        assertTrue(MotorK24A4.rpmShiftAmber < MotorK24A4.rpmRedline)
        assertTrue(MotorK24A4.rpmRedline <= MotorK24A4.rpmFuelCut)
        assertTrue(MotorK24A4.rpmFuelCut <= MotorK24A4.rpmMax)
        // La suelta va por DEBAJO del enganche, en vueltas y en carga, o la
        // histeresis no seria histeresis sino un parpadeo garantizado.
        assertTrue(MotorK24A4.rpmVtecSuelta < MotorK24A4.rpmVtec)
        assertTrue(MotorK24A4.vtecMinLoadPct < MotorK24A4.vtecCargaEnganche)
    }
}
