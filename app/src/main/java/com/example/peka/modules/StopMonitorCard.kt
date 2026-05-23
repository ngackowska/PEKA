package com.example.peka.modules

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import com.example.peka.ui.theme.DarkAccent
import com.example.peka.ui.theme.DarkCardBackground
import com.example.peka.ui.theme.DarkHeaderText
import com.example.peka.ui.theme.HalfTransparentDarkCardBackground
import com.example.peka.ui.theme.TransparentDarkCardBackground
import com.example.peka.ui.theme.neumorphicShadow

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon


// Komponent kafelka z dashboard (obrazek mapki + odjazdy)
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun StopMonitorCard(
    stop: BusStop,
    departures: List<TimeData>,
    onClick: () -> Unit,
    isOnMapScreen: Boolean,
    isFavorite: Boolean = false, // Sprawdzamy czy to ulubiony
    alarmLine: String? = null,   // Numer linii, jeśli alarm jest ustawiony
    onAlarmClick: () -> Unit = {} // Akcja po kliknięciu w dzwoneczek
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.75f)
            .padding(horizontal = 0.dp, vertical = 12.dp)
            .clickable { onClick() }
            .neumorphicShadow(
                cornerRadius = 20.dp,
                shadowRadius = 10.dp
            )
            // 2. POTEM nakładamy tło i zaokrąglenie (MUSI być takie samo jak w cieniu!)
            .background(
                color = DarkCardBackground,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box {

            // Warstwa 1 | Mapa z Nagłówkiem
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .background(DarkCardBackground)
            ) {

                Text(
                    text = stop.stop_name,
                    color = DarkHeaderText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(18.dp, 12.dp, 16.dp, 0.dp).fillMaxWidth()
                )

                // Obrazek mapki z Geoapify
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                        .background(DarkCardBackground),
                    contentAlignment = Alignment.Center
                ) {
                    if (!isOnMapScreen){
                        val currentWidthDp = maxWidth.value
                        val currentHeightDp = maxHeight.value

                        val mapImageUrl = getStaticMapUrl(
                            lat = stop.stop_lat,
                            lon = stop.stop_lon,
                            width = currentWidthDp,
                            height = currentHeightDp
                        )

                        AsyncImage(
                            model = mapImageUrl,
                            contentDescription = "Mapa dla przystanku ${stop.stop_name}",
                            modifier = Modifier
                                .fillMaxHeight(),
                            alignment = Alignment.CenterEnd,
                            contentScale = ContentScale.Crop
                        )
                    }


                }
            }


            // Warstwa 2 | Gradient
            val gradientColors = listOf(
                DarkCardBackground,
                TransparentDarkCardBackground // Ciemnoniebieski
                  // Jasnoniebieski
            )
            Column(modifier = Modifier.fillMaxHeight().fillMaxWidth()){
                Text(
                    text = stop.stop_name,
                    color = DarkHeaderText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(18.dp, 12.dp, 16.dp, 0.dp).fillMaxWidth()
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.3f)
                        .background(brush = Brush.verticalGradient(colors = gradientColors))

                ){}
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.7f)

                ){}
            }


            // Warstwa 3 | Dane odjazdów
            Row(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(0.45f)
                ) {}

                Column(
                    modifier = Modifier
                        .weight(0.55f)
                        .fillMaxHeight()
                        .background(TransparentDarkCardBackground)
                ) {

                    BoxWithConstraints(modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()){

                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .blur(6.dp)
                        )
                        {
                            Box(
                                modifier = Modifier
                                    .padding(6.dp, 14.dp)
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .background(HalfTransparentDarkCardBackground)
                            )
                        }


                        Column(modifier = Modifier
                            .fillMaxHeight()
                            .padding(12.dp, 14.dp, 20.dp, 14.dp)
                        ){
                            // Lista odjazdów
                            val displayLimit = 5
                            if (departures.isEmpty()) {
                                Text(text = "Brak odjazdów", color = Color.Red, fontSize = 12.sp)
                            } else {
                                departures.take(displayLimit).forEach { timeInfo ->
                                    MonitorRow(timeInfo)
                                }
                            }
                        }


                        if (isFavorite) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 12.dp, bottom = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .background(Color(0xFF2A2A2A), RoundedCornerShape(50))
                                        .clickable { onAlarmClick() }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Alarm",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (alarmLine != null) "Linia $alarmLine" else "Ustaw alarm",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }


                    }
                }
            }
        }
    }
}

// Pobranie obrazka mapi z Geoapify
fun getStaticMapUrl(lat: Double, lon: Double, width: Float, height: Float): String {
    val apiKey = MAPS_API_KEY
    val zoom = 16.5

    val downloadWidth = width + (width/2).toInt()

    Log.d("KORDY", lat.toString())
    Log.d("KORDY", lon.toString())
    Log.d("LINK", "https://maps.geoapify.com/v1/staticmap?style=dark-matter-yellow-roads&width=${downloadWidth.toInt() * 2}&height=${height.toInt() * 2}&center=lonlat:$lon,${lat+0.0002}&zoom=$zoom&marker=lonlat:$lon,$lat;type:material;color:%2320ce55&apiKey=$apiKey"
    )

        return "https://maps.geoapify.com/v1/staticmap?style=dark-matter-yellow-roads&width=${downloadWidth.toInt() * 2}&height=${height.toInt() * 2}&center=lonlat:$lon,${lat+0.0002}&zoom=$zoom&marker=lonlat:$lon,$lat;type:material;color:%2320ce55&apiKey=$apiKey"
}

// Wiersz w liście odjazdów
@Composable
fun MonitorRow(timeInfo: TimeData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 0.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Numer linii i kierunek
        Row(modifier = Modifier.weight(1f)) {
            Text(
                text = timeInfo.line.padEnd(3, ' '),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Thin,
                fontFamily = FontFamily.SansSerif
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = timeInfo.direction,
                color = Color.White,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Thin,
                fontFamily = FontFamily.SansSerif
            )
        }

        // Czas do odjazdu
        Text(
            text = "${timeInfo.minutes}m",
            color = if (timeInfo.minutes <= 3) DarkAccent else Color.White, // Zielony jeśli blisko
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
        )
    }
}