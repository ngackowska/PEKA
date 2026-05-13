package com.example.peka.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.peka.viewmodels.StopsViewModel
import com.example.peka.modules.StopMonitorCard
import com.example.peka.ui.theme.DarkHeaderText
import com.example.peka.viewmodels.DashboardViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

import android.util.Log
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.compose.ui.platform.LocalContext


@Composable
fun DashboardScreen(
    navController: NavController,
    dashboardViewModel: DashboardViewModel = viewModel(),
    stopsViewModel: StopsViewModel = viewModel(),
    modifier: Modifier
) {

//    val allStops by viewModel.allStops.collectAsState()
////    val stopsToShow = allStops.take(10)
//    val stopsToShow = allStops.filter { it.stop_name == "Kórnicka" }
//    val departuresList by viewModelDepartures.departures.collectAsState()

    val context = LocalContext.current

    val allStops by stopsViewModel.allStops.collectAsState()
    val nearest by dashboardViewModel.nearestStops.collectAsState()
    val departuresMap by dashboardViewModel.departuresMap.collectAsState()

    // ##################################################
    // lokalizacja użytkownika - do sprawdzenia w poznaniu

    val userLocation by dashboardViewModel.userLocation.collectAsState()

    // Rejestrator zapytania o uprawnienia
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Sprawdzamy, czy użytkownik dał nam chociaż jedno z uprawnień (dokładne LUB przybliżone)
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        Log.d("VIMO_TEST", "Odpowiedź systemu: Fine=$fineGranted, Coarse=$coarseGranted")

        if (fineGranted || coarseGranted) {
            dashboardViewModel.startLocationUpdates()
        } else {
            Log.e("VIMO_TEST", "Użytkownik (lub system) odrzucił uprawnienia!")
        }
    }

    // Pytamy o uprawnienia tylko raz przy starcie ekranu
    LaunchedEffect(Unit) {
        Log.d("VIMO_TEST", "Ekran załadowany, sprawdzam uprawnienia...")
        kotlinx.coroutines.delay(500) // Pół sekundy przerwy dla stabilności systemu

        val hasFineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFineLocation) {
            Log.d("VIMO_TEST", "Aplikacja ma już uprawnienia! Uruchamiam lokalizację.")
            dashboardViewModel.startLocationUpdates()
        } else {
            Log.d("VIMO_TEST", "Brak uprawnień. Wywołuję okienko systemowe.")
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }


    LaunchedEffect(allStops, userLocation) {
        if (allStops.isNotEmpty() && userLocation != null) {
            dashboardViewModel.calculateNearestStops(
                userLat = userLocation!!.latitude,
                userLon = userLocation!!.longitude,
                allStops = allStops
            )
        }
    }

    // ########################################################

    // Aktualnie - sztywna lokalizacja użytkownika (Posnania)

//    val userLat = 52.39837217947031
//    val userLon = 16.954065647695536

    // Lepiej przy zmianie lokalizacji użytkownika???
    // ALBO NIE WIEM

    // Reagujemy na zmianę listy wszystkich przystanków
//    LaunchedEffect(allStops) {
//        if (allStops.isNotEmpty()) {
//            dashboardViewModel.calculateNearestStops(userLat, userLon, allStops)
//        }
//    }

    if (userLocation == null || allStops.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))

                // Diagnostyka na ekranie:
                if (userLocation == null) {
                    Text("Czekam na sygnał GPS / Uprawnienia...", color = Color.White)
                }
                if (allStops.isEmpty()) {
                    Text("Czekam na dane z bazy (Stops)...", color = Color.White)
                }
            }
        }
    } else {
        LazyColumn(modifier = modifier.padding(20.dp, 10.dp)) {
            if (nearest.isNotEmpty()) {
                item {
                    Text(
                        text = "Najbliższe przystanki",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 0.dp, horizontal = 20.dp),
                        color = DarkHeaderText
                    )
                }
                items(nearest) { stop ->
                    // Pobranie czasów odjazdów co 20s
                    LaunchedEffect(stop.stop_code) {
                        while (isActive) {
                            dashboardViewModel.fetchDeparturesForStop(stop.stop_code)
                            delay(20_000L)
                        }
                    }

                    val stopDepartures = departuresMap[stop.stop_code] ?: emptyList()

                    StopMonitorCard(
                        stop = stop,
                        departures = stopDepartures,
                        onClick = {
                            navController.navigate("stop_details/${stop.stop_code}")
                        },
                        isOnMapScreen = false
                    )
                }
            }
        }
    }


}