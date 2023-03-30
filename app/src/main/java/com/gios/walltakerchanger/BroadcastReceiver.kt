package com.gios.walltakerchanger

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class BroadcastReceiver : BroadcastReceiver() {
    @Suppress("DEPRECATION")
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

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + 10000,
            pendingIntent
        )
    }
}