package com.example.peka.screens


import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.peka.R
import com.example.peka.database.FavoriteStopDao
import com.example.peka.modules.FavoriteButton
import com.example.peka.modules.getStaticMapUrl
import com.example.peka.ui.theme.DarkBackground
import com.example.peka.ui.theme.DarkCardBackground
import com.example.peka.ui.theme.DarkHeaderText
import com.example.peka.ui.theme.DarkText
import com.example.peka.ui.theme.TransparentDarkBackground
import com.example.peka.ui.theme.insetNeumorphicShadow
import com.example.peka.ui.theme.neumorphicShadow
import com.example.peka.viewmodels.BollardsViewModel
import com.example.peka.viewmodels.StopDetailsViewModel
import com.example.peka.viewmodels.StopsViewModel

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BollardsScreen(
    navController: NavController,
    stopName: String,
    stopsViewModel: StopsViewModel = viewModel(),
    viewModel: BollardsViewModel = viewModel(),
    favoriteStopDao: FavoriteStopDao
) {
    val allStops by stopsViewModel.allStops.collectAsState()

    val bollards by viewModel.bollards.collectAsState()

    val isLoading by viewModel.isLoading.collectAsState()

    val stopsMap = remember(bollards, allStops) {
        if (bollards.isEmpty() || allStops.isEmpty()) {
            emptyMap() // Jeśli czegokolwiek brakuje, zwracamy pustą mapę (kręci się kółko)
        } else {
            // Obliczamy mapę błyskawicznie w locie
            allStops
                .filter { stop ->
                    bollards.any { it.bollard.tag.trim().equals(stop.stop_code.trim(), ignoreCase = true) }
                }
                .associateBy { it.stop_code.trim() }
        }
    }

    LaunchedEffect(stopName) {
        viewModel.fetchBollards(stopName)
    }

    // 1. Definiujemy "pożeracza" scrolla
    val consumeAllScrollBehavior = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                // available to ta "siła scrolla", która została po dojechaniu do końca listy.
                // Zwracając 'available', mówimy systemowi: "Biorę to wszystko, nie przekazuj wyżej!"
                return available
            }
        }
    }

    Scaffold(
        modifier = Modifier.background(DarkBackground),
        containerColor = DarkBackground,
        topBar = {

        }
    ) { padding ->
        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier
                .fillMaxWidth()
                .padding(padding))
        } else {

            Box(modifier = Modifier
                .padding(padding)
                .fillMaxSize()){
                Column(
                    modifier = Modifier
//            .fillMaxSize()
                        .background(DarkBackground)
                        ,
//        verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp, 10.dp)
                    ){
                        Column(){
                            Spacer(modifier = Modifier.height(6.dp))

                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = DarkText,
                                modifier = Modifier
                                    .clickable(
                                        onClick = { navController.popBackStack() },
                                        enabled = true
                                    )
                                    .padding(8.dp, 12.dp)
                                    .width(24.dp)
                            )


                        }


                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,

                            ){
                            Text(
                                text = "Wybierz przystanek",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 16.dp),
                                color = DarkText
                            )

                        }



                    }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(30.dp),
                    ) {
                        item { Spacer(Modifier.height(0.dp)) }
                        itemsIndexed(bollards) { index, item ->
                            Card(
                                modifier = Modifier
                                    .neumorphicShadow(20.dp, 10.dp)
                                    .fillMaxWidth()
                                    .clickable {
                                        // Używamy tag (np. TRAU43) z obiektu bollard
                                        navController.navigate("stop_details/${item.bollard.tag}")
                                    },
                                shape = RoundedCornerShape(20.dp),
                                colors = CardColors(
                                    containerColor = DarkCardBackground,
                                    contentColor = DarkText,
                                    disabledContainerColor = DarkCardBackground,
                                    disabledContentColor = DarkText
                                )
                            ) {



                                Box() {

                                    Column() {
                                        Row(
                                            modifier = Modifier.fillMaxWidth()
                                                .padding(top = 14.dp, start = 22.dp, end = 22.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.Bottom) {
                                                Text(
                                                    text = "Nazwa",
                                                    fontWeight = FontWeight.Light,
                                                    fontSize = 16.sp,
                                                    modifier = Modifier.alignByBaseline(),
                                                    color = DarkText
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = item.bollard.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 20.sp,
                                                    modifier = Modifier.alignByBaseline(),
                                                    color = DarkText
                                                )
                                            }
                                            Row(verticalAlignment = Alignment.Bottom) {
                                                Text(
                                                    text = item.bollard.tag,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp,
                                                    modifier = Modifier.alignByBaseline(),
                                                    color = DarkText
                                                )
                                            }
                                        }


                                        Spacer(modifier = Modifier.height(12.dp))

                                        Card(
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .padding(20.dp, 0.dp)
                                                .insetNeumorphicShadow(10.dp, 10.dp),
                                            colors = CardColors(
                                                containerColor = DarkBackground,
                                                contentColor = DarkText,
                                                disabledContainerColor = DarkBackground,
                                                disabledContentColor = DarkText
                                            )

                                        ) {
                                            LazyColumn(
                                                modifier = Modifier
                                                    .padding(15.dp, 0.dp)
                                                    .fillMaxWidth() // Rozciąga się na boki
                                                    .heightIn(max = 94.dp) // <-- TUTAJ: Sztywna wysokość idealnie na 3 elementy!  // Lekkie tło dla widoczności obszaru
                                                    .nestedScroll(consumeAllScrollBehavior),
                                                contentPadding = PaddingValues(0.dp), // Usuwamy wewnętrzny margines, żeby łatwiej liczyć // Odstępy między elementami
                                            ) {
                                                item { Spacer(Modifier.height(10.dp)) }
                                                items(item.directions) { dir ->
                                                    Text(
                                                        "${dir.lineName} -> ${dir.direction}",
                                                        fontSize = 14.sp,
                                                        color = DarkHeaderText
                                                    )
                                                }
                                                item { Spacer(Modifier.height(10.dp)) }

                                            }


                                        }



                                        BoxWithConstraints(
                                            modifier = Modifier
                                                .height(150.dp)
                                                .fillMaxWidth()
                                                .padding(20.dp)
                                                .background(TransparentDarkBackground),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            // Kopiujemy stan do lokalnej zmiennej dla bezpiecznego "Smart Cast"

                                            // Sprawdzamy, czy dane już się pobrały (czy nie są nullem)


                                            val details = stopsMap[item.bollard.tag]
                                            // Skoro weszliśmy do tego IF-a, Kotlin wie, że 'details' na 100% ma dane.
                                            // Nie potrzebujemy już wykrzykników !!
                                            if (details != null) {
                                                val mapImageUrl = getStaticMapUrl(
                                                    lat = details.stop_lat,
                                                    lon = details.stop_lon,
                                                    width = maxWidth.value,
                                                    height = maxHeight.value
                                                )

                                                Card(
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.insetNeumorphicShadow(
                                                        10.dp,
                                                        10.dp
                                                    )

                                                ) {
                                                    AsyncImage(
                                                        model = mapImageUrl,
                                                        contentDescription = "Mapa dla przystanku ${details.stop_name}",
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .background(DarkCardBackground),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                }
                                                Icon(
                                                    painter = painterResource(id = R.drawable.bus_stop_marker), // Twoja ikona z drawable
                                                    contentDescription = "Lokalizacja",
                                                    tint = Color.Unspecified, // Zachowaj oryginalne kolory z pliku SVG/XML
                                                    modifier = Modifier
                                                        .size(20.dp) // Ustaw rozmiar pinezki

                                                        // ⚠️ KLUCZOWA POPRAWKA (Grawitacja pinezki):
                                                        // Domyślnie środek ikony będzie na środku mapy.
                                                        // Klasyczna pinezka ma jednak "ostrze" na samym dole.
                                                        // Aby to ostrze wskazywało idealny punkt, musimy przesunąć ikonę
                                                        // w górę o połowę jej wysokości (40dp / 2 = 20dp).
                                                        .offset(y = (12).dp)
                                                )
                                            } else {
                                                // CO POKAZAĆ, GDY DANE JESZCZE SIĘ POBIERAJĄ?
                                                // Możesz tu dać np. kręcące się kółko ładowania albo puste tło
                                                CircularProgressIndicator(
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                    val details = stopsMap[item.bollard.tag]
                                    if (details != null){
                                        Card(
                                            Modifier.align(Alignment.BottomStart),
                                            shape = RoundedCornerShape(20.dp),
                                            colors = CardColors(
                                                containerColor = DarkCardBackground,
                                                contentColor = DarkText,
                                                disabledContainerColor = DarkCardBackground,
                                                disabledContentColor = DarkText
                                            )
                                        ){
                                            FavoriteButton(details,favoriteStopDao,
                                                modifier =
                                                    Modifier.padding(5.dp,2.dp,2.dp,5.dp)
                                                ,)

                                        }


                                    }
                                }
                            }
                        }
                        item { Spacer(Modifier.height(8.dp)) }
                    }


                }

            }


        }
    }
}