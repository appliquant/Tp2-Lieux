package com.example.tp2.db

import android.app.Application

/**
 * Utilisé dans le manifest
 */
class PlaceApplication : Application() {
    val database: PlaceDatabase by lazy { PlaceDatabase.DBSingelton.getInstance(this) }
}