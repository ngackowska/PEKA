package com.example.peka.modules

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.peka.screens.DashboardScreen
import com.example.peka.screens.MapScreen
import com.example.peka.screens.FavoritesScreen


// Nawigacja dashboard (dolny pasek - ulubione, dashboard, mapa)


sealed class Screen(val route: String, val label: String, val icon: @Composable () -> Unit) {
    object Favorites : Screen("favorites", "Ulubione", { Icon(Icons.Filled.Favorite, contentDescription = null) })
    object Dashboard : Screen("dashboard", "Dashboard", { Icon(Icons.Filled.Home, contentDescription = null) })
    object Map : Screen("map", "Mapa", { Icon(Icons.Filled.Place, contentDescription = null) })
}

@Composable
fun MainNavigationContainer(
    rootNavController: NavHostController,
    onLogoutClick: () -> Unit
) {
    val bottomNavController = rememberNavController()
    val items = listOf(
        Screen.Favorites,
        Screen.Dashboard,
        Screen.Map
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = screen.icon,
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            bottomNavController.navigate(screen.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Favorites.route) { FavoritesScreen(
                navController = bottomNavController,
                onLogout = onLogoutClick
            ) }
            composable(Screen.Dashboard.route) { DashboardScreen(
                navController = rootNavController
            ) }
            composable(Screen.Map.route) { MapScreen(
                navController = bottomNavController
            ) }
        }
    }
}


