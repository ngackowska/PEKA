package com.example.peka.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.peka.database.BusStop
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StopsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FirebaseFirestore.getInstance()

    private val _allStops = MutableStateFlow<List<BusStop>>(emptyList())
    val allStops: StateFlow<List<BusStop>> = _allStops

    private val sharedPreferences = application.getSharedPreferences("peka_prefs", Context.MODE_PRIVATE)
    private val VERSION_KEY = "stops_version"



    private val _streetSearchResults = MutableStateFlow<List<String>>(emptyList())
    val streetSearchResults: StateFlow<List<String>> = _streetSearchResults

    private val _isSearchLoading = MutableStateFlow(false)
    val isSearchLoading: StateFlow<Boolean> = _isSearchLoading

    private var searchJob: kotlinx.coroutines.Job? = null



//    init {
//        val settings = FirebaseFirestoreSettings.Builder()
//            .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
//            .build()
//
//        db.firestoreSettings = settings
//
//        checkAndFetchStops()
//    }

    init {
        checkAndFetchStops()
    }



    fun searchByStreet(query: String) {
        // Anulujemy poprzednie zapytanie, jeśli użytkownik wpisał kolejną literę
        searchJob?.cancel()

        if (query.isBlank()) {
            _streetSearchResults.value = emptyList()
            return
        }

        searchJob = viewModelScope.launch(Dispatchers.IO) {
            delay(500) // Czekamy pół sekundy, aż użytkownik przestanie pisać

            _isSearchLoading.value = true
            try {
                val p0Json = "{\"name\":\"$query\"}"
                // Korzystamy z API
                val response = com.example.peka.api.pekaApiService.getBollardsByStreet(p0 = p0Json)

                // Wyciągamy z odpowiedzi same unikalne nazwy przystanków
                val distinctNames = response.success.bollards.map { it.bollard.name }.distinct()
                _streetSearchResults.value = distinctNames

            } catch (e: Exception) {
                e.printStackTrace()
                _streetSearchResults.value = emptyList()
            } finally {
                _isSearchLoading.value = false
            }
        }
    }



    private fun checkAndFetchStops() {
        val localVersion = sharedPreferences.getInt(VERSION_KEY, 0)
        Log.d("TEST_BAZY", "Lokalna wersja bazy: $localVersion")

        db.collection("system_data").document("metadata")
            .get(Source.SERVER)
            .addOnSuccessListener { metaDocument ->
                val serverVersion = metaDocument.getLong("stops_version")?.toInt() ?: 1
                Log.d("TEST_BAZY", "Serwerowa wersja bazy: $serverVersion")

                if (serverVersion > localVersion) {
                    Log.d("TEST_BAZY", "Wykryto nową wersję. Pobieram dane z SERWERA.")
                    fetchStopsFromFirebase(Source.SERVER, serverVersion)
                } else {
                    Log.d("TEST_BAZY", "Baza jest aktualna. Ładuję dane z pamięci CACHE.")
                    fetchStopsFromFirebase(Source.CACHE, null)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("TEST_BAZY", "Błąd sprawdzania wersji: ${exception.message}. Próba wczytania z pamięci.")
                fetchStopsFromFirebase(Source.CACHE, null)
            }
    }

    private fun fetchStopsFromFirebase(dataSource: Source, serverVersion: Int?) {
        db.collection("bus_stops")
            .get(dataSource)
            .addOnSuccessListener { result ->
                val stopsList = mutableListOf<BusStop>()

                for (document in result) {
                    try {
                        val stopId = document.getLong("stop_id")?.toInt() ?: 0
                        val stopCode = document.getString("stop_code") ?: ""
                        val stopName = document.getString("stop_name") ?: ""
                        val stopLat = document.getDouble("stop_lat") ?: 0.0
                        val stopLon = document.getDouble("stop_lon") ?: 0.0
                        val zoneId = document.getString("zone_id") ?: ""

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
                Log.d("TEST_BAZY", "Pobrano dokładnie ${stopsList.size} przystanków.")
                if (serverVersion != null) {
                    sharedPreferences.edit().putInt(VERSION_KEY, serverVersion).apply()
                    Log.d("TEST_BAZY", "Zapisano nową wersję ($serverVersion) w pamięci telefonu.")
                }
            }
            .addOnFailureListener { exception ->
                println("Błąd pobierania przystanków: ${exception.message}")
            }
    }
}