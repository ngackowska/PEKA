package com.example.peka.screens

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

@Composable
fun DashboardScreen(
    navController: NavController,
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

    val userLat = 52.4064
    val userLon = 16.9252

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
                    text = "3 Najbliższe przystanki",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(nearest) { stop ->
                // Pobieramy odjazdy dla każdego wyświetlonego przystanku
                LaunchedEffect(stop.stop_code) {
                    dashboardViewModel.fetchDeparturesForStop(stop.stop_code)
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