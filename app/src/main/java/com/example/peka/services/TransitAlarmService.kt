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

            val jobId = AlarmScheduler.generateAlarmId(stopCode, line)

            // Jeśli już nasłuchujemy tej konkretnej linii na tym przystanku, ignorujemy
            if (!activeJobs.containsKey(jobId)) {
                // Odpalamy nowy stoper dla tego alarmu
                val job = serviceScope.launch {
                    startPollingForStop(jobId, stopCode, stopName, line, minutesBefore)
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
        minutesBefore: Int // <-- Dodany parametr czasu z suwaka
    ) {
        try {
            while (isActive) {
                try {
                    // 1. Przygotowujemy zapytanie do API PEKA (tak samo jak w ViewModel)
                    val p0Json = "{\"symbol\":\"$stopCode\"}"

                    // Odpytujemy API (to zadziała, bo jesteśmy w CoroutineScope(Dispatchers.IO))
                    val response = com.example.peka.api.pekaApiService.getTimes(p0 = p0Json)
                    val departures = response.success.times

                    // 2. Szukamy pierwszego (najbliższego) odjazdu dla naszej wybranej linii
                    val targetDeparture = departures.firstOrNull { it.line == line }

                    // 3. Sprawdzamy warunek alarmu
                    if (targetDeparture != null) {
                        if (targetDeparture.minutes <= minutesBefore) {
                            // STRZAŁ! Tramwaj nadjeżdża w określonym czasie.
                            sendLoudNotification(stopName, line, targetDeparture.minutes, jobId)

                            // Przerwanie pętli - robota dla tego alarmu jest skończona
                            break
                        }
                    }
                } catch (e: Exception) {
                    // Chwilowy brak internetu lub błąd serwera PEKA.
                    // Ignorujemy błąd, pętla pójdzie dalej i spróbuje znów za minutę.
                    e.printStackTrace()
                }

                // 4. Odczekujemy 60 sekund przed kolejnym zapytaniem (oszczędzanie baterii i serwerów API)
                delay(60_000L)
            }
        } finally {
            // Sprzątanie po zakończeniu zadania (zawsze się wykona, nawet po 'break')
            activeJobs.remove(jobId)
            checkIfSelfDestructNeeded()
        }
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