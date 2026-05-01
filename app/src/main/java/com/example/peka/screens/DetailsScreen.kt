package com.example.peka.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.peka.modules.DepartureCard
import com.example.peka.modules.getStaticMapUrl
import com.example.peka.viewmodels.StopDetailsViewModel

@Composable
fun DetailsScreen(
    navController: NavController,
    stopCode: String,
    viewModel: StopDetailsViewModel = viewModel()
) {
//    val departuresList by viewModel.departures.collectAsState()
//    val stopName by viewModel.stopName.collectAsState()

    val departures by viewModel.departures.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val stopDetails by viewModel.stopDetails.collectAsState()


    LaunchedEffect(stopCode) {
        viewModel.startLoadingData(stopCode)
    }

    Column(
        modifier = Modifier
//            .fillMaxSize()

            .padding(16.dp),
//        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Button(onClick = { navController.popBackStack() }) {
            Text("Wróć")
        }
        if (stopDetails != null) {

            Text(
                text = "${stopDetails!!.stop_name} (${stopDetails!!.zone_id})",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = "Kordynaty: ${stopDetails!!.stop_lat}, ${stopDetails!!.stop_lon}",
                fontSize = 12.sp,
                color = androidx.compose.ui.graphics.Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        } else {

            Text(
                text = "Przystanek: $stopCode",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        if (isLoading && departures.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.padding(32.dp))
        } else {
            LazyColumn(
                modifier = Modifier.weight(0.75f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(departures) { timeData ->
                    DepartureCard(timeData)
                }
            }

            // ####################################################
            // TU DAŁAM OBRAZEK PRZYSTANKU ALE MOŻNA DAĆ MAPĘ? IDK
            // ####################################################

            if (stopDetails != null) {

                Box(
                    modifier = Modifier
                        .weight(0.25f)
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {


                    val mapImageUrl = getStaticMapUrl(lat = stopDetails!!.stop_lat, lon = stopDetails!!.stop_lon)


                    println("MÓJ LINK DO MAPY: $mapImageUrl")

                    AsyncImage(
                        model = mapImageUrl,
                        contentDescription = "Mapa dla przystanku ${stopDetails!!.stop_name}",
                        modifier = Modifier
//                        .weight(0.4f)
                            .fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                }

            } else {
                Box(modifier = Modifier.weight(0.25f).fillMaxWidth())
            }


        }


    }
}