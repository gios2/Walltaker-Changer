package com.gios.walltakerchanger


import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder


class Service : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @SuppressLint("ScheduleExactAlarm")
    override fun onCreate() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, BroadcastReceiver::class.java)
        val alarmIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + 10000,
            alarmIntent
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Updater.update(this)
        return super.onStartCommand(intent, flags, startId)
    }


    override fun onDestroy() {
        unregisterReceiver(receiver)
    }
}
