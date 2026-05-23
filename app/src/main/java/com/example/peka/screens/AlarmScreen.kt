package com.example.peka.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.peka.database.AlarmDao
import com.example.peka.database.AlarmEntity
import com.example.peka.viewmodels.DashboardViewModel
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

    LaunchedEffect(stopCode) {
        dashboardViewModel.fetchDeparturesForStop(stopCode)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alarm $stopName") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Wybór linii (Dropdown)
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = if (selectedLine.isEmpty()) "Brak aktywnych linii" else "Linia $selectedLine",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Wybierz linię") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    availableLines.forEach { line ->
                        DropdownMenuItem(
                            text = { Text("Linia $line") },
                            onClick = {
                                selectedLine = line
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Pola czasu (uproszczone jako tekst dla wygody, docelowo TimePicker)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = { Text("Od (GG:MM)") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = endTime,
                    onValueChange = { endTime = it },
                    label = { Text("Do (GG:MM)") },
                    modifier = Modifier.weight(1f)
                )
            }

            // Suwak minut
            Column {
                Text("Ile minut przed odjazdem: ${minutesBefore.toInt()} min", fontSize = 16.sp)
                Slider(
                    value = minutesBefore,
                    onValueChange = { minutesBefore = it },
                    valueRange = 1f..30f,
                    steps = 29
                )
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
                        }
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Zapisz Alarm", fontSize = 16.sp)
            }
        }
    }
}