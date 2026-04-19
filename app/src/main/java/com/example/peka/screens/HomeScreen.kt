package com.example.peka.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("ekran główny")
        Button(onClick = { navController.navigate("api_screen") }) {
            Text("Przejdź do drugiego ekranu")
        }
        Text("ekran mapy")
        Button(onClick = { navController.navigate("map_screen") }) {
            Text("Przejdź do mapy")
        }

        Text("DASHBOARD")
        Button(onClick = { navController.navigate("dashboard_screen") }) {
            Text("DASHBOARD")
        }
    }
}