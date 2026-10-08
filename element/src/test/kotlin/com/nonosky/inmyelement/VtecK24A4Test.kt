package com.nonosky.inmyelement

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * El VTEC del K24A4, con SUS numeros.
 *
 * Vive en el sabor `element` porque afirma cifras de ESTE motor: el codigo
 * del VTEC es compartido y lo que cambia de un motor a otro son las
 * constantes.
 *
 * ⚠️ Estos umbrales estan SIN CALIBRAR contra el carro. Salen de datalogs de
 * un K24A4 de Accord —mismo motor, otro vehiculo— leyendo el solenoide con
 * scan tool: enganche a 2.200-2.345 rpm con ~91 % de carga, desenganche a
 * 2.108 con 71 %. Cuando se pueda registrar rpm y carga en el Element, estos
 * numeros se ajustan y estas pruebas se mueven con ellos.
 */
class VtecK24A4Test {

    @Test
    fun `este motor engancha MUY abajo`() {
        // La cifra que define al carro: 2.200. Aqui el
        // VTEC entra y sale en cada cuesta, y por eso el aviso es una
        // lampara y no un fogonazo a pantalla completa.
        assertEquals(2_200, MotorK24A4.rpmVtec)
        assertFalse(MotorK24A4.vtecActive(rpm = 2_199, loadPct = 100))
        assertTrue(MotorK24A4.vtecActive(rpm = 2_200, loadPct = 70))
    }

    @Test
    fun `necesita revoluciones Y carga`() {
        assertFalse(MotorK24A4.vtecActive(rpm = 5_000, loadPct = 15))
        assertTrue(MotorK24A4.vtecActive(rpm = 5_000, loadPct = 80))
    }

    @Test
    fun `la carga corta por debajo del setenta por ciento`() {
        // 69 % no basta ni a 6.000 rpm. La guarda existe para no cantar
        // VTEC en retencion, que es cuando las vueltas suben sin pedal.
        assertFalse(MotorK24A4.vtecActive(rpm = 6_000, loadPct = 69))
        assertTrue(MotorK24A4.vtecActive(rpm = 6_000, loadPct = 70))
    }

    @Test
    fun `una vez enganchado aguanta hasta el umbral de suelta`() {
        // Entre 2.100 y 2.200 el resultado DEPENDE de si ya venia
        // enganchado. Sin esta histeresis la lampara parpadearia sin parar
        // en ciudad, que en este motor es la situacion normal.
        assertFalse(
            "a 2150 sin venir de enganchado, no engancha",
            MotorK24A4.vtecActive(rpm = 2_150, loadPct = 90, enganchadoAntes = false),
        )
        assertTrue(
            "a 2150 viniendo de enganchado, sigue",
            MotorK24A4.vtecActive(rpm = 2_150, loadPct = 90, enganchadoAntes = true),
        )
        assertFalse(
            "a 2099 se suelta aunque viniera enganchado",
            MotorK24A4.vtecActive(rpm = 2_099, loadPct = 90, enganchadoAntes = true),
        )
    }

    @Test
    fun `el perfil dice que este carro SI puede tener AFR real`() {
        // Lleva sonda LAF de banda ancha de fabrica, asi que el reloj de
        // mezcla tiene sentido — aunque siga sin confirmarse que la ECU
        // exponga el 0134.
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
        // Y la suelta del VTEC va por DEBAJO del enganche, o la histeresis no
        // seria histeresis sino un parpadeo garantizado.
        assertTrue(MotorK24A4.rpmVtecSuelta < MotorK24A4.rpmVtec)
    }
}
