package com.example.peka.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive

class TransitAlarmService : Service() {

    companion object {
        const val ACTION_START_LISTENING = "ACTION_START_LISTENING"
        const val CHANNEL_ID = "transit_silent_channel"
        const val FOREGROUND_ID = 101
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Klucz to nasz unikalny Hash (stopCode_line), Wartość to zadanie w tle (Job)
    private val activeJobs = ConcurrentHashMap<Int, Job>()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        // Startujemy usługę z cichym powiadomieniem (Wymóg systemu)
        startForeground(FOREGROUND_ID, createSilentNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_START_LISTENING) {
            val stopCode = intent.getStringExtra("STOP_CODE") ?: return START_NOT_STICKY
            val stopName = intent.getStringExtra("STOP_NAME") ?: return START_NOT_STICKY
            val line = intent.getStringExtra("LINE") ?: return START_NOT_STICKY

            val minutesBefore = intent.getIntExtra("MINUTES_BEFORE", 5)
            val endTime = intent.getStringExtra("END_TIME") ?: "23:59"

            val jobId = AlarmScheduler.generateAlarmId(stopCode, line)

            // Jeśli już nasłuchujemy tej konkretnej linii na tym przystanku, ignorujemy
            if (!activeJobs.containsKey(jobId)) {
                // Odpalamy nowy stoper dla tego alarmu
                val job = serviceScope.launch {
                    startPollingForStop(jobId, stopCode, stopName, line, minutesBefore, endTime)
                }
                activeJobs[jobId] = job
            }
        }

        // Wewnątrz onStartCommand w TransitAlarmService:
        if (intent?.action == "ACTION_STOP_LISTENING") {
            val jobId = intent.getIntExtra("JOB_ID", -1)
            if (jobId != -1) {
                // Zatrzymujemy timer dla tego alarmu
                activeJobs[jobId]?.cancel()
                activeJobs.remove(jobId)
                checkIfSelfDestructNeeded()
            }
        }

        return START_NOT_STICKY
    }

    private suspend fun CoroutineScope.startPollingForStop(
        jobId: Int,
        stopCode: String,
        stopName: String,
        line: String,
        minutesBefore: Int,
        endTime: String
    ) {
        // Zbiór, który zapamięta godziny odjazdów (np. "07:15"), o których już wysłaliśmy powiadomienie
        val notifiedDepartures = mutableSetOf<String>()

        try {
            while (isActive) {
                // 1. SPRAWDZAMY, CZY OKIENKO CZASOWE SIĘ SKOŃCZYŁO
                if (isPastEndTime(endTime)) {
                    break // Zegar minął endTime (np. jest już 09:01), przerywamy nasłuchiwanie!
                }

                try {
                    val p0Json = "{\"symbol\":\"$stopCode\"}"
                    val response = com.example.peka.api.pekaApiService.getTimes(p0 = p0Json)
                    val departures = response.success.times

                    // 2. Szukamy WSZYSTKICH nadjeżdżających tramwajów wybranej linii
                    val upcomingTrams = departures.filter { it.line == line }

                    for (tram in upcomingTrams) {
                        // Jeśli tramwaj jest za X minut, a my JESZCZE O NIM NIE MÓWILIŚMY
                        if (tram.minutes <= minutesBefore && !notifiedDepartures.contains(tram.departure)) {

                            sendLoudNotification(stopName, line, tram.minutes, jobId)

                            // Dodajemy jego unikalny czas odjazdu do "pamięci", by nie powiadomić o nim za minutę
                            notifiedDepartures.add(tram.departure)
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Czekamy 60 sekund
                delay(60_000L)
            }
        } finally {
            activeJobs.remove(jobId)
            checkIfSelfDestructNeeded()
        }
    }

    // Funkcja pomocnicza: Sprawdza, czy bieżący czas na telefonie jest późniejszy niż 'endTime'
    private fun isPastEndTime(endTime: String): Boolean {
        val parts = endTime.split(":")
        if (parts.size != 2) return false

        val endHour = parts[0].toIntOrNull() ?: return false
        val endMinute = parts[1].toIntOrNull() ?: return false

        val now = java.util.Calendar.getInstance()
        val currentHour = now.get(java.util.Calendar.HOUR_OF_DAY)
        val currentMinute = now.get(java.util.Calendar.MINUTE)

        return currentHour > endHour || (currentHour == endHour && currentMinute >= endMinute)
    }

    private fun checkIfSelfDestructNeeded() {
        if (activeJobs.isEmpty()) {
            // Zaden alarm już nie nasłuchuje. Zabijamy serwis.
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun sendLoudNotification(stopName: String, line: String, minutes: Int, jobId: Int) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val loudChannelId = "transit_loud_channel"

        // Tworzymy kanał o wysokim priorytecie (wymagane od Androida 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                loudChannelId,
                "Alarmy Odjazdów",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Powiadomienia o zbliżających się odjazdach"
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, loudChannelId)
            .setContentTitle("Czas wyjść!")
            .setContentText("Linia $line z $stopName odjeżdża za $minutes minut.")
            .setSmallIcon(android.R.drawable.ic_dialog_alert) // Tu podepnij swoją ikonę dzwoneczka
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) // Zniknie po kliknięciu
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Użyje domyślnego dźwięku telefonu
            .build()

        manager.notify(jobId, notification)
    }

    private fun createSilentNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("Peka Monitor")
        .setContentText("Nasłuchuję odjazdów w tle...")
        .setSmallIcon(android.R.drawable.ic_dialog_info) // Zmień na swoją ikonkę
        .setPriority(NotificationCompat.PRIORITY_MIN)
        .build()

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Ciche śledzenie odjazdów",
                NotificationManager.IMPORTANCE_MIN
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel() // Ubijamy wszystkie stopery, jeśli system nas zniszczy
    }
}