package com.example.peka.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.example.peka.modules.OSMMapView
import com.example.peka.database.StopsViewModel

@Composable
fun MapScreen(
    navController: NavController,
    viewModel: StopsViewModel = viewModel()
) {

    val allStops by viewModel.allStops.collectAsState()
//    val stopsToShow = allStops.take(10)
//    val stopsToShow = allStops.filter { it.stop_name == "Kórnicka" }

    Column(modifier = Modifier.fillMaxSize()) {
        // Górny pasek z przyciskiem powrotu
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = { navController.popBackStack() }) {
                Text("Wróć")
            }
            Spacer(modifier = Modifier.weight(1f))
            Text("Mapa Przystanków", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }


        Row() {
            OSMMapView(
            stops = allStops,
//                stops = stopsToShow,
                modifier = Modifier.fillMaxSize().padding(30.dp)
            )
        }
    }
}