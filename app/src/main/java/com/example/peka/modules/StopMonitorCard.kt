package com.example.peka.modules

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.peka.BuildConfig.MAPS_API_KEY
import com.example.peka.api.TimeData
import com.example.peka.database.BusStop
import androidx.compose.foundation.clickable

// Komponent kafelka z dashboard (obrazek mapki + odjazdy)
@Composable
fun StopMonitorCard(
    stop: BusStop,
    departures: List<TimeData>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {

            // Obrazek mapki z Geoapify
            Box(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight()
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {

                val mapImageUrl = getStaticMapUrl(lat = stop.stop_lat, lon = stop.stop_lon)

                AsyncImage(
                    model = mapImageUrl,
                    contentDescription = "Mapa dla przystanku ${stop.stop_name}",
                    modifier = Modifier
                        .fillMaxHeight(),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxHeight()
                    .background(Color(0xFF1E1E1E))
                    .padding(8.dp)
            ) {
                // Nagłówek monitora (Nazwa przystanku)
                Text(
                    text = stop.stop_name.uppercase(),
                    color = Color(0xFFFFB300),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // Lista odjazdów
                val displayLimit = 3
                if (departures.isEmpty()) {
                    Text(text = "Brak odjazdów", color = Color.Red, fontSize = 12.sp)
                } else {
                    departures.take(displayLimit).forEach { timeInfo ->
                        MonitorRow(timeInfo)
                    }
                }
            }
        }
    }
}

// Pobranie obrazka mapi z Geoapify
fun getStaticMapUrl(lat: Double, lon: Double): String {
    val apiKey = MAPS_API_KEY
    val zoom = 17

    Log.d("KORDY", lat.toString())
    Log.d("KORDY", lon.toString())

    return "https://maps.geoapify.com/v1/staticmap?style=osm-carto&width=400&height=400&center=lonlat:$lon,$lat&zoom=$zoom&marker=lonlat:$lon,$lat;type:material;color:%23ff0000&apiKey=$apiKey"
}

// Wiersz w liście odjazdów
@Composable
fun MonitorRow(timeInfo: TimeData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Numer linii i kierunek
        Row(modifier = Modifier.weight(1f)) {
            Text(
                text = timeInfo.line.padEnd(3, ' '),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = timeInfo.direction,
                color = Color.White,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Czas do odjazdu
        Text(
            text = "${timeInfo.minutes}m",
            color = if (timeInfo.minutes <= 3) Color(0xFF00FF00) else Color.White, // Zielony jeśli blisko
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
        )
    }
}