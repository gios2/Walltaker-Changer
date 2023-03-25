package com.gios.walltakerchanger

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.gios.walltakerchanger.Updater.Companion.updateWallpaper
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate


class Service : Service() {
    private var timer: Timer? = null
    private var task: TimerTask? = null
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        Toast.makeText(this, "Walltaker Changer Service Created", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "onCreate")
        timer = Timer()
        timer!!.scheduleAtFixedRate(delay = 0, period = 10000L) {
            updateWallpaper(this@Service)
        }
    }


    override fun stopService(name: Intent?): Boolean {
        timer!!.cancel()
        task!!.cancel()
        return super.stopService(name)
    }

    companion object {
        private const val TAG = "AutoService"
    }

    override fun onDestroy() {
        timer!!.cancel()
        task!!.cancel()
        Log.i(TAG, "onCreate() , service stopped...")
    }
}
