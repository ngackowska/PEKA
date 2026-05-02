package com.example.peka.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.peka.database.BusStop
import com.example.peka.modules.OSMMapView
import com.example.peka.viewmodels.StopsViewModel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*


@Composable
fun MapScreen(
    navController: NavController,
    viewModel: StopsViewModel = viewModel()
) {

    val allStops by viewModel.allStops.collectAsState()
//    val stopsToShow = allStops.take(10)
//    val stopsToShow = allStops.filter { it.stop_name == "Kórnicka" }
    var selectedStop by remember { mutableStateOf<BusStop?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {



        OSMMapView(
            stops = allStops,
//                stops = stopsToShow,
            modifier = Modifier.fillMaxSize().padding(30.dp),
            onMarkerClick = { clickedStop ->
                selectedStop = clickedStop
            }
        )


        AnimatedVisibility(
            visible = selectedStop != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            selectedStop?.let { stop ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stop.stop_name,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                            IconButton(onClick = { selectedStop = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Zamknij")
                            }
                        }

                        Text(
                            text = "Kod słupka: ${stop.stop_code}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Strefa: ${stop.zone_id}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))


                        Button(
                            onClick = {
                                navController.navigate("stop_details/${stop.stop_code}")
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Pokaż odjazdy")
                        }
                    }
                }
            }
        }

    }
}