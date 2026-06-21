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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardColors
import androidx.compose.material3.Icon
import androidx.compose.ui.platform.LocalContext
import com.example.peka.R
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.example.peka.ui.theme.DarkCardBackground
import com.example.peka.ui.theme.DarkText
import com.example.peka.ui.theme.neumorphicShadow

// Komponent wyświetlający informacje o danym odjeździe pojazdu
// (pojedynczy odjazd danej linii widoczny np. po kliknięciu w kafelek na dashboard)

@Composable
fun DepartureCard(timeData: TimeData) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .neumorphicShadow(
                20.dp,
                10.dp
            ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardColors(
            containerColor = DarkCardBackground,
            contentColor = DarkText,
            disabledContainerColor = DarkCardBackground,
            disabledContentColor = DarkText
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp,16.dp,20.dp,0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(0.6f)){
                Text(
                    text = "Linia ${timeData.line}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.Bottom){
                    Text(text = "Kierunek", fontWeight = FontWeight.Light, fontSize = 12.sp,modifier = Modifier.alignByBaseline())
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "${timeData.direction}", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.alignByBaseline())

                }




            }
            Column(Modifier.weight(0.4f), verticalArrangement = Arrangement.Bottom, horizontalAlignment = Alignment.End){
                Text(
                    text = "${timeData.minutes} min",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    textAlign = TextAlign.End
                )

                if (timeData.departure != null) {
                    Row(verticalAlignment = Alignment.Bottom){
                        Text(text = "Odjazd", fontWeight = FontWeight.Light, fontSize = 12.sp,modifier = Modifier.alignByBaseline())
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "${timeData.departure.substringAfter("T").take(5)}", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.alignByBaseline())
                    }
                }
            }

        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp,0.dp, 20.dp,16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column() {

                if (timeData.ticketMachine == true) {
                    Row(){
                    Text(text = "Dostępny biletomat.", fontWeight = FontWeight.Light, fontSize = 12.sp,modifier = Modifier.alignByBaseline())
                    }
                }

                if (timeData.vehicle != null) {
                    Row(verticalAlignment = Alignment.Bottom){
                        Text(text = "Numer pojazdu", fontWeight = FontWeight.Light, fontSize = 12.sp,modifier = Modifier.alignByBaseline())
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "${timeData.vehicle}", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.alignByBaseline())
                    }
                }


            }


            // Tu jakieś ikonki pewnie apbo krótko
            // na razie kilka przykładowych żeby zobaczyć co to (lowEntrance vs lowFloor nie wiem które to xD)
            // do porównania z PEKA

            Row {
                val iconSize = 24.dp

                Spacer(Modifier.height(iconSize).width(1.dp))

                if (timeData.bike == true) {
                    Icon(
                        painter = painterResource(id = R.drawable.bike),
                        contentDescription = "Możliwość przewozu rowerów.",
                        modifier = Modifier
                            .size(iconSize) // Ustawienie rozmiaru obrazka
                            .clickable {
                                // Wyświetlenie dymku z tekstem po kliknięciu
                                Toast.makeText(context, "Możliwość przewozu rowerów.", Toast.LENGTH_SHORT).show()
                            },
                        tint = DarkText
                    )
                }



                if (timeData.airCnd == true) {
                    Icon(
                        painter = painterResource(id = R.drawable.snow),
                        contentDescription = "Pojazd wyposażony w klimatyzację.",
                        modifier = Modifier
                            .size(iconSize) // Ustawienie rozmiaru obrazka
                            .padding(start = 3.dp)
                            .clickable {
                                // Wyświetlenie dymku z tekstem po kliknięciu
                                Toast.makeText(context, "Pojazd wyposażony w klimatyzację.", Toast.LENGTH_SHORT).show()
                            },
                        tint = DarkText
                    )
                }

                if (timeData.charger == true) {
                    Icon(
                        painter = painterResource(id = R.drawable.usb),
                        contentDescription = "Możliwość ładowania gniazdem USB.",
                        modifier = Modifier
                            .size(iconSize) // Ustawienie rozmiaru obrazka
                            .padding(start = 3.dp)
                            .clickable {
                                // Wyświetlenie dymku z tekstem po kliknięciu
                                Toast.makeText(context, "Możliwość ładowania gniazdem USB.", Toast.LENGTH_SHORT).show()
                            },
                        tint = DarkText
                    )
                }

                if (timeData.lowEntranceBus == true) {
                    Icon(
                        painter = painterResource(id = R.drawable.wheel_star),
                        contentDescription = "Kurs obsługiwany pojazdem z niską podłogą w środkowym członie.",
                        modifier = Modifier
                            .size(iconSize) // Ustawienie rozmiaru obrazka
                            .padding(start = 3.dp)
                            .clickable {
                                // Wyświetlenie dymku z tekstem po kliknięciu
                                Toast.makeText(context, "Kurs obsługiwany pojazdem z niską podłogą w środkowym członie.", Toast.LENGTH_SHORT).show()
                            },
                        tint = DarkText
                    )
                }

                if (timeData.lowFloorBus == true) {
                    Icon(
                        painter = painterResource(id = R.drawable.wheel),
                        contentDescription = "Kurs obsługiwany pojazdem niskopodłogowym.",
                        modifier = Modifier
                            .size(iconSize) // Ustawienie rozmiaru obrazka
                            .padding(start = 3.dp)
                            .clickable {
                                // Wyświetlenie dymku z tekstem po kliknięciu
                                Toast.makeText(context, "Kurs obsługiwany pojazdem niskopodłogowym.", Toast.LENGTH_SHORT).show()
                            },
                        tint = DarkText

                    )
                }

                if (timeData.lfRamp == true) {
                    Icon(
                        painter = painterResource(id = R.drawable.wheel_ramp),
                        contentDescription = "Kurs obsługiwany pojazdem niskopodłogowym z rampą.",
                        modifier = Modifier
                            .size(iconSize) // Ustawienie rozmiaru obrazka
                            .padding(start = 3.dp)
                            .clickable {
                                // Wyświetlenie dymku z tekstem po kliknięciu
                                Toast.makeText(context, "Kurs obsługiwany pojazdem niskopodłogowym z rampą.", Toast.LENGTH_SHORT).show()
                            },
                        tint = DarkText
                    )
                }

                if (timeData.leRamp == true) {
                    Icon(
                        painter = painterResource(id = R.drawable.wheel_ramp_star),
                        contentDescription = "Kurs obsługiwany pojazdem z niską podłogą w środkowym członie z rampą.",
                        modifier = Modifier
                            .size(iconSize) // Ustawienie rozmiaru obrazka
                            .padding(start = 3.dp)
                            .clickable {
                                // Wyświetlenie dymku z tekstem po kliknięciu
                                Toast.makeText(context, "Kurs obsługiwany pojazdem z niską podłogą w środkowym członie z rampą.", Toast.LENGTH_SHORT).show()
                            },
                        tint = DarkText
                    )
                }


            }

        }


    }
}

