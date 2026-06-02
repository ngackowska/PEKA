package com.example.peka.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.peka.R
import com.example.peka.modules.DepartureCard
import com.example.peka.modules.getStaticMapUrl
import com.example.peka.ui.theme.DarkBackground
import com.example.peka.ui.theme.DarkCardBackground
import com.example.peka.ui.theme.DarkText
import com.example.peka.ui.theme.TransparentDarkBackground
import com.example.peka.ui.theme.neumorphicShadow
import com.example.peka.viewmodels.StopDetailsViewModel

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun DetailsScreen(
    navController: NavController,
    stopCode: String,
    viewModel: StopDetailsViewModel = viewModel()
) {
//    val departuresList by viewModel.departures.collectAsState()
//    val stopName by viewModel.stopName.collectAsState()

    val departures by viewModel.departures.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val stopDetails by viewModel.stopDetails.collectAsState()

    Scaffold(
        modifier = Modifier.background(DarkBackground),
        containerColor = DarkBackground
    ) { innerPadding ->

        LaunchedEffect(stopCode) {
            viewModel.startLoadingData(stopCode)
        }
        Box(modifier = Modifier.padding(innerPadding)){

            Column(
                modifier = Modifier
//            .fillMaxSize()
                    .background(DarkBackground)
                    .padding(20.dp),
//        verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                ){
                    Column(){
                        Spacer(modifier = Modifier.height(6.dp))

                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = DarkText,
                            modifier = Modifier
                                .clickable(onClick = {navController.popBackStack()}, enabled = true)
                                .padding(8.dp,12.dp )
                                .width(24.dp)
                        )


                    }


                    Column(
                        modifier = Modifier.fillMaxWidth().padding(end=40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,

                    ){

                        if (stopDetails != null) {

                            Text(
                                text = "${stopDetails!!.stop_name}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 16.dp),
                                color = DarkText
                            )
                            Text(
                                text = "Strefa ${stopDetails!!.zone_id}",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        } else {

                            Text(
                                text = "Przystanek: $stopCode",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }

                    }

                }



                if (isLoading && departures.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.padding(32.dp))
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(0.75f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(22.dp)
                    ) {
                        item{
                            Spacer(modifier = Modifier.padding(vertical = 0.dp))
                        }
                        items(departures) { timeData ->
                            DepartureCard(timeData)
                        }
                        item{
                            Spacer(modifier = Modifier.padding(vertical = 0.dp))
                        }
                    }

                    // ####################################################
                    // TU DAŁAM OBRAZEK PRZYSTANKU ALE MOŻNA DAĆ MAPĘ? IDK
                    // ####################################################

                    if (stopDetails != null) {

                            BoxWithConstraints(
                                modifier = Modifier
                                    .weight(0.25f)
                                    .fillMaxWidth()
                                    .padding(top = 16.dp)
                                    .background(TransparentDarkBackground),
                                contentAlignment = Alignment.Center,

                                ) {



                                val mapImageUrl = getStaticMapUrl(lat = stopDetails!!.stop_lat, lon = stopDetails!!.stop_lon, width = maxWidth.value, height = maxHeight.value)

                                println("MÓJ LINK DO MAPY: $mapImageUrl")
                                Card( shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier
                                        .neumorphicShadow(
                                            20.dp,
                                            10.dp
                                )){

                                AsyncImage(
                                    model = mapImageUrl,
                                    contentDescription = "Mapa dla przystanku ${stopDetails!!.stop_name}",
                                    modifier = Modifier
//                        .weight(0.4f)
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

                        }


                    } else {
                        Box(modifier = Modifier.weight(0.25f).fillMaxWidth())
                    }


                }


            }

        }



    }


}