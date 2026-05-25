package com.example.peka.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.peka.api.TimeData
import com.example.peka.api.pekaApiService
import com.example.peka.database.BusStop
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.location.Location
import android.location.LocationManager
import android.location.LocationListener
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import com.example.peka.database.AlarmEntity
import com.example.peka.database.FavoriteStopDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers

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
//    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)


    private val locationManager = application.getSystemService(Context.LOCATION_SERVICE) as LocationManager


    private var hasSyncedFavorites = false

    fun syncFavoritesFromCloud(
        dao: FavoriteStopDao,
        alarmDao: com.example.peka.database.AlarmDao,
        allStops: List<BusStop>
    ) {
        // Jeśli już zsynchronizowaliśmy dane w tej sesji lub nie mamy jeszcze listy przystanków - przerywamy
        if (hasSyncedFavorites || allStops.isEmpty()) return

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(uid).get().addOnSuccessListener { snapshot ->
            // Pobieramy listę kodów (Stringów) z chmury
            val favoriteCodes = snapshot.get("favorite_stops") as? List<String> ?: emptyList()

            val remoteAlarms = snapshot.get("alarms") as? Map<String, Map<String, Any>> ?: emptyMap()

            viewModelScope.launch(Dispatchers.IO) {
                // MAGIA: Filtrujemy wszystkie przystanki (allStops), zostawiając tylko te z Firebase
                val stopsToInsert = allStops.filter { favoriteCodes.contains(it.stop_code) }

                // Wrzucamy pełne obiekty do lokalnej bazy Room
                stopsToInsert.forEach { dao.insert(it) }

                if (remoteAlarms is Map<*, *>) {
                    remoteAlarms.forEach { (stopCode, data) ->
                        val alarmMap = data as? Map<*, *> ?: return@forEach

                        val alarmEntity = AlarmEntity(
                            stop_code = stopCode.toString(),
                            line = alarmMap["line"]?.toString() ?: "",
                            startTime = alarmMap["startTime"]?.toString() ?: "07:00",
                            endTime = alarmMap["endTime"]?.toString() ?: "09:00",
                            minutesBefore = (alarmMap["minutesBefore"] as? Long)?.toInt() ?: 5
                        )
                        alarmDao.insertAlarm(alarmEntity)
                    }
                }


            }

            // Zaznaczamy, że pobieranie zakończone sukcesem
            hasSyncedFavorites = true
        }
    }


    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
//        try {
//            // 1. SZYBKI START: Próbujemy pobrać ostatnią znaną lokalizację od razu
//            val lastKnownGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
//            val lastKnownNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
//
//            // Wybieramy tę, która jest dostępna (GPS jest dokładniejszy, Network jest szybszy)
//            val bestLastKnown = lastKnownGps ?: lastKnownNetwork
//            if (bestLastKnown != null) {
//                _userLocation.value = bestLastKnown
//            }
//
//            // 2. NASŁUCHIWANIE NA ŻYWO: Aktualizuj, jeśli użytkownik przemieści się o 30 metrów
//            val locationListener = object : LocationListener {
//                override fun onLocationChanged(location: Location) {
//                    _userLocation.value = location
//                }
//                // Wymagane przez starsze wersje Androida
//                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
//            }
//
//            // Rejestrujemy nasłuchiwacz (używamy GPS_PROVIDER dla dokładności)
//            locationManager.requestLocationUpdates(
//                LocationManager.GPS_PROVIDER,
//                10000L, // Sprawdzaj co 10 sekund
//                30f,    // Zmiana o minimum 30 metrów
//                locationListener
//            )
//
//        } catch (e: SecurityException) {
//            e.printStackTrace() // Brak uprawnień (choć zablokowaliśmy to już w widoku)
//        } catch (e: Exception) {
//            e.printStackTrace() // Inne błędy, np. wyłączony całkowicie GPS w telefonie
//        }

        val mockLocation = android.location.Location(android.location.LocationManager.GPS_PROVIDER).apply {
            latitude = 52.4037  // Tutaj wpisz docelową szerokość geograficzną
            longitude = 16.9541 // Tutaj wpisz docelową długość geograficzną
        }

        _userLocation.value = mockLocation

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