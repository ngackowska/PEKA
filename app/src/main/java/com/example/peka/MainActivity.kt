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
import com.example.peka.modules.MainNavigationContainer
import com.example.peka.screens.ApiScreen
import com.example.peka.screens.DashboardScreen
import com.example.peka.screens.HomeScreen
import com.example.peka.screens.LoginScreen
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

    NavHost(navController = navController, startDestination = "login_screen") {
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
            MainNavigationContainer(rootNavController = navController)
        }
    }
}