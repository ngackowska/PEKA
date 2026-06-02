package com.example.peka.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.peka.R
import com.example.peka.database.AlarmDao
import com.example.peka.database.AlarmEntity
import com.example.peka.services.AlarmScheduler
import com.example.peka.ui.theme.DarkAccent
import com.example.peka.ui.theme.DarkBackground
import com.example.peka.ui.theme.DarkCardBackground
import com.example.peka.ui.theme.DarkHeaderText
import com.example.peka.ui.theme.DarkSelectedAccent
import com.example.peka.ui.theme.DarkText
import com.example.peka.ui.theme.TransparentDarkCardBackground
import com.example.peka.ui.theme.neumorphicShadow
import com.example.peka.viewmodels.DashboardViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen(
    navController: NavController,
    stopCode: String,
    stopName: String,
    alarmDao: AlarmDao,
    dashboardViewModel: DashboardViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()

    // Pobieramy odjazdy, aby wyciągnąć z nich dostępne numery linii
    val departuresMap by dashboardViewModel.departuresMap.collectAsState()
    val departures = departuresMap[stopCode] ?: emptyList()
    val availableLines = departures.map { it.line }.distinct().sorted()

    // Stany formularza
    var selectedLine by remember { mutableStateOf(availableLines.firstOrNull() ?: "") }
    var expanded by remember { mutableStateOf(false) }
    var startTime by remember { mutableStateOf("07:00") }
    var endTime by remember { mutableStateOf("09:00") }
    var minutesBefore by remember { mutableStateOf(5f) }

    val context = LocalContext.current

    LaunchedEffect(stopCode) {
        dashboardViewModel.fetchDeparturesForStop(stopCode)
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {

        }
    ) { padding ->
        Box(modifier = Modifier
            .padding(padding)
            .fillMaxSize()){
            Column(
                modifier = Modifier
//            .fillMaxSize()
                    .padding(20.dp)
                    .background(DarkBackground)
                ,
//        verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp, 0.dp)
                ){
                    Column(){
                        Spacer(modifier = Modifier.height(6.dp))

                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = DarkText,
                            modifier = Modifier
                                .clickable(
                                    onClick = { navController.popBackStack() },
                                    enabled = true
                                )
                                .padding(8.dp, 12.dp)
                                .width(24.dp)
                        )


                    }


                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,

                        ){
                        Row(){
                            Text(
                                text = "Alarm",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Thin,
                                modifier = Modifier.padding(top = 16.dp),
                                color = DarkText
                            )
                            Text(
                                text = " $stopName",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 16.dp),
                                color = DarkText
                            )

                        }


                    }



                }
                Column(){
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = "Wybierz linie",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 0.dp, horizontal = 20.dp),
                        color = DarkHeaderText,

                        )
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        TextField(
                            value = if (selectedLine.isEmpty()) "Brak aktywnych linii" else "Linia $selectedLine",
                            onValueChange = {},
                            readOnly = true,
                            singleLine = true,
                            leadingIcon = { Icon(painterResource(R.drawable.bus), modifier = Modifier.padding(start=5.dp), contentDescription = "Szukaj", tint = DarkText) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(top = 7.dp, bottom = 17.dp, start = 0.dp, end = 0.dp)
                                .neumorphicShadow(
                                    cornerRadius = 20.dp,
                                    shadowRadius = 10.dp
                                )
                            ,
                            shape = RoundedCornerShape(50.dp),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                unfocusedContainerColor = DarkCardBackground,
                                focusedContainerColor =  DarkCardBackground,
                                unfocusedTextColor = DarkText,
                                focusedTextColor = DarkText
                            ),
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 14.sp, // Twój nowy, większy (lub mniejszy) rozmiar tekstu
                                lineHeight = 10.sp
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            containerColor = DarkCardBackground,
                            shape = RoundedCornerShape(20.dp),
                            shadowElevation = 10.dp,

                        ) {
                            availableLines.forEach { line ->
                                DropdownMenuItem(
                                    text = { Text("Linia $line", modifier = Modifier.padding(horizontal = 10.dp)) },
                                    onClick = {
                                        selectedLine = line
                                        expanded = false
                                    }
                                )
                            }
                        }



                    }

                }


                Spacer(Modifier.height(20.dp))


                Text(
                    text = "Czas informowania",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp),
                    color = DarkText
                )


            // Wybór linii (Dropdown)


            // Pola czasu (uproszczone jako tekst dla wygody, docelowo TimePicker)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Od (GG:MM)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 0.dp, horizontal = 20.dp),
                        color = DarkHeaderText,

                        )
                    TextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        singleLine = true,
                        modifier = Modifier

                            .padding(top = 7.dp, bottom = 17.dp, start = 0.dp, end = 10.dp)
                            .neumorphicShadow(
                                cornerRadius = 20.dp,
                                shadowRadius = 10.dp
                            ),
                        shape = RoundedCornerShape(50.dp),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            unfocusedContainerColor = DarkCardBackground,
                            focusedContainerColor = DarkCardBackground,
                            unfocusedTextColor = DarkText,
                            focusedTextColor = DarkText
                        ),
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 14.sp, // Twój nowy, większy (lub mniejszy) rozmiar tekstu
                            lineHeight = 10.sp
                        )
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Do (GG:MM)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 0.dp, horizontal = 20.dp),
                        color = DarkHeaderText,

                        )
                        TextField(
                            value = endTime,
                            onValueChange = { endTime = it },
                            singleLine = true,
                            modifier = Modifier

                                .padding(top = 7.dp, bottom = 17.dp, start = 10.dp, end = 0.dp)
                                .neumorphicShadow(
                                    cornerRadius = 20.dp,
                                    shadowRadius = 10.dp
                                ),
                            shape = RoundedCornerShape(50.dp),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                unfocusedContainerColor = DarkCardBackground,
                                focusedContainerColor = DarkCardBackground,
                                unfocusedTextColor = DarkText,
                                focusedTextColor = DarkText
                            ),
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 14.sp, // Twój nowy, większy (lub mniejszy) rozmiar tekstu
                                lineHeight = 10.sp
                            )
                        )
                    }

            }

            Spacer(Modifier.height(20.dp))

            // Suwak minut
            Column {
                Row(){
                    Text(
                        text = "Ile minut przed odjazdem ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 20.dp).alignByBaseline(),
                        color = DarkHeaderText,

                        )
                    Text(
                        text = " ${minutesBefore.toInt()} min",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 20.dp).alignByBaseline(),
                        color = DarkHeaderText,

                        )

                }

                Card(
                    shape = RoundedCornerShape(26.dp),
                    modifier = Modifier
                        .padding(top=7.dp)
                        .neumorphicShadow(26.dp,12.dp),

                    colors = CardColors(
                        containerColor = DarkCardBackground,
                        contentColor = DarkText,
                        disabledContainerColor = DarkCardBackground,
                        disabledContentColor = DarkText
                    )
                ){
                    Slider(
                        value = minutesBefore,
                        onValueChange = { minutesBefore = it },
                        valueRange = 1f..30f,
                        steps = 29,
                        modifier = Modifier.padding(10.dp, 4.dp),
                        colors = SliderColors(
                            thumbColor = DarkAccent,
                            activeTrackColor = TransparentDarkCardBackground,
                            activeTickColor = DarkAccent,
                            inactiveTrackColor = TransparentDarkCardBackground,
                            inactiveTickColor = DarkHeaderText,
                            disabledThumbColor = DarkAccent,
                            disabledActiveTrackColor = TransparentDarkCardBackground,
                            disabledActiveTickColor = DarkAccent,
                            disabledInactiveTrackColor = TransparentDarkCardBackground,
                            disabledInactiveTickColor = DarkHeaderText
                        ),
                        thumb = {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)  // Szerokość
                                    .height(30.dp) // <-- TUTAJ ZMIENIASZ WYSOKOŚĆ
                                    .background(
                                        color = DarkAccent,
                                        shape = RoundedCornerShape(6.dp) // Zaokrąglone rogi (pigułka)
                                    )
                            )
                        }
                    )

                }

            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (selectedLine.isNotEmpty()) {
                        coroutineScope.launch(Dispatchers.IO) {
                            alarmDao.insertAlarm(
                                AlarmEntity(
                                    stop_code = stopCode,
                                    line = selectedLine,
                                    startTime = startTime,
                                    endTime = endTime,
                                    minutesBefore = minutesBefore.toInt()
                                )
                            )


                            // 2. Zapis do chmury Firebase Firestore
                            val uid = FirebaseAuth.getInstance().currentUser?.uid
                            if (uid != null) {
                                val db = FirebaseFirestore.getInstance()

                                // Przygotowujemy paczkę danych dla tego jednego alarmu
                                val alarmData = hashMapOf(
                                    "line" to selectedLine,
                                    "startTime" to startTime,
                                    "endTime" to endTime,
                                    "minutesBefore" to minutesBefore.toInt()
                                )

                                // Używamy notacji "alarms.KOD", aby stworzyć lub zaktualizować tylko ten jeden przystanek
                                db.collection("users").document(uid)
                                    .update("alarms.$stopCode", alarmData)

                            }

                        }

//
//                        ALARM
//                         Ustawiamy systemowy wyzwalacz!
                        AlarmScheduler.scheduleAlarm(
                            context = context, // Pobierz context z LocalContext.current na górze ekranu
                            stopCode = stopCode,
                            stopName = stopName,
                            line = selectedLine,
                            startTime = startTime,
                            endTime = endTime,
                            minutesBefore = minutesBefore.toInt()
                        )



                        navController.popBackStack()
                    }
                },
                modifier = Modifier.height(50.dp).neumorphicShadow(20.dp,10.dp, darkShadowColor = DarkSelectedAccent, lightShadowColor = DarkAccent),
                colors = ButtonColors(
                    containerColor = DarkAccent,
                    contentColor = DarkBackground,
                    disabledContainerColor = DarkAccent,
                    disabledContentColor = DarkBackground
                )
            ) {
                Text("Ustaw Alarm", fontSize = 16.sp,modifier =  Modifier.padding(12.dp,6.dp))
            }
                Spacer(modifier = Modifier.weight(0.5f))
        }
    }}
}