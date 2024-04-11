package com.gios.walltakerchanger

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.preference.PreferenceManager


class BroadcastReceiver : BroadcastReceiver() {
    @SuppressLint("ScheduleExactAlarm")
    override fun onReceive(context: Context, intent: Intent?) {

        Updater.update(context)
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intents =
            Intent(context, com.gios.walltakerchanger.BroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intents,
            PendingIntent.FLAG_IMMUTABLE
        )

        val sharedPreferences: SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(context)
        timeCheckT = sharedPreferences.getString("timeCheck", "10")!!.toInt()
        timeCheckT *= 1000
        if (timeCheckT < 10000) {
            timeCheckT += 10000
        }
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + timeCheckT,
            pendingIntent
        )
    }
}