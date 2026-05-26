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
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.ui.platform.LocalContext
import com.example.peka.R
import androidx.compose.ui.res.painterResource

// Komponent wyświetlający informacje o danym odjeździe pojazdu
// (pojedynczy odjazd danej linii widoczny np. po kliknięciu w kafelek na dashboard)

@Composable
fun DepartureCard(timeData: TimeData) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 16.dp, 16.dp, 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Linia: ${timeData.line}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Kierunek: ${timeData.direction}")



            }
            Text(
                text = "${timeData.minutes} min",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 0.dp, 16.dp, 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column() {

                if (timeData.ticketMachine == true) {
                    Text(text = "Dostępny biletomat.", fontSize = 12.sp)
                }

                if (timeData.vehicle != null) {
                    Text(text = "Numer pojazdu: ${timeData.vehicle}", fontSize = 12.sp)
                }

                if (timeData.departure != null) {
                    Text(text = "Odjazd: ${timeData.departure.substringAfter("T").take(5)}", fontSize = 12.sp)
                }
            }

            // Tu jakieś ikonki pewnie apbo krótko
            // na razie kilka przykładowych żeby zobaczyć co to (lowEntrance vs lowFloor nie wiem które to xD)
            // do porównania z PEKA

            Row {
                if (timeData.bike == true) {
                    Image(
                        painter = painterResource(id = R.drawable.bike),
                        contentDescription = "Możliwość przewozu rowerów.",
                        modifier = Modifier
                            .size(32.dp) // Ustawienie rozmiaru obrazka
                            .clickable {
                                // Wyświetlenie dymku z tekstem po kliknięciu
                                Toast.makeText(context, "Możliwość przewozu rowerów.", Toast.LENGTH_SHORT).show()
                            }
                    )
                }



                if (timeData.airCnd == true) {
                    Image(
                        painter = painterResource(id = R.drawable.snow),
                        contentDescription = "Pojazd wyposażony w klimatyzację.",
                        modifier = Modifier
                            .size(32.dp) // Ustawienie rozmiaru obrazka
                            .padding(start = 3.dp)
                            .clickable {
                                // Wyświetlenie dymku z tekstem po kliknięciu
                                Toast.makeText(context, "Pojazd wyposażony w klimatyzację.", Toast.LENGTH_SHORT).show()
                            }
                    )
                }

                if (timeData.charger == true) {
                    Image(
                        painter = painterResource(id = R.drawable.usb),
                        contentDescription = "Możliwość ładowania gniazdem USB.",
                        modifier = Modifier
                            .size(32.dp) // Ustawienie rozmiaru obrazka
                            .padding(start = 3.dp)
                            .clickable {
                                // Wyświetlenie dymku z tekstem po kliknięciu
                                Toast.makeText(context, "Możliwość ładowania gniazdem USB.", Toast.LENGTH_SHORT).show()
                            }
                    )
                }

                if (timeData.lowEntranceBus == true) {
                    Image(
                        painter = painterResource(id = R.drawable.wheel_star),
                        contentDescription = "Kurs obsługiwany pojazdem z niską podłogą w środkowym członie.",
                        modifier = Modifier
                            .size(32.dp) // Ustawienie rozmiaru obrazka
                            .padding(start = 3.dp)
                            .clickable {
                                // Wyświetlenie dymku z tekstem po kliknięciu
                                Toast.makeText(context, "Kurs obsługiwany pojazdem z niską podłogą w środkowym członie.", Toast.LENGTH_SHORT).show()
                            }
                    )
                }

                if (timeData.lowFloorBus == true) {
                    Image(
                        painter = painterResource(id = R.drawable.wheel),
                        contentDescription = "Kurs obsługiwany pojazdem niskopodłogowym.",
                        modifier = Modifier
                            .size(32.dp) // Ustawienie rozmiaru obrazka
                            .padding(start = 3.dp)
                            .clickable {
                                // Wyświetlenie dymku z tekstem po kliknięciu
                                Toast.makeText(context, "Kurs obsługiwany pojazdem niskopodłogowym.", Toast.LENGTH_SHORT).show()
                            }
                    )
                }

                if (timeData.lfRamp == true) {
                    Image(
                        painter = painterResource(id = R.drawable.wheel_ramp),
                        contentDescription = "Kurs obsługiwany pojazdem niskopodłogowym z rampą.",
                        modifier = Modifier
                            .size(32.dp) // Ustawienie rozmiaru obrazka
                            .padding(start = 3.dp)
                            .clickable {
                                // Wyświetlenie dymku z tekstem po kliknięciu
                                Toast.makeText(context, "Kurs obsługiwany pojazdem niskopodłogowym z rampą.", Toast.LENGTH_SHORT).show()
                            }
                    )
                }

                if (timeData.leRamp == true) {
                    Image(
                        painter = painterResource(id = R.drawable.wheel_ramp_star),
                        contentDescription = "Kurs obsługiwany pojazdem z niską podłogą w środkowym członie z rampą.",
                        modifier = Modifier
                            .size(32.dp) // Ustawienie rozmiaru obrazka
                            .padding(start = 3.dp)
                            .clickable {
                                // Wyświetlenie dymku z tekstem po kliknięciu
                                Toast.makeText(context, "Kurs obsługiwany pojazdem z niską podłogą w środkowym członie z rampą.", Toast.LENGTH_SHORT).show()
                            }
                    )
                }


            }

        }


    }
}

