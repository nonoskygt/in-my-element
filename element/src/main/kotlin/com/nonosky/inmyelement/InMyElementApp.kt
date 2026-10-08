package com.nonosky.inmyelement

import android.app.Application
import com.nonosky.carsoc.Carro

/**
 * Lo primero que corre en "In my element": le dice a la libreria comun en
 * que carro esta. Android crea la Application antes que cualquier pantalla,
 * servicio o receptor, asi que nada llega a preguntar por el carro antes.
 */
class InMyElementApp : Application() {
    override fun onCreate() {
        Carro.instalar(PerfilElement, MotorK24A4)
        super.onCreate()
    }
}
