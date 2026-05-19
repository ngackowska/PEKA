package com.example.peka.modules


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import com.example.peka.database.BusStop
import com.example.peka.database.FavoriteStopDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun FavoriteButton(
    stop: BusStop,
    dao: FavoriteStopDao // Przekazujesz DAO lub ViewModel
) {
    // Zbieramy stan z bazy. Jeśli baza jest pusta lub ładuje, domyślnie false.
    val isFav by dao.isFavorite(stop.stop_code).collectAsState(initial = false)
    val coroutineScope = rememberCoroutineScope()

    IconButton(
        onClick = {
            // ZMIANA TUTAJ: Wymuszamy uruchomienie w tle (IO)
            coroutineScope.launch(Dispatchers.IO) {
                val entity = BusStop(
                    stop_code = stop.stop_code,
                    stop_name = stop.stop_name,
                    stop_lat = stop.stop_lat,
                    stop_lon = stop.stop_lon,
                    stop_id = stop.stop_id,
                    zone_id = stop.zone_id
                )
                if (isFav) {
                    dao.delete(entity)
                } else {
                    dao.insert(entity)
                }
            }
        }
    ) {
        Icon(
            imageVector = if (isFav) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = "Ulubiony",
            tint = if (isFav) Color.Red else Color.Gray
        )
    }
}