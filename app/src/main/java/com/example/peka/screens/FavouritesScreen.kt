package com.example.peka.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.peka.database.FavoriteStopDao
import com.example.peka.modules.StopMonitorCard
import com.example.peka.viewmodels.DashboardViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.isActive

@Composable
fun FavoritesScreen(
    navController: NavController,
    rootNavController: NavController,
    onLogout: () -> Unit,
    modifier: Modifier,
    favoriteStopDao: FavoriteStopDao,
    dashboardViewModel: DashboardViewModel = viewModel(),
    ) {

    val departuresMap by dashboardViewModel.departuresMap.collectAsState()


    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("ekran ulubionych")
        Button(onClick = { navController.navigate("home_screen") }) {
            Text("wróć")
        }

        Button(onClick = {
            FirebaseAuth.getInstance().signOut()

            onLogout()
        }) {
            Text("Wyloguj się")
        }

        val favoriteStops = favoriteStopDao.getAllFavorites().collectAsState(initial = emptyList()).value

        if (favoriteStops.isEmpty()) {
            Text(text = "Nie masz jeszcze żadnych ulubionych przystanków.")
        } else {
            // 3. Rysujemy wydajną listę LazyColumn
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp) // Ładny odstęp z każdej strony listy
            ) {
                // Funkcja 'items' automatycznie przejdzie po każdym elemencie bazy
                items(
                    items = favoriteStops,
                    key = { it.stop_code } // 'key' pomaga Compose optymalizować animacje i przewijanie
                ) { favoriteEntity ->

//                    // Tutaj rysujesz wygląd pojedynczego wiersza.
//                    // Możesz stworzyć osobny komponent 'FavoriteStopRow' lub użyć swojego istniejącego.
//                    Row(){
//                        Text(favoriteEntity.stop_name)
//                    }

                    // Pobranie czasów odjazdów co 20s
                    LaunchedEffect(favoriteEntity.stop_code) {
                        while (isActive) {
                            dashboardViewModel.fetchDeparturesForStop(favoriteEntity.stop_code)
                            delay(20_000L)
                        }
                    }

                    val stopDepartures = departuresMap[favoriteEntity.stop_code] ?: emptyList()

                    StopMonitorCard(
                        stop = favoriteEntity,
                        departures = stopDepartures,
                        onClick = {
                            rootNavController.navigate("stop_details/${favoriteEntity.stop_code}")
                        },
                        isOnMapScreen = false
                    )
                }
            }
        }

    }


}