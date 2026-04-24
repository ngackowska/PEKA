package com.example.peka.modules

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.peka.BuildConfig.MAPS_API_KEY
import com.example.peka.api.TimeData

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