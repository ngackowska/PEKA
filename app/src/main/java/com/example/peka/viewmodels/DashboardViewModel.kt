package com.example.peka.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.peka.api.TimeData
import com.example.peka.api.pekaApiService
import com.example.peka.database.BusStop
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.location.Location

class DashboardViewModel : ViewModel() {

    // Przechowuje odjazdy przypisane do konkretnego kodu przystanku (stop_code)
    private val _departuresMap = MutableStateFlow<Map<String, List<TimeData>>>(emptyMap())
    val departuresMap: StateFlow<Map<String, List<TimeData>>> = _departuresMap

    // Przykładowe listy przystanków, w przyszłości zasilane z Firebase lub GPS
    private val _favoriteStops = MutableStateFlow<List<BusStop>>(emptyList())
    val favoriteStops: StateFlow<List<BusStop>> = _favoriteStops

    private val _nearestStops = MutableStateFlow<List<BusStop>>(emptyList())
    val nearestStops: StateFlow<List<BusStop>> = _nearestStops

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
        _nearestStops.value = sortedStops.take(3)
    }

}