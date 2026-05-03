package com.example.peka.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.peka.ui.theme.DarkCardBackground
import com.example.peka.viewmodels.StopsViewModel

@Composable
fun LiveSearchScreen(
    navController: NavController,
    searchQuery: String,
    viewModel: StopsViewModel = viewModel() // Korzystamy z Twojego cache'a przystanków!
) {
    val allStops by viewModel.allStops.collectAsState()

    // Błyskawiczne filtrowanie na żywo w pamięci operacyjnej
    val uniqueStops = remember(searchQuery, allStops) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            allStops
                .filter { stop -> stop.stop_name.contains(searchQuery, ignoreCase = true) }
                .distinctBy { it.stop_name } // Zostawia tylko pierwszy element o danej nazwie
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (uniqueStops.isEmpty() && searchQuery.isNotBlank()) {
            Text(
                text = "Brak wyników dla: $searchQuery",
                color = Color.White,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uniqueStops) { stop ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // Otwieramy ekran odjazdów używając rootNavController
                            navController.navigate("bollards_list/${stop.stop_name}")
                        },
                    colors = CardDefaults.cardColors(containerColor = DarkCardBackground)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = stop.stop_name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}