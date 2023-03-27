
package com.gios.walltakerchanger


import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast


class Service : Service() {
    private var alarmManager: AlarmManager? = null
    private var alarmIntent: PendingIntent? = null
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        Toast.makeText(this, "Walltaker Changer Service Created", Toast.LENGTH_SHORT).show()
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, BroadcastReceiver::class.java)
        alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0)
        setAlarm()
    }
    private fun setAlarm() {
        Log.d(TAG, "setAlarm")
        alarmManager!!.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + 10000,
            alarmIntent
        )
    }

    override fun stopService(name: Intent?): Boolean {
        return super.stopService(name)
    }

    companion object {
        private const val TAG = "AutoService"
    }

    override fun onDestroy() {
        Log.i(TAG, "onCreate() , service stopped...")
        unregisterReceiver(receiver)
    }
}
