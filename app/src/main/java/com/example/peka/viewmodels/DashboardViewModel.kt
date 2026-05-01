package com.example.peka.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.ViewModel
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

    // Przechowuje odjazdy przypisane do konkretnego kodu przystanku (stop_code)
    private val _departuresMap = MutableStateFlow<Map<String, List<TimeData>>>(emptyMap())
    val departuresMap: StateFlow<Map<String, List<TimeData>>> = _departuresMap

    // Przykładowe listy przystanków, w przyszłości zasilane z Firebase lub GPS
    private val _favoriteStops = MutableStateFlow<List<BusStop>>(emptyList())
    val favoriteStops: StateFlow<List<BusStop>> = _favoriteStops

    private val _nearestStops = MutableStateFlow<List<BusStop>>(emptyList())
    val nearestStops: StateFlow<List<BusStop>> = _nearestStops


    // Stan przechowujący aktualną pozycję użytkownika
    private val _userLocation = MutableStateFlow<android.location.Location?>(null)
    val userLocation: StateFlow<android.location.Location?> = _userLocation

    // Klient usług lokalizacyjnych Google
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    // Adnotacja tłumiąca błąd o braku uprawnień (sprawdzimy je w warstwie UI przed wywołaniem tej funkcji)
    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        // Konfiguracja reguł odświeżania lokalizacji
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L) // sprawdzaj co ~10 sekund
            .setMinUpdateDistanceMeters(30f) // aktualizuj pozycję tylko jeśli użytkownik przeszedł co najmniej 30 metrów
            .build()

        // Co ma się stać, gdy telefon odbierze nową lokalizację
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    // Aktualizujemy stan. To automatycznie powiadomi UI i zaktualizuje dystans do przystanków
                    _userLocation.value = location
                }
            }
        }

        // Uruchomienie ciągłego nasłuchiwania
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    fun fetchDeparturesForStop(stopCode: String) {
        viewModelScope.launch {
            try {
                // Tworzymy JSON w oparciu o przekazany kod przystanku
                val p0Json = "{\"symbol\":\"$stopCode\"}"
                val response = pekaApiService.getTimes(p0 = p0Json)

                // Kopiujemy obecną mapę, dodajemy nowe dane i aktualizujemy stan
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
            // Funkcja oblicza dystans w metrach i zapisuje wynik do tablicy 'results'
            Location.distanceBetween(userLat, userLon, stop.stop_lat, stop.stop_lon, results)
            results[0]
        }

        // Zapisujemy 3 pierwsze elementy z posortowanej listy
        _nearestStops.value = sortedStops.take(2)
    }

}