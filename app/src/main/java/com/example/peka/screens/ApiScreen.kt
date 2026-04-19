package com.example.peka.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.peka.ApiViewModel
import com.example.peka.DepartureCard
import com.example.peka.StopMonitorCard
import com.example.peka.database.BusStop

@Composable
fun ApiScreen(
    navController: NavController,
    viewModel: ApiViewModel = viewModel()
) {
    val departuresList by viewModel.departures.collectAsState()
    val stopName by viewModel.stopName.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
//        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { navController.popBackStack() }) {
            Text("Wróć")
        }

        StopMonitorCard(BusStop(106, "TRAU43", "Traugutta", 52.3847652100, 16.9151159000, "A"), departuresList)

        Text(
            text = "Przystanek: $stopName",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(departuresList) { timeData ->
                DepartureCard(timeData)
            }
        }


    }
}