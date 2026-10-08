package com.nonosky.inmyelement

import com.nonosky.carsoc.PerfilVehiculo

/**
 * Quien es este carro: lo que la libreria `:comun` necesita saber del
 * ELEMENT y que esta app le entrega al arrancar (`Carro.instalar`).
 *
 * Cada app trae el suyo. Ninguna clase de `:comun` sabe en que carro corre:
 * pregunta a `Carro.perfil`.
 *
 * ⚠️ Las MAC son VALORES POR OMISION, no verdades. Son las que se midieron
 * en el carro, pero el menu de emparejamiento las sobrescribe en
 * preferencias: si el dueño cambia una bateria, no hay que recompilar.
 */
object PerfilElement : PerfilVehiculo {

    override val clave = "element"
    override val nombre = "In my element"
    override val vehiculo = "Honda Element 2003-2006"
    override val motor = "K24A4"

    /** ISO 9141-2. Confirmado por la base de compatibilidad de Klavkarr. */
    override val protocoloEsperado = "ISO 9141-2"

    /** Esta convertido en casa rodante: manda el litio, no el motor. */
    override val esCasaRodante = true

    /** Dos bancos de litio, cada uno con su BMS JBD. */
    override val tieneBancoVivienda = true

    /**
     * Lleva sonda LAF de banda ANCHA de fabrica (pieza 36531-PZD-A01), asi
     * que el PID 0134 podria dar una relacion de mezcla real y el reloj de
     * AFR tiene sentido.
     *
     * ⚠️ SIN CONFIRMAR en este carro: hace falta leer el bitmask con el
     * contacto puesto. Mientras no se confirme, el reloj va vacio — que es
     * distinto de no tenerlo.
     */
    override val tieneAfrReal = true

    /** Refrigeradora Alpicool por BLE. */
    override val tieneNevera = true

    /** Receptor TPMS por USB (CH340). */
    override val tieneTpms = true

    /**
     * El VTEC de este motor engancha a ~2.200 rpm con carga y suelta a
     * ~2.100: entra y sale en cada cuesta. Por eso el aviso es una LAMPARA
     * de estado y no un fogonazo a pantalla completa.
     */
    override val vtecEsAcontecimiento = false



    /** Que tema de dibujo usa. Ver el paquete `ui/tema`. */
    override val tema = "topografico"

    /** Ver [PerfilVehiculo.tokenDescubrimiento]. Distinto del otro carro, siempre. */
    override val tokenDescubrimiento = "INMYELEMENT"

    override val tablaDtc = R.raw.dtc

    /** La bolsa de aire: lo que cuenta la luz SRS, traducido. El OBD-II generico no la lee. */
    override val tablaSrs: Int? = R.raw.srs
}
