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

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.sp
import com.example.peka.screens.LiveSearchScreen
import com.example.peka.ui.theme.DarkText
import com.example.peka.ui.theme.HalfTransparentDarkBackground
import com.example.peka.ui.theme.TransparentDarkBackground
import com.example.peka.ui.theme.neumorphicShadow
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

// Nawigacja dashboard (dolny pasek - ulubione, dashboard, mapa)


sealed class Screen(val route: String, val label: String, val icon: @Composable () -> Unit) {
    object Favorites : Screen("favorites", "Ulubione", { Icon(Icons.Filled.Favorite, contentDescription = null, modifier = Modifier.size(34.dp)) })
    object Dashboard : Screen("dashboard", "Dashboard", { Icon(Icons.Filled.Home, contentDescription = null, modifier = Modifier.size(34.dp)) })
    object Map : Screen("map", "Mapa", { Icon(Icons.Filled.Place, contentDescription = null, modifier = Modifier.size(34.dp)) })
    object Search : Screen("search", "Szukaj", { Icon(Icons.Filled.Search, contentDescription = null) })
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
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

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("")}

    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentRoute != Screen.Map.route,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(300.dp)) {
                Row() {
                    Text(
                        text = "ViMo",
                        fontSize = 24.sp,
                        modifier = Modifier.padding(16.dp)
                    )

                    IconButton(onClick = { coroutineScope.launch { drawerState.close() } }) {
                        Icon(Icons.Default.Close, contentDescription = "Zamknij menu")
                    }
                }

                HorizontalDivider()

                NavigationDrawerItem(
                    label = { Text("Wyszukiwanie zaawansowane") },
                    selected = currentRoute == Screen.Search.route, // Opcjonalne: podświetla element, jeśli użytkownik już jest na tym ekranie
                    onClick = {
                        // 1. Zamykamy menu po kliknięciu
                        coroutineScope.launch { drawerState.close() }

                        // 2. Wywołujemy nawigację do ekranu Search
                        bottomNavController.navigate(Screen.Search.route) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )

                NavigationDrawerItem(
                    label = { Text("O autorach") },
                    selected = false,
                    onClick = { /* TODO: Nawigacja do ekranu o autorach */ }
                )
                NavigationDrawerItem(
                    label = { Text("Ustawienia") },
                    selected = false,
                    onClick = { /* TODO: Nawigacja do ustawień */ }
                )
                NavigationDrawerItem(
                    label = { Text("Wyloguj się") },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        FirebaseAuth.getInstance().signOut()
                        onLogoutClick()
                    }
                )
            }
        }
    ) {

        Scaffold(
            topBar = {
                val customBrush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to HalfTransparentDarkBackground,   // Zaczynamy czerwonym na samej górze
                        0.8f to HalfTransparentDarkBackground,   // Trzymamy solidny czerwony aż do równej połowy (50%)
                        1.0f to TransparentDarkBackground   // Od połowy w dół płynnie przechodzimy w niebieski
                    )
                )
                Row(

                    modifier = Modifier
                        .background(brush = customBrush)
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(0.dp, top = 5.dp, bottom = 15.dp, end = 0.dp)
                        .height(85.dp)
                        .fillMaxWidth(),



                ){
                    Card(
                        modifier = Modifier
                            .padding(start = 17.dp, end = 10.dp, top = 17.dp, bottom = 17.dp)
                            .fillMaxHeight()
                            .aspectRatio(1.0f)
                            .neumorphicShadow(
                                cornerRadius = 20.dp,
                                shadowRadius = 10.dp
                            ),

                        colors = CardColors(
                            containerColor = DarkCardBackground,
                            contentColor = DarkText,
                            disabledContainerColor = DarkCardBackground,
                            disabledContentColor = DarkCardBackground
                        ),
                        shape = RoundedCornerShape(50.dp)
                    ){
                        IconButton(
                            modifier = Modifier.padding(10.dp),
                            onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Otwórz menu", tint = DarkText)
                        }
                    }
                    TextField(
                        value = searchQuery,
                        onValueChange = { newValue ->
                            searchQuery = newValue

                            // LOGIKA NAWIGACJI PRZY PISANIU:
                            if (newValue.isNotEmpty() && currentRoute != Screen.Search.route) {
                                // Jeśli użytkownik zaczął pisać, idziemy do Search
                                bottomNavController.navigate(Screen.Search.route) { launchSingleTop = true }
                            } else if (newValue.isEmpty() && currentRoute == Screen.Search.route) {
                                // Jeśli skasował cały tekst, wracamy do poprzedniego ekranu
                                bottomNavController.popBackStack()
                            }
                        },
                        placeholder = { Text("Szukaj przystanku...", color = DarkText, fontSize = 14.sp, lineHeight = 10.sp) },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Szukaj", tint = DarkText) },
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f)
                            .padding(top = 17.dp, bottom = 17.dp, start = 10.dp, end = 17.dp)
                            .neumorphicShadow(
                                cornerRadius = 20.dp,
                                shadowRadius = 10.dp
                            )
                        ,
                        shape = RoundedCornerShape(50.dp),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            unfocusedContainerColor = DarkCardBackground,
                            focusedContainerColor =  DarkCardBackground,
                            unfocusedTextColor = DarkText,
                            focusedTextColor = DarkText
                        ),
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 14.sp, // Twój nowy, większy (lub mniejszy) rozmiar tekstu
                            lineHeight = 10.sp
                        )
                    )

                }






            },
            floatingActionButtonPosition = FabPosition.Center,
            modifier = Modifier,
            floatingActionButton = {
                if (currentRoute != Screen.Search.route) {
                    BoxWithConstraints(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 5.dp)
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
                                    CompositionLocalProvider(LocalRippleConfiguration provides null) {
                                        NavigationBarItem(
                                            modifier = Modifier.offset(y = 5.dp),
                                            icon = screen.icon,
                                            label = {
                                                Text(
                                                    screen.label,
                                                    modifier = Modifier.offset(y = (-8).dp),
                                                    fontSize = 10.sp
                                                )
                                            },
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
                }

            }
        ) { innerPadding ->
            NavHost(
                navController = bottomNavController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier
//                    .padding(innerPadding)
                    .background(DarkBackground)
                    .fillMaxSize()
            ) {
                composable(Screen.Favorites.route) { FavoritesScreen(
                    navController = bottomNavController,
                    onLogout = onLogoutClick,
                    modifier = Modifier.padding(innerPadding)
                ) }
                composable(Screen.Dashboard.route) { DashboardScreen(
                    navController = rootNavController,
                    modifier = Modifier.padding(innerPadding)
                ) }
                composable(Screen.Map.route) { MapScreen(
//                navController = bottomNavController
                    navController = rootNavController,
                    modifier = Modifier.padding(bottom = 140.dp) // 2. ZMIANA: Ręcznie odpychamy treść od dołu o wysokość paska
                ) }

                composable(Screen.Search.route) {
                    LiveSearchScreen(
                        navController = rootNavController, // Główny kontroler, żeby otworzyć pełne szczegóły
                        searchQuery = searchQuery,
                        modifier = Modifier.padding(innerPadding)
                    )
                }



            }
        } // Scaffold

    }


}


