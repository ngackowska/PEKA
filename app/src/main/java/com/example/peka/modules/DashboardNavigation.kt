package com.example.peka.modules

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
import com.example.peka.ui.theme.DarkBackground
import com.example.peka.ui.theme.DarkCardBackground
import com.example.peka.ui.theme.DarkNavIcon
import com.example.peka.ui.theme.TransparentDarkCardBackground


// Nawigacja dashboard (dolny pasek - ulubione, dashboard, mapa)


sealed class Screen(val route: String, val label: String, val icon: @Composable () -> Unit) {
    object Favorites : Screen("favorites", "Ulubione", { Icon(Icons.Filled.Favorite, contentDescription = null, modifier = Modifier.size(32.dp)) })
    object Dashboard : Screen("dashboard", "Dashboard", { Icon(Icons.Filled.Home, contentDescription = null, modifier = Modifier.size(32.dp)) })
    object Map : Screen("map", "Mapa", { Icon(Icons.Filled.Place, contentDescription = null, modifier = Modifier.size(32.dp)) })
}

@SuppressLint("UnusedBoxWithConstraintsScope")
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
        floatingActionButtonPosition = FabPosition.Center,
        modifier = Modifier,
        floatingActionButton = {
            BoxWithConstraints(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)


            ){
                Card(modifier = Modifier
                    .width((maxWidth.value * 0.75).dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = CardColors(
                        containerColor = DarkCardBackground,
                        contentColor = TransparentDarkCardBackground,
                        disabledContainerColor = TransparentDarkCardBackground,
                        disabledContentColor = TransparentDarkCardBackground
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 8.dp,     // Podstawowy cień
                    )

                ){
                    NavigationBar(modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp, 5.dp),
                        windowInsets = WindowInsets(0.dp),
                        containerColor = DarkCardBackground,
                    )
                    {
                        val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination

                        items.forEach { screen ->
                            NavigationBarItem(
                                modifier = Modifier,
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
                                },
                                colors = NavigationBarItemDefaults.colors(

                                    // 1. EFEKT AKTYWNEJ STRONY ("Pigułka" w tle ikony)
                                    // Używam tu zielonego koloru z 20% przezroczystości (alpha = 0.2f) dla fajnego efektu
                                    indicatorColor = Color(0xFF20CE55).copy(alpha = 0.0f),
                                    // Jeśli chcesz to CAŁKOWICIE UKRYĆ, użyj: Color.Transparent

                                    // 2. KOLORY IKON
                                    selectedIconColor = Color(0xFF20CE55), // Ikona aktualnej strony
                                    unselectedIconColor = DarkNavIcon, // Ikony pozostałych stron

                                    // 3. KOLORY TEKSTÓW (jeśli etykiety są widoczne)
                                    selectedTextColor = Color(0xFF20CE55),
                                    unselectedTextColor = DarkNavIcon
                                )
                            )
                        }

                    }

                }


            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
                .background(DarkBackground)
                .fillMaxHeight()
        ) {
            composable(Screen.Favorites.route) { FavoritesScreen(
                navController = bottomNavController,
                onLogout = onLogoutClick
            ) }
            composable(Screen.Dashboard.route) { DashboardScreen(
                navController = rootNavController
            ) }
            composable(Screen.Map.route) { MapScreen(
//                navController = bottomNavController
                navController = rootNavController
            ) }
        }
    }
}


