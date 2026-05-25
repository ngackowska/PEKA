package com.example.peka.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object AlarmScheduler {

    // Generuje unikalne ID dla każdego alarmu (np. "KORN43_7" -> 1837462)
    fun generateAlarmId(stopCode: String, line: String): Int {
        return "${stopCode}_${line}".hashCode()
    }

    fun scheduleAlarm(context: Context, stopCode: String, stopName: String, line: String, startTime: String,
                      minutesBefore: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Przygotowujemy intencję, która wybudzi nasz Receiver
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("STOP_CODE", stopCode)
            putExtra("STOP_NAME", stopName)
            putExtra("LINE", line)
            putExtra("MINUTES_BEFORE", minutesBefore)
        }

        val alarmId = generateAlarmId(stopCode, line)

        // FLAG_UPDATE_CURRENT nadpisze alarm, jeśli użytkownik zmieni jego ustawienia
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Parsowanie czasu "HH:mm" na milisekundy (dla dzisiejszego dnia)
        val timeParts = startTime.split(":")
        val hour = timeParts[0].toInt()
        val minute = timeParts[1].toInt()

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        // Jeśli czas już dzisiaj minął, ustawiamy na jutro
        if (calendar.timeInMillis < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Zlecamy systemowi wybudzenie
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }

    fun cancelAlarm(context: Context, stopCode: String, line: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val alarmId = generateAlarmId(stopCode, line)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Usuwamy zaplanowaną pobudkę z systemu
        alarmManager.cancel(pendingIntent)

        // Wysyłamy do naszego Serwisu informację "Przestań nasłuchiwać tego przystanku"
        val stopIntent = Intent(context, TransitAlarmService::class.java).apply {
            action = "ACTION_STOP_LISTENING"
            putExtra("JOB_ID", alarmId)
        }
        context.startService(stopIntent)
    }

}