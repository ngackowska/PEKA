package com.example.peka.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.peka.api.ApiViewModel
import com.example.peka.database.BusStop
import com.example.peka.database.StopsViewModel
import com.example.peka.modules.DepartureCard
import com.example.peka.modules.StopMonitorCard
import com.example.peka.viewmodels.DashboardViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun DashboardScreen(
    dashboardViewModel: DashboardViewModel = viewModel(),
    stopsViewModel: StopsViewModel = viewModel()
) {

//    val allStops by viewModel.allStops.collectAsState()
////    val stopsToShow = allStops.take(10)
//    val stopsToShow = allStops.filter { it.stop_name == "Kórnicka" }
//    val departuresList by viewModelDepartures.departures.collectAsState()

    val allStops by stopsViewModel.allStops.collectAsState()
    val nearest by dashboardViewModel.nearestStops.collectAsState()
    val departuresMap by dashboardViewModel.departuresMap.collectAsState()

    // ##################################################
    // lokalizacja użytkownika - do sprawdzenia w poznaniu
//
//    val userLocation by dashboardViewModel.userLocation.collectAsState()
//
//    // Rejestrator zapytania o uprawnienia
//    val permissionLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.RequestPermission()
//    ) { isGranted ->
//        if (isGranted) {
//            // Gdy użytkownik kliknie "Zezwól", uruchamiamy strumień lokalizacji
//            dashboardViewModel.startLocationUpdates()
//        } else {
//            // Opcjonalnie: obsługa braku zgody (np. wyświetlenie komunikatu)
//        }
//    }
//
//    // Pytamy o uprawnienia tylko raz przy starcie ekranu
//    LaunchedEffect(Unit) {
//        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
//    }
//
//
//    LaunchedEffect(allStops, userLocation) {
//        if (allStops.isNotEmpty() && userLocation != null) {
//            dashboardViewModel.calculateNearestStops(
//                userLat = userLocation!!.latitude,
//                userLon = userLocation!!.longitude,
//                allStops = allStops
//            )
//        }
//    }

    // ########################################################

    val userLat = 52.39837217947031
    val userLon = 16.954065647695536

    // Lepiej przy zmianie lokalizacji użytkownika???
    // ALBO NIE WIEM

    // Reagujemy na zmianę listy wszystkich przystanków
    LaunchedEffect(allStops) {
        if (allStops.isNotEmpty()) {
            dashboardViewModel.calculateNearestStops(userLat, userLon, allStops)
        }
    }

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        if (nearest.isNotEmpty()) {
            item {
                Text(
                    text = "Najbliższe przystanki",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(nearest) { stop ->
                // Pobieramy odjazdy dla każdego wyświetlonego przystanku
                LaunchedEffect(stop.stop_code) {
                    while (isActive) {
                        dashboardViewModel.fetchDeparturesForStop(stop.stop_code)
                        delay(20_000L)
                    }
                }

                val stopDepartures = departuresMap[stop.stop_code] ?: emptyList()

                StopMonitorCard(
                    stop = stop,
                    departures = stopDepartures
                )
            }
        }
    }



//    Column(
//        modifier = Modifier.fillMaxSize(),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text("ekran dashboard")
//        Button(onClick = { navController.navigate("api_screen") }) {
//            Text("wróć")
//        }
//
//
//
//
//    }

//    LazyColumn(
//        verticalArrangement = Arrangement.spacedBy(8.dp)
//    ) {
//        items(stopsToShow) { stop ->
//            StopMonitorCard(
//                BusStop(stop.stop_id,
//            )
//        }
//    }


}