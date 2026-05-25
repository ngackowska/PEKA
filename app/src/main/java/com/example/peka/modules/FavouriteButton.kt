package com.example.peka.modules


import android.util.Log
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
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

                // Pobieramy instancje Firebase
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                val db = FirebaseFirestore.getInstance()

                if (isFav) {
                    dao.delete(entity)

                    if (uid != null) {
                        db.collection("users").document(uid)
                            .update("favorite_stops", FieldValue.arrayRemove(stop.stop_code))

                        db.collection("users").document(uid)
                            .update("alarms.${stop.stop_code}", FieldValue.delete())

                    }
                } else {
                    dao.insert(entity)
                    Log.d("ULUBIONE", "DODAJE")

                    if (uid != null) {
                        Log.d("ULUBIONE", "DODAJE BAZA")
                        db.collection("users").document(uid)
                            .update("favorite_stops", FieldValue.arrayUnion(stop.stop_code))
                    }
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