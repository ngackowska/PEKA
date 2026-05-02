package com.example.peka.modules

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
import com.example.peka.api.TimeData

// Komponent wyświetlający informacje o danym odjeździe pojazdu
// (pojedynczy odjazd danej linii widoczny np. po kliknięciu w kafelek na dashboard)

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

                // Tu jakieś ikonki pewnie apbo krótko
                // na razie kilka przykładowych żeby zobaczyć co to (lowEntrance vs lowFloor nie wiem które to xD)
                // do porównania z PEKA

                if (timeData.airCnd == true) {
                    Text(text = "Klimatyzacja: Tak", fontSize = 12.sp)
                }

                if (timeData.bike == true) {
                    Text(text = "Rower: Tak", fontSize = 12.sp)
                }

                if (timeData.lowEntranceBus == true) {
                    Text(text = "lowEntranceBus: Tak", fontSize = 12.sp)
                }

                if (timeData.lowFloorBus == true) {
                    Text(text = "lowFloorBus: Tak", fontSize = 12.sp)
                }

                if (timeData.ticketMachine == true) {
                    Text(text = "ticketMachine: Tak", fontSize = 12.sp)
                }


            }
            Text(
                text = "${timeData.minutes} min",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp
            )
        }
    }
}

