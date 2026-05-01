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
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.peka.BuildConfig.MAPS_API_KEY
import com.example.peka.database.BusStop
import com.example.peka.modules.MainNavigationContainer
import com.example.peka.screens.ApiScreen
import com.example.peka.screens.DashboardScreen
import com.example.peka.screens.DetailsScreen
import com.example.peka.screens.HomeScreen
import com.example.peka.screens.LoginScreen
import com.example.peka.screens.MapScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ===== NOWY KOD: Globalna konfiguracja bazy Firebase =====
        try {
            val db = FirebaseFirestore.getInstance()
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                .build()
            db.firestoreSettings = settings
        } catch (e: Exception) {
            // Ignorujemy błąd - oznacza to, że baza już zdążyła się zainicjować
            // swoimi domyślnymi ustawieniami (które i tak wspierają cache)
        }
        // =========================================================

//        enableEdgeToEdge()
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

    val auth = FirebaseAuth.getInstance()

    val startScreen = if (auth.currentUser != null) {
        "dashboard_screen"
    } else {
        "login_screen"
    }

    NavHost(navController = navController, startDestination = startScreen) {
        composable(route = "home_screen") {
            HomeScreen(navController = navController)
        }
        composable(route = "login_screen") {
            LoginScreen(navController = navController)
        }

        composable(route = "api_screen") {
            ApiScreen(navController = navController)
        }
        composable(route = "map_screen") {
            MapScreen(navController = navController)
        }
        composable(route = "dashboard_screen") {
            MainNavigationContainer(
                rootNavController = navController,
                onLogoutClick = {
                    // Ten kod wykona się, gdy klikniesz przycisk w FavoritesScreen!

                    // Używamy głównego kontrolera, żeby przeskoczyć do ekranu logowania
                    navController.navigate("login_screen") {
                        // Czyścimy WSZYSTKO od samego początku stosu
                        // inclusive = true oznacza, że usuwamy też sam startowy punkt
                        popUpTo(0) { inclusive = true }
                    }
                })
        }

        composable(
            route = "stop_details/{stopCode}",
            arguments = listOf(navArgument("stopCode") { type = NavType.StringType })
        ) { backStackEntry ->

            // Wydobywamy parametr ze ścieżki (lub podajemy pusty string jako zabezpieczenie)
            val code = backStackEntry.arguments?.getString("stopCode") ?: ""

            // Rysujemy nowy ekran i wstrzykujemy mu wydobyty kod
            DetailsScreen(navController = navController, stopCode = code)
        }

    }
}