package com.example.peka.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.peka.ui.theme.DarkAccent
import com.example.peka.ui.theme.DarkCardBackground
import com.example.peka.ui.theme.DarkText
import com.example.peka.viewmodels.StopsViewModel

enum class SearchMode {
    STOP_NAME, STREET
}


@Composable
fun LiveSearchScreen(
    navController: NavController,
    searchQuery: String,
    viewModel: StopsViewModel = viewModel(), // Korzystamy z Twojego cache'a przystanków!,
    modifier: Modifier,
) {
    var searchMode by remember { mutableStateOf(SearchMode.STOP_NAME) }

    val allStops by viewModel.allStops.collectAsState()

    val streetResults by viewModel.streetSearchResults.collectAsState()
    val isSearchLoading by viewModel.isSearchLoading.collectAsState()

//    // Błyskawiczne filtrowanie na żywo w pamięci operacyjnej
//    val uniqueStops = remember(searchQuery, allStops) {
//        if (searchQuery.isBlank()) {
//            emptyList()
//        } else {
//            allStops
//                .filter { stop -> stop.stop_name.contains(searchQuery, ignoreCase = true) }
//                .distinctBy { it.stop_name } // Zostawia tylko pierwszy element o danej nazwie
//        }
//    }


    // 1. REAGUJEMY NA ZMIANY W TEKŚCIE LUB TRYBIE
    LaunchedEffect(searchQuery, searchMode) {
        if (searchMode == SearchMode.STREET) {
            viewModel.searchByStreet(searchQuery)
        }
    }

    // 2. PRZYGOTOWANIE ZUNIFIKOWANEJ LISTY WYNIKÓW (SAME NAZWY)
    val displayList: List<String> = remember(searchQuery, searchMode, allStops, streetResults) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else if (searchMode == SearchMode.STOP_NAME) {
            // Lokalnie: szukamy w pamięci podręcznej i wyciągamy nazwy
            allStops
                .filter { it.stop_name.contains(searchQuery, ignoreCase = true) }
                .map { it.stop_name }
                .distinct()
        } else {
            // Chmurowo: bierzemy wyniki z ViewModelu
            streetResults
        }
    }


    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        // Przełącznik (Segmented Control)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .background(DarkCardBackground, RoundedCornerShape(50.dp)),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Przycisk "Przystanek"
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { searchMode = SearchMode.STOP_NAME }
                    .background(
                        if (searchMode == SearchMode.STOP_NAME) DarkAccent.copy(alpha = 0.2f) else Color.Transparent,
                        RoundedCornerShape(50.dp)
                    )
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Przystanek",
                    color = if (searchMode == SearchMode.STOP_NAME) DarkAccent else DarkText,
                    fontWeight = if (searchMode == SearchMode.STOP_NAME) FontWeight.Bold else FontWeight.Normal
                )
            }

            // Przycisk "Ulica"
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { searchMode = SearchMode.STREET }
                    .background(
                        if (searchMode == SearchMode.STREET) DarkAccent.copy(alpha = 0.2f) else Color.Transparent,
                        RoundedCornerShape(50.dp)
                    )
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Ulica",
                    color = if (searchMode == SearchMode.STREET) DarkAccent else DarkText,
                    fontWeight = if (searchMode == SearchMode.STREET) FontWeight.Bold else FontWeight.Normal
                )
            }
        }



        // Komunikaty o stanie
        if (searchMode == SearchMode.STREET && isSearchLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                color = DarkAccent
            )
        } else if (displayList.isEmpty() && searchQuery.isBlank()) {
            Text(
                text = "Tu pojawią się wyniki wyszukiwania.",
                color = Color.White,
                modifier = Modifier.padding(top = 16.dp)
            )
        } else if (displayList.isEmpty() && searchQuery.isNotBlank()) {
            Text(
                text = "Brak wyników dla: $searchQuery",
                color = Color.White,
                modifier = Modifier.padding(top = 16.dp)
            )
        }




//        if (uniqueStops.isEmpty() && searchQuery.isBlank()) {
//            Text(
//                text = "Tu pojawią się wyniki wyszukiwania.",
//                color = Color.White,
//                modifier = Modifier.padding(top = 16.dp)
//            )
//        }
//
//        if (uniqueStops.isEmpty() && searchQuery.isNotBlank()) {
//            Text(
//                text = "Brak wyników dla: $searchQuery",
//                color = Color.White,
//                modifier = Modifier.padding(top = 16.dp)
//            )
//        }


        // Wyniki
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(displayList) { stopName ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate("bollards_list/$stopName")
                        },
                    colors = CardDefaults.cardColors(containerColor = DarkCardBackground),
                    shape = RoundedCornerShape(30.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp, 12.dp)) {
                        Text(
                            text = stopName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText
                        )
                    }
                }
            }
        }





//        LazyColumn(
//            modifier = Modifier.fillMaxSize(),
//            verticalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            items(uniqueStops) { stop ->
//                Card(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clickable {
//                            // Otwieramy ekran odjazdów używając rootNavController
//                            navController.navigate("bollards_list/${stop.stop_name}")
//                        },
//                    colors = CardDefaults.cardColors(containerColor = DarkCardBackground),
//                    shape = RoundedCornerShape( 30.dp)
//                ) {
//                    Column(modifier = Modifier.padding(24.dp, 12.dp)) {
//                        Text(text = stop.stop_name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText)
//                    }
//                }
//            }
//        }
    }
}