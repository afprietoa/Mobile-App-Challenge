package com.unal.reto9.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

/** Fuente de ubicación; existe como interfaz para poder sustituirla en los tests instrumentados. */
interface LocationSource {
    /** El llamador debe haber comprobado el permiso. [onResult] recibe null si no hay fix. */
    fun current(onResult: (Location?) -> Unit)
    fun stop()
}

class LocationProvider(context: Context) : LocationSource {
    private val client = LocationServices.getFusedLocationProviderClient(context)
    private var cancel: CancellationTokenSource? = null

    @SuppressLint("MissingPermission")
    override fun current(onResult: (Location?) -> Unit) {
        cancel?.cancel()
        val token = CancellationTokenSource().also { cancel = it }
        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, token.token)
            .addOnSuccessListener(onResult)
            .addOnFailureListener { onResult(null) }
    }

    override fun stop() {
        cancel?.cancel()
    }
}
