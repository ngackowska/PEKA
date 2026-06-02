package com.example.peka.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.peka.database.AlarmDao
import com.example.peka.database.FavoriteStopDao
import com.example.peka.modules.StopMonitorCard
import com.example.peka.ui.theme.DarkHeaderText
import com.example.peka.viewmodels.DashboardViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun FavoritesScreen(
    rootNavController: NavController,
    onLogout: () -> Unit,
    modifier: Modifier,
    favoriteStopDao: FavoriteStopDao,
    dashboardViewModel: DashboardViewModel = viewModel(),
    alarmDao: AlarmDao
    ) {

    val departuresMap by dashboardViewModel.departuresMap.collectAsState()



    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val favoriteStops = favoriteStopDao.getAllFavorites().collectAsState(initial = emptyList()).value

        if (favoriteStops.isEmpty()) {
            Text(text = "Nie masz jeszcze żadnych ulubionych przystanków.")
        } else {
            // 3. Rysujemy wydajną listę LazyColumn
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp) // Ładny odstęp z każdej strony listy
            ) {
                item{
                    Spacer(modifier = Modifier.height(5.dp))
                }

                item {
                    Text(
                        text = "Ulubione przystanki",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 0.dp, horizontal = 20.dp),
                        color = DarkHeaderText
                    )
                }
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

                    val alarm by alarmDao.getAlarmForStop(favoriteEntity.stop_code).collectAsState(initial = null)

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
                        isOnMapScreen = false,

                        isFavorite = true,
                        alarmLine = alarm?.line,
                        onAlarmClick = {
                            rootNavController.navigate("alarm_screen/${favoriteEntity.stop_code}/${favoriteEntity.stop_name}")
                        },
                        isOnFavouriteScreen = true
                    )
                }
            }
        }

    }


}