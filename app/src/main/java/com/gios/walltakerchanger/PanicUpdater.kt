package com.gios.walltakerchanger

import android.app.WallpaperManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.PowerManager
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.FutureTarget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

@Suppress("DEPRECATION")
class PanicUpdater {
    companion object {
        fun pUpdate(context: Context) {

            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WalltakerChanger:WAKEUP")
            wl.acquire(10 * 60 * 1000L /*10 minutes*/)
            val sharedPreferences: SharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context)
            panicHome = sharedPreferences.getString("panicHome", "")!!
            panicLock = sharedPreferences.getString("panicLock", "")!!
            val image =
                "https://i.pinimg.com/originals/dc/b9/03/dcb903fac6f299ad7c85ad9d0e460c7c.jpg"
            if (panicHome.isNotEmpty()) {
                sett(panicHome, "home", context)
            } else {
                sett(image, "home", context)
            }
            if (panicLock.isNotEmpty()) {
                sett(panicLock, "lock", context)
            } else {
                sett(image, "lock", context)
            }
            wl.release()
        }

        private fun sett(image: String, where: String, context: Context) {
            val wallpaperManager = WallpaperManager.getInstance(context)
            lateinit var bitmap: Bitmap
            runBlocking {
                val job = launch {
                    val displayMetrics = DisplayMetrics()
                    val windowsManager =
                        context.getSystemService(AppCompatActivity.WINDOW_SERVICE) as WindowManager
                    windowsManager.defaultDisplay.getMetrics(
                        displayMetrics
                    )

                    val width = displayMetrics.widthPixels
                    val height = displayMetrics.heightPixels

                    val futureTarget: FutureTarget<Bitmap> =
                        Glide.with(context).asBitmap()
                            .load(image)
                            .fitCenter()
                            .submit(width, height)
                    bitmap = withContext(Dispatchers.IO) {
                        futureTarget.get()
                    }
                    Glide.with(context).clear(futureTarget)
                }
                job.join()
                if (where == "home") {
                    wallpaperManager.setBitmap(
                        bitmap,
                        null,
                        true,
                        WallpaperManager.FLAG_SYSTEM
                    )
                }
                if (where == "lock") {
                    wallpaperManager.setBitmap(
                        bitmap,
                        null,
                        true,
                        WallpaperManager.FLAG_LOCK
                    )
                }
            }
        }
    }
}