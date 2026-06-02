package com.example.peka.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.peka.database.BusStop
import com.example.peka.modules.OSMMapView
import com.example.peka.viewmodels.StopsViewModel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.peka.modules.StopMonitorCard
import com.example.peka.viewmodels.DashboardViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import com.example.peka.database.FavoriteStopDao
import com.example.peka.modules.FavoriteButton
import com.example.peka.ui.theme.HalfTransparentDarkBackground
import com.example.peka.ui.theme.TransparentDarkBackground


@Composable
fun MapScreen(
    navController: NavController,
    viewModel: StopsViewModel = viewModel(),
    dashboardViewModel: DashboardViewModel = viewModel(),
    modifier: Modifier = Modifier,
    favoriteStopDao: FavoriteStopDao
) {

    val context = LocalContext.current
    val allStops by viewModel.allStops.collectAsState()
    var selectedStop by remember { mutableStateOf<BusStop?>(null) }
    val departuresMap by dashboardViewModel.departuresMap.collectAsState()
    var gradientHeight by remember { mutableStateOf<Dp>(100.dp) }

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            // Skoro mamy już uprawnienia z Dashboardu, po prostu odpalamy antenę
            dashboardViewModel.startLocationUpdates()
        }
    }


    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {

        OSMMapView(
            stops = allStops,
//                stops = stopsToShow,
            modifier = Modifier.fillMaxSize(),
            onMarkerClick = { clickedStop ->
                selectedStop = clickedStop
                gradientHeight = 500.dp
            },
            onMapClick = {
                selectedStop = null
                gradientHeight = 100.dp
            },
            selectedStop = selectedStop,
            dashboardViewModel = dashboardViewModel
        )

        val customBrush = Brush.verticalGradient(
            colorStops = arrayOf(
                0.0f to TransparentDarkBackground,
                0.5f to HalfTransparentDarkBackground,   // Zaczynamy czerwonym na samej górze
                1.0f to HalfTransparentDarkBackground,   // Trzymamy solidny czerwony aż do równej połowy (50%)
                  // Od połowy w dół płynnie przechodzimy w niebieski
            )
        )

        Box(
            modifier = Modifier.fillMaxWidth()
                .height(gradientHeight)
                .background(brush = customBrush)
        ){

        }


        AnimatedVisibility(
            visible = selectedStop != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, end = 16.dp, bottom = 110.dp)
        ) {
            selectedStop?.let { stop ->

                LaunchedEffect(stop.stop_code) {
                    while (isActive) {
                        dashboardViewModel.fetchDeparturesForStop(stop.stop_code)
                        delay(20_000L)
                    }
                }

                val stopDepartures = departuresMap[stop.stop_code] ?: emptyList()
                Box(
                    modifier = Modifier.padding(bottom=50.dp),
                    contentAlignment = Alignment.BottomStart
                ){
                    StopMonitorCard(
                        stop = stop,
                        departures = stopDepartures,
                        onClick = {
                            navController.navigate("stop_details/${stop.stop_code}")
                        },
                        isOnMapScreen = true,
                        isOnFavouriteScreen = false

                    )
                    FavoriteButton(
                        stop = stop,
                        dao = favoriteStopDao,
                        modifier = Modifier.padding(10.dp, 20.dp)
                    )

                }

            }
        }

    }
}