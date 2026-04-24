package com.example.peka.modules

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
import com.example.peka.api.TimeData
import com.example.peka.database.BusStop

@Composable
fun StopMonitorCard(
    stop: BusStop,
    departures: List<TimeData> // Używamy Twojego modelu z poprzednich kroków
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp) // Stała wysokość karty, aby lista wyglądała równo
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {

            // ==========================================
            // LEWA STRONA: Mała mapka (Statyczny obraz)
            // ==========================================
            Box(
                modifier = Modifier
                    .weight(0.4f) // Zajmuje 40% szerokości
                    .fillMaxHeight()
                    .background(Color.LightGray), // Tło zastępcze
                contentAlignment = Alignment.Center
            ) {
                // Tutaj w przyszłości wstawisz AsyncImage z biblioteki Coil (ładowanie z URL)
                // lub Image z zasobów lokalnych (R.drawable.map_placeholder)

//                Text("Mapka\n(Obrazek)", fontSize = 12.sp, color = Color.DarkGray)

                val mapImageUrl = getStaticMapUrl(lat = stop.stop_lat, lon = stop.stop_lon)


                println("MÓJ LINK DO MAPY: $mapImageUrl")

                AsyncImage(
                    model = mapImageUrl,
                    contentDescription = "Mapa dla przystanku ${stop.stop_name}",
                    modifier = Modifier
//                        .weight(0.4f)
                        .fillMaxHeight(),
                    contentScale = ContentScale.Crop // Ważne: przycina obrazek tak, aby ładnie wypełnił swoje 40% miejsca bez zniekształceń
                )

            }

            // ==========================================
            // PRAWA STRONA: Wirtualny Monitor
            // ==========================================
            Column(
                modifier = Modifier
                    .weight(0.6f) // Zajmuje 60% szerokości
                    .fillMaxHeight()
                    .background(Color(0xFF1E1E1E)) // Ciemnoszare/Czarne tło monitora
                    .padding(8.dp)
            ) {
                // Nagłówek monitora (Nazwa przystanku)
                Text(
                    text = stop.stop_name.uppercase(),
                    color = Color(0xFFFFB300), // Pomarańczowy kolor (jak LED)
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // Lista odjazdów na monitorze (ograniczona np. do 3 najbliższych)
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

// Komponent pomocniczy dla pojedynczego wiersza na monitorze
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
                text = timeInfo.line.padEnd(3, ' '), // Wyrównanie numeru linii
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace // Czcionka o stałej szerokości liter
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