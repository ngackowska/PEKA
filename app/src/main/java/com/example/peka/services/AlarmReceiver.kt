package com.example.peka.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Wyciągamy dane przekazane przez Schedulera
        val stopCode = intent.getStringExtra("STOP_CODE") ?: return
        val stopName = intent.getStringExtra("STOP_NAME") ?: return
        val line = intent.getStringExtra("LINE") ?: return
        val minutesBefore = intent.getIntExtra("MINUTES_BEFORE", 5)
        val endTime = intent.getStringExtra("END_TIME") ?: "23:59"

        // Tworzymy rozkaz startu/aktualizacji dla naszego Serwisu Nasłuchującego
        val serviceIntent = Intent(context, TransitAlarmService::class.java).apply {
            action = TransitAlarmService.ACTION_START_LISTENING
            putExtra("STOP_CODE", stopCode)
            putExtra("STOP_NAME", stopName)
            putExtra("LINE", line)
            putExtra("END_TIME", endTime)
            putExtra("MINUTES_BEFORE", minutesBefore)
        }

        // Bezpieczne uruchomienie Foreground Service
        ContextCompat.startForegroundService(context, serviceIntent)
    }
}