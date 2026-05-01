package com.example.peka.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.viewModelScope
import com.example.peka.api.TimeData
import com.example.peka.api.pekaApiService
import com.example.peka.database.BusStop
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.location.Location
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.*

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    // Odjazdy przypisane do konkretnego kodu przystanku (stop_code)
    private val _departuresMap = MutableStateFlow<Map<String, List<TimeData>>>(emptyMap())
    val departuresMap: StateFlow<Map<String, List<TimeData>>> = _departuresMap

    // ###################################################
    // NA PÓŹNIEJ DO FIREBASE (users --> favorite_stops)
    // ###################################################
    private val _favoriteStops = MutableStateFlow<List<BusStop>>(emptyList())
    val favoriteStops: StateFlow<List<BusStop>> = _favoriteStops

    private val _nearestStops = MutableStateFlow<List<BusStop>>(emptyList())
    val nearestStops: StateFlow<List<BusStop>> = _nearestStops


    private val _userLocation = MutableStateFlow<android.location.Location?>(null)
    val userLocation: StateFlow<android.location.Location?> = _userLocation
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    // ###################################################
    // TEŻ NA PÓŹNIEJ DO LOKALIZACJI
    // ###################################################
    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        // Sprawdza pozycję użytkownika co 10s, aktualizuje jeżeli przemieści się o co najmniej 30 metrów
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L)
            .setMinUpdateDistanceMeters(30f)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    _userLocation.value = location
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    fun fetchDeparturesForStop(stopCode: String) {
        viewModelScope.launch {
            try {
                val p0Json = "{\"symbol\":\"$stopCode\"}"
                val response = pekaApiService.getTimes(p0 = p0Json)

                val updatedMap = _departuresMap.value.toMutableMap()
                updatedMap[stopCode] = response.success.times
                _departuresMap.value = updatedMap

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun calculateNearestStops(userLat: Double, userLon: Double, allStops: List<BusStop>) {
        if (allStops.isEmpty()) return

        val sortedStops = allStops.sortedBy { stop ->
            val results = FloatArray(1)
            Location.distanceBetween(userLat, userLon, stop.stop_lat, stop.stop_lon, results)
            results[0]
        }

        _nearestStops.value = sortedStops.take(2)
    }

}