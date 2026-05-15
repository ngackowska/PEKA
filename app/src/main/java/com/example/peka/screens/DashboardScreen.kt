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

    val context = LocalContext.current

    val allStops by stopsViewModel.allStops.collectAsState()
    val nearest by dashboardViewModel.nearestStops.collectAsState()
    val departuresMap by dashboardViewModel.departuresMap.collectAsState()
    val userLocation by dashboardViewModel.userLocation.collectAsState()

    // Rejestrator uprawnień
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineGranted || coarseGranted) {
            dashboardViewModel.startLocationUpdates()
        }
    }

    // SPRAWDZENIE UPRAWNIEŃ PRZY STARCIE
    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            // Jeśli już daliśmy zgodę w ustawieniach, od razu włączamy GPS
            dashboardViewModel.startLocationUpdates()
        } else {
            // W przeciwnym razie prosimy o okienko
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // 3. WYLICZANIE NAJBLIŻSZYCH PRZYSTANKÓW
    LaunchedEffect(allStops, userLocation) {
        if (allStops.isNotEmpty() && userLocation != null) {
            dashboardViewModel.calculateNearestStops(
                userLat = userLocation!!.latitude,
                userLon = userLocation!!.longitude,
                allStops = allStops
            )
        }
    }

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
        } else {
            item {
                CircularProgressIndicator(color = Color(0xFF20CE55))
            }
        }
    }


}