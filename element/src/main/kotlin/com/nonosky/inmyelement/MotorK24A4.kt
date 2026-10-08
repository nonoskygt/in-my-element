package com.nonosky.inmyelement

import com.nonosky.carsoc.Motor

/**
 * Constantes del K24A4 (Honda Element 2003-2006).
 *
 * Todo lo ajustable del comportamiento del tablero vive aqui. Si un valor
 * resulta estar mal en el carro, se corrige una linea en este archivo y nada
 * mas: ni la vista ni el scheduler traen numeros magicos propios.
 *
 * ⚠️ VARIOS NUMEROS ESTAN SIN CONFIRMAR EN EL CARRO. Ver cada apartado.
 */
object MotorK24A4 : Motor {

    /**
     * Maximo del tacometro.
     *
     * ⚠️ SIN CONFIRMAR EN EL CARRO. Honda no publica el redline del K24A4:
     * no aparece ni en la hoja oficial de especificaciones de 2004 ni en el
     * manual del propietario. Las fuentes de terceros se contradicen entre
     * 6.500 (Wikipedia, que deja vacia la celda del limitador) y 6.800 (varias
     * webs, probablemente confundiendolo con el K24A8 de 2007 en adelante).
     *
     * Se toma 7.000 como tope de esfera para que el limitador quepa dentro sea
     * cual sea. La medida real: pedir 010C a fondo en 2a con el resto de PIDs
     * apagados y buscar el pico.
     */
    override val rpmMax = 7_000

    /**
     * Enganche del VTEC.
     *
     * ⚠️ AQUI EL VTEC NO ES UN ACONTECIMIENTO: ENTRA Y SALE A CADA RATO.
     *
     * El K24A4 lleva i-VTEC de DOS balancines y solo en admision, y engancha
     * MUY abajo: logs reales del mismo motor con scan tool leyendo el
     * solenoide dan enganche a 2.200-2.345 rpm con ~91 % de carga, y
     * desenganche a 2.108 rpm con 71 %. O sea que entra y sale
     * constantemente en conduccion normal, hasta subiendo una cuesta a 50 km/h.
     *
     * Honda NO publica la cifra: su nota de prensa solo da una tabla
     * cualitativa. Estos valores salen de datalogs de un K24A4 de Accord —el
     * mismo motor, pero no este carro— y hay que calibrarlos registrando rpm y
     * carga en el Element.
     *
     * Por eso el aviso es una LAMPARA de estado y no un fondo rojo
     * parpadeante: a esta frecuencia, un fogonazo a pantalla completa tendria
     * la pantalla latiendo todo el viaje.
     *
     * Se usa 2.300 —el medio de los enganches registrados— y no 2.200: con
     * 2.200 y la carga baja de antes, el dueño lo vio entrar antes de tiempo.
     */
    override val rpmVtec = 2_300

    /**
     * Histeresis del VTEC, en rpm.
     *
     * Sin esto la lampara parpadea sin parar cuando las revoluciones rondan el
     * umbral. Los logs dan enganche a 2.200 y desenganche a 2.108: ~100 rpm.
     */
    override val rpmVtecSuelta = 2_100

    /** Inicio de la zona roja pintada en la carátula. */
    override val rpmRedline = 6_500

    /** Corte de combustible. ⚠️ Sin confirmar; ver RPM_MAX. */
    override val rpmFuelCut = 6_800

    /** Umbral ambar del shift light. Debajo de esto el arco va verde. */
    override val rpmShiftAmber = 6_000

    /**
     * Carga minima (%) para SEGUIR con el VTEC enganchado.
     *
     * Los logs dan ~91 % al enganchar y 71 % al soltar. Sin guarda, cualquier
     * subida de vueltas en retencion cantaria VTEC.
     */
    override val vtecMinLoadPct = 70

    /**
     * Carga minima (%) para ENGANCHAR: los logs dan ~91 %.
     *
     * ⚠️ Antes se usaba 70 tambien para entrar, y con carga media el tablero
     * lo daba por enganchado cuando el motor todavia no habia cambiado: el
     * dueño lo noto. Ahora entra con 90 y se sostiene con 70, como en los logs.
     */
    override val vtecCargaEnganche = 90

    /** Antiguedad (ms) a partir de la cual un valor se dibuja en gris. */
    override val staleAfterMs = 3_000L

    /** Zona normal de temperatura de refrigerante (°C), para la barra. */
    override val coolantHighC = 105

    /**
     * Escala de color del agua.
     *
     * En el K24 el termostato abre sobre los 80 grados y la temperatura de
     * trabajo se asienta entre 85 y 95. Los umbrales no se afinan al motor
     * porque el margen de peligro lo marca el refrigerante, no el motor.
     */
    override val coolantTibioC = 80
    override val coolantAvisoC = 100

    /**
     * Estequiometrica de la gasolina, para el reloj de mezcla.
     *
     * El Element lleva sonda LAF de BANDA ANCHA aguas arriba (pieza
     * 36531-PZD-A01). Si la ECU expone 0134, de ahi sale
     * una relacion de equivalencia (lambda) y el AFR real es lambda * 14.7.
     *
     * ⚠️ Si 0134 NO esta soportado, el reloj se queda vacio y manda la fila de
     * ajustes. Convertir los ajustes de combustible en un AFR seria inventarlo:
     * los ajustes dicen cuanto corrige la centralita, no que mezcla hay.
     */
    override val afrEstequiometrica = 14.7f

    /** Extremos de la esfera del reloj de mezcla. */
    override val afrMin = 10.0f
    override val afrMax = 20.0f
}
