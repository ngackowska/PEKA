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
import com.google.firebase.auth.FirebaseAuth

@Composable
fun FavoritesScreen(
    navController: NavController,
    onLogout: () -> Unit
) {

    Column(
        modifier = Modifier.fillMaxSize(),
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

    }


}