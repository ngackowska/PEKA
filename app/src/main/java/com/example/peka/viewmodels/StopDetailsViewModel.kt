package com.example.peka.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.peka.api.TimeData
import com.example.peka.api.pekaApiService
import com.example.peka.database.BusStop
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class StopDetailsViewModel : ViewModel() {

    // Trzymamy odjazdy tylko dla tego konkretnego przystanku (nie ma potrzeby używania Map/Słownika)
    private val _departures = MutableStateFlow<List<TimeData>>(emptyList())
    val departures: StateFlow<List<TimeData>> = _departures

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    // --- SEKCJA BAZY DANYCH (Nowa) ---
    private val db = FirebaseFirestore.getInstance()

    // Używamy opcjonalnego (nullable) BusStop?, bo na początku nie mamy jeszcze danych
    private val _stopDetails = MutableStateFlow<BusStop?>(null)
    val stopDetails: StateFlow<BusStop?> = _stopDetails


    // Wspólna funkcja startowa, która odpala i API, i bazę
    fun startLoadingData(stopCode: String) {
        fetchStopInfoFromFirebase(stopCode)
        startFetchingDepartures(stopCode)
    }

    private fun fetchStopInfoFromFirebase(stopCode: String) {
        db.collection("bus_stops")
            .whereEqualTo("stop_code", stopCode) // <-- Magia filtrowania
            .limit(1) // Zabezpieczenie: interesuje nas tylko pierwszy trafiony wynik
            .get(Source.CACHE)
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Pobieramy pierwszy (i jedyny) dokument z wyników
                    val document = querySnapshot.documents[0]

                    try {
                        val stopId = document.getLong("stop_id")?.toInt() ?: 0
                        val name = document.getString("stop_name") ?: ""
                        val lat = document.getDouble("stop_lat") ?: 0.0
                        val lon = document.getDouble("stop_lon") ?: 0.0
                        val zoneId = document.getString("zone_id") ?: ""

                        _stopDetails.value = BusStop(stopId, stopCode, name, lat, lon, zoneId)
                    } catch (e: Exception) {
                        Log.e("StopDetails", "Błąd parsowania przystanku: ${e.message}")
                    }
                } else {
                    Log.d("StopDetails", "Nie znaleziono przystanku o kodzie $stopCode w bazie.")
                }
            }
            .addOnFailureListener { e ->
                Log.e("StopDetails", "Błąd pobierania z bazy: ${e.message}")
            }
    }


    // Funkcja odpalana przez ekran z przekazanym kodem
    fun startFetchingDepartures(stopCode: String) {
        viewModelScope.launch {
            // Pętla odświeżająca dane np. co 15 sekund
            while (isActive) {
                try {
                    val p0Json = "{\"symbol\":\"$stopCode\"}"
                    val response = pekaApiService.getTimes(p0 = p0Json)

                    _departures.value = response.success.times
                    _isLoading.value = false
                } catch (e: Exception) {
                    e.printStackTrace()
                    _isLoading.value = false
                }

                delay(15_000L) // Czekamy 15 sekund przed kolejnym odświeżeniem
            }
        }
    }
}