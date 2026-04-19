package com.example.peka

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.peka.ui.theme.PEKATheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.items
import com.example.peka.BuildConfig.MAPS_API_KEY
import com.example.peka.database.BusStop
import com.example.peka.screens.ApiScreen
import com.example.peka.screens.DashboardScreen
import com.example.peka.screens.HomeScreen
import com.example.peka.screens.MapScreen


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            PEKATheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home_screen") {
        composable(route = "home_screen") {
            HomeScreen(navController = navController)
        }
        composable(route = "api_screen") {
            ApiScreen(navController = navController)
        }
        composable(route = "map_screen") {
            MapScreen(navController = navController)
        }
        composable(route = "dashboard_screen") {
            DashboardScreen(navController = navController)
        }
    }
}


// Funkcja generująca link do statycznego obrazka mapy
fun getStaticMapUrl(lat: Double, lon: Double): String {
    // UWAGA: Zarejestruj się na darmowym koncie Geoapify (lub Mapbox) i podmień ten klucz
    val apiKey = MAPS_API_KEY
    val zoom = 16 // Przybliżenie mapy

    Log.d("KORDY", lat.toString())
    Log.d("KORDY", lon.toString())

    // Zwracamy gotowy link URL.
    // Zawiera on centrum mapy (center) oraz czerwoną pinezkę (marker) w tym samym miejscu.
    return "https://maps.geoapify.com/v1/staticmap?style=osm-carto&width=400&height=400&center=lonlat:$lon,$lat&zoom=$zoom&marker=lonlat:$lon,$lat;type:material;color:%23ff0000&apiKey=$apiKey"
}

@Composable
fun DepartureCard(timeData: TimeData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Linia: ${timeData.line}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Kierunek: ${timeData.direction}")

                // Przykład wykorzystania parametru opcjonalnego
//                if (timeData.airCnd == true) {
//                    Text(text = "Klimatyzacja: Tak", fontSize = 12.sp)
//                }
            }
            Text(
                text = "${timeData.minutes} min",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp
            )
        }
    }
}