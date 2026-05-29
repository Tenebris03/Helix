package com.tenebris.health_tracker.data.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat

data class DeviceLocation(
    val latitude: Double,
    val longitude: Double
)

class LocationProvider(private val context: Context) {

    fun getLastKnownLocation(): DeviceLocation? {
        if (!hasPermission()) return null

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null

        val providers = listOf(
            LocationManager.NETWORK_PROVIDER,
            LocationManager.GPS_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )

        for (provider in providers) {
            try {
                val location = locationManager.getLastKnownLocation(provider) ?: continue
                return DeviceLocation(location.latitude, location.longitude)
            } catch (_: SecurityException) {
                continue
            } catch (_: IllegalArgumentException) {
                continue
            }
        }
        return null
    }

    private fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}
