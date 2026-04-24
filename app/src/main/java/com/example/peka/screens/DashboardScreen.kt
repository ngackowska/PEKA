package com.example.peka.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.peka.api.ApiViewModel
import com.example.peka.database.BusStop
import com.example.peka.database.StopsViewModel
import com.example.peka.modules.DepartureCard
import com.example.peka.modules.StopMonitorCard

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: StopsViewModel = viewModel(),
    viewModelDepartures: ApiViewModel = viewModel()
) {

//    val allStops by viewModel.allStops.collectAsState()
////    val stopsToShow = allStops.take(10)
//    val stopsToShow = allStops.filter { it.stop_name == "Kórnicka" }
//    val departuresList by viewModelDepartures.departures.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("ekran dashboard")
        Button(onClick = { navController.navigate("api_screen") }) {
            Text("wróć")
        }


    }

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