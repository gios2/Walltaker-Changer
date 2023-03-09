package com.gios.walltakerchanger


import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.PowerManager
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.FutureTarget
import com.github.kittinunf.fuel.httpGet
import com.google.gson.GsonBuilder


@Suppress("DEPRECATION")
class Updater {

    companion object {
        private var lastUrl = ""

        @SuppressLint("WakelockTimeout")
        fun updateWallpaper(context: Context) {

            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager

            wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WalltakerChanger:WAKEUP")
            wl.acquire()
            val sharedPreferences: SharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context)

            linkId = sharedPreferences.getString("Id", "0")
            linkUrl = "https://walltaker.joi.how/api/links/$linkId.json"

            setHome = sharedPreferences.getBoolean("wallpaper", false)
            setLock = sharedPreferences.getBoolean("lockscreen", false)
            linkUrl!!.httpGet().header("User-Agent" to "Walltaker-Changer/$verNr")
                .responseString { _, response, result ->
                    if (response.statusCode == 200) {
                        val gson = GsonBuilder().create()
                        val data = gson.fromJson(result.get(), LinkData::class.java)
                        if (data != null) {
                            id = data.id
                            expires = data.expires
                            username = data.username
                            terms = data.terms
                            blacklist = data.blacklist
                            post_url = data.post_url
                            post_thumbnail_url = data.post_thumbnail_url
                            post_description = data.post_description
                            created_at = data.created_at
                            updated_at = data.updated_at
                            set_by = data.set_by
                            response_type = data.response_type
                            response_text = data.response_text
                            online = data.online

                            if (post_url != null && !post_url.isNullOrEmpty()) {
                                if (post_url != lastUrl) {

                                    val displayMetrics = DisplayMetrics()
                                    val windowsManager =
                                        context.getSystemService(WINDOW_SERVICE) as WindowManager
                                    windowsManager.defaultDisplay.getMetrics(displayMetrics)
                                    val width = displayMetrics.widthPixels
                                    val height = displayMetrics.heightPixels

                                    val futureTarget: FutureTarget<Bitmap> =
                                        Glide.with(context).asBitmap().load(post_url).fitCenter()
                                            .submit(width, height)
                                    val bitmap = futureTarget.get()
                                    Glide.with(context).clear(futureTarget)

                                    val wallpaperManager = WallpaperManager.getInstance(context)

                                    if (setHome) {
                                        println("Setting home")
                                        wallpaperManager.setBitmap(
                                            bitmap, null, true, WallpaperManager.FLAG_SYSTEM
                                        )
                                    }
                                    if (setLock) {
                                        println("Setting lock")
                                        wallpaperManager.setBitmap(
                                            bitmap, null, true, WallpaperManager.FLAG_LOCK
                                        )
                                    }

                                }
                                lastUrl = data.post_url
                            }
                        }
                    } else {
                        println("error on json request: ${response.statusCode}")
                    }
                }
            wl.release()
        }
    }
}