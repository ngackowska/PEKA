package com.example.peka

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.peka.ui.theme.PEKATheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.room.Room
import com.example.peka.database.AlarmDao
import com.example.peka.database.AppDatabase
import com.example.peka.database.FavoriteStopDao
import com.example.peka.modules.MainNavigationContainer
import com.example.peka.screens.AlarmScreen
import com.example.peka.screens.BollardsScreen
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

        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "peka_database" // Nazwa pliku bazy w telefonie
        ).fallbackToDestructiveMigration().build()

        // Wyciągasz "pilota" (DAO) do sterowania bazą
        val favoriteStopDao = database.favoriteStopDao()
        val alarmDao = database.alarmDao()


//        enableEdgeToEdge()
        setContent {
            PEKATheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AppNavigation(favoriteStopDao = favoriteStopDao, alarmDao = alarmDao)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    favoriteStopDao: FavoriteStopDao,
    alarmDao: AlarmDao
) {
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
            MapScreen(navController = navController, modifier = Modifier.padding(bottom = 140.dp),favoriteStopDao = favoriteStopDao)
        }

        composable(route = "dashboard_screen") {
            MainNavigationContainer(
                rootNavController = navController,
                onLogoutClick = {
                    navController.navigate("login_screen") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                favoriteStopDao = favoriteStopDao,
                alarmDao = alarmDao
                )
        }

        composable(
            route = "stop_details/{stopCode}",
            arguments = listOf(navArgument("stopCode") { type = NavType.StringType })
        ) { backStackEntry ->
            val code = backStackEntry.arguments?.getString("stopCode") ?: ""
            DetailsScreen(navController = navController, stopCode = code)
        }

        composable(route = "bollards_list/{stopName}") { backStackEntry ->
            val stopName = backStackEntry.arguments?.getString("stopName") ?: ""
            BollardsScreen(navController = navController, stopName = stopName, favoriteStopDao = favoriteStopDao)
        }

        composable("alarm_screen/{stopCode}/{stopName}") { backStackEntry ->
            val stopCode = backStackEntry.arguments?.getString("stopCode") ?: ""
            val stopName = backStackEntry.arguments?.getString("stopName") ?: ""

            AlarmScreen(
                navController = navController, // Lub inny odpowiedni kontroler
                stopCode = stopCode,
                stopName = stopName,
                alarmDao = alarmDao // Przekazujesz nowe DAO
            )
        }

    }
}