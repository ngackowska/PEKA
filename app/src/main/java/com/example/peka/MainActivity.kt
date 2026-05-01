package com.example.peka

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.peka.ui.theme.PEKATheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.peka.modules.MainNavigationContainer
import com.example.peka.screens.DetailsScreen
import com.example.peka.screens.LoginScreen
import com.example.peka.screens.MapScreen
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            val db = FirebaseFirestore.getInstance()
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                .build()
            db.firestoreSettings = settings
        } catch (e: Exception) {
        }


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
        composable(route = "login_screen") {
            LoginScreen(navController = navController)
        }

        composable(route = "map_screen") {
            MapScreen(navController = navController)
        }

        composable(route = "dashboard_screen") {
            MainNavigationContainer(
                rootNavController = navController,
                onLogoutClick = {
                    navController.navigate("login_screen") {
                        popUpTo(0) { inclusive = true }
                    }
                })
        }

        composable(
            route = "stop_details/{stopCode}",
            arguments = listOf(navArgument("stopCode") { type = NavType.StringType })
        ) { backStackEntry ->
            val code = backStackEntry.arguments?.getString("stopCode") ?: ""
            DetailsScreen(navController = navController, stopCode = code)
        }

    }
}