package com.example.peka.screens


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.peka.viewmodels.BollardsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BollardsScreen(
    navController: NavController,
    stopName: String,
    viewModel: BollardsViewModel = viewModel()
) {
    val bollards by viewModel.bollards.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(stopName) {
        viewModel.fetchBollards(stopName)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wybierz przystanek") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(bollards) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // Używamy tag (np. TRAU43) z obiektu bollard
                                navController.navigate("stop_details/${item.bollard.tag}")
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(buildAnnotatedString {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("Nazwa: ") }
                                append(item.bollard.name)
                            }, fontSize = 16.sp)

                            Text(buildAnnotatedString {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("Symbol: ") }
                                append(item.bollard.tag) // Wyświetlamy tag jako symbol
                            }, fontSize = 16.sp)

                            Spacer(modifier = Modifier.height(12.dp))

                            // Łączymy wszystkie kierunki w jeden string
                            val directionsText = item.directions.joinToString(separator = ", ") { dir ->
                                "${dir.lineName} -> ${dir.direction}"
                            }

                            Text(
                                text = directionsText,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}