package com.example.peka

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.peka.database.BusStop

class StopsViewModel : ViewModel() {

    // 1. Inicjalizacja bazy
    private val db = FirebaseFirestore.getInstance()

    private val _allStops = MutableStateFlow<List<BusStop>>(emptyList())
    val allStops: StateFlow<List<BusStop>> = _allStops

    init {
        // Zmieniono na PersistentCacheSettings (Pamięć na dysku)
        val settings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
            .build()

        db.firestoreSettings = settings

        // Pobranie danych
        fetchStopsFromFirebase()
    }

    private fun fetchStopsFromFirebase() {
        db.collection("bus_stops")
            .get()
            .addOnSuccessListener { result ->
                val stopsList = mutableListOf<BusStop>()

                for (document in result) {
                    try {
                        // Ręczne wyciąganie każdego pola z dokumentu Firebase
                        val stopId = document.getLong("stop_id")?.toInt() ?: 0
                        val stopCode = document.getString("stop_code") ?: ""
                        val stopName = document.getString("stop_name") ?: ""
                        val stopLat = document.getDouble("stop_lat") ?: 0.0
                        val stopLon = document.getDouble("stop_lon") ?: 0.0
                        val zoneId = document.getString("zone_id") ?: ""

                        // Budowanie obiektu BusStop
                        val stop = BusStop(
                            stop_id = stopId,
                            stop_code = stopCode,
                            stop_name = stopName,
                            stop_lat = stopLat,
                            stop_lon = stopLon,
                            zone_id = zoneId
                        )

                        stopsList.add(stop)
                    } catch (e: Exception) {
                        println("Pominięto błędny dokument: ${document.id}")
                    }
                }

                _allStops.value = stopsList
                android.util.Log.d("TEST_BAZY", "Pobrano dokładnie ${stopsList.size} przystanków z chmury.")
                if (stopsList.isNotEmpty()) {
                    val first = stopsList[0]
                    android.util.Log.d("TEST_BAZY", "Pierwszy to: ${first.stop_name}, lat: ${first.stop_lat}, lon: ${first.stop_lon}")
                }
            }
            .addOnFailureListener { exception ->
                println("Błąd pobierania przystanków: ${exception.message}")
            }
    }
}