package com.gios.walltakerchanger

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.WallpaperManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.FutureTarget
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.httpGet
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File


@Suppress("DEPRECATION")
class Updater {
    companion object {
        private var lastUrl = ""
        private var lastUrlHome = ""
        private var lastUrlLock = ""
        fun update(context: Context) {

            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WalltakerChanger:WAKEUP")
            wl.acquire(10 * 60 * 1000L /*10 minutes*/)
            val sharedPreferences: SharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context)
            multiMode = sharedPreferences.getBoolean("multimode", false)
            livS = sharedPreferences.getBoolean("liveS", false)
            livM = sharedPreferences.getBoolean("liveM", false)
            iFit = sharedPreferences.getBoolean("iFit", false)
            iFitH = sharedPreferences.getBoolean("iFitH", false)
            iFitL = sharedPreferences.getBoolean("iFitL", false)
            iFitLive = sharedPreferences.getBoolean("iFitLive", false)
            notifi = sharedPreferences.getBoolean("notifi", false)

            if (livS) {
                linkId = sharedPreferences.getString("id", "0")
                linkUrl = "https://walltaker.joi.how/api/links/$linkId.json"
                linkUrl!!.httpGet().header("User-Agent" to "Walltaker-Changer/")
                    .responseString { _, response, result ->
                        if (response.statusCode == 200) {
                            val gson = GsonBuilder().create()
                            val data = gson.fromJson(result.get(), LinkData::class.java)
                            if (data != null) {
                                post_url = data.post_url
                                if (!post_url.isNullOrEmpty() && liveUrl != post_url) {
                                    live_set_by = data.set_by
                                    if (notifi) {
                                        notifier(context)
                                    }
                                    if (!post_url.isNullOrEmpty()) {
                                        if (sharedPreferences.getBoolean("download", false)) {
                                            downloadFile(
                                                post_url
                                            )
                                        }
                                    }
                                    new = true
                                    liveUrl = data.post_url
                                }
                            }
                        } else {
                            println("error on json request: ${response.statusCode}")
                            println(response)
                        }
                    }
                wl.release()

            } else if (multiMode) {
                linkIdHome = sharedPreferences.getString("IdHome", "0")
                linkUrlHome = "https://walltaker.joi.how/api/links/$linkIdHome.json"
                linkUrlHome!!.httpGet().header("User-Agent" to "Walltaker-Changer/")
                    .responseString { _, response, result ->
                        if (response.statusCode == 200) {
                            val gson = GsonBuilder().create()
                            val data = gson.fromJson(result.get(), LinkData::class.java)
                            if (livM) {
                                if (data != null) {
                                    post_url = data.post_url
                                    live_set_by = data.set_by

                                    if (liveUrl != post_url) {
                                        if (!post_url.isNullOrEmpty()) {
                                            if (notifi && !live_set_by.isNullOrEmpty()) {
                                                notifier(context)
                                            }
                                            if (sharedPreferences.getBoolean("download", false)) {
                                                downloadFile(
                                                    post_url
                                                )
                                            }
                                        }
                                        new = true
                                        liveUrl = data.post_url

                                    }
                                }
                            } else {
                                if (data != null) {
                                    post_url_home = data.post_url
                                    set_by_home = data.set_by

                                    if (!post_url_home.isNullOrEmpty()) {
                                        if (post_url_home != lastUrlHome) {
                                            if (notifi) {
                                                notifier(context)
                                            }
                                            lateinit var bitmap: Bitmap
                                            val wallpaperManager =
                                                WallpaperManager.getInstance(context)
                                            runBlocking {
                                                val job = launch {
                                                    val displayMetrics = DisplayMetrics()
                                                    val windowsManager =
                                                        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                                                    windowsManager.defaultDisplay.getMetrics(
                                                        displayMetrics
                                                    )
                                                    val width = displayMetrics.widthPixels
                                                    val height = displayMetrics.heightPixels

                                                    val futureTarget: FutureTarget<Bitmap> =
                                                        Glide.with(context)
                                                            .asBitmap()
                                                            .load(post_url_home)
                                                            .fitCenter()
                                                            .submit(width, height)
                                                    bitmap = withContext(Dispatchers.IO) {
                                                        futureTarget.get()
                                                    }
                                                    if (iFitH) {
                                                        bitmap =
                                                            bitmapResizer(
                                                                bitmap,
                                                                height,
                                                                width,
                                                            )
                                                    } else {
                                                        returnBitmap(
                                                            bitmap,
                                                            width,
                                                            height
                                                        )
                                                    }
                                                    Glide.with(context).clear(futureTarget)
                                                }
                                                job.join()
                                                try {
                                                    println("Setting home")
                                                    wallpaperManager.setBitmap(
                                                        bitmap,
                                                        null,
                                                        true,
                                                        WallpaperManager.FLAG_SYSTEM
                                                    )
                                                } catch (e: Exception) {
                                                    lastUrlHome = ""
                                                }
                                            }
                                        }

                                        if (sharedPreferences.getBoolean("download1", false)) {
                                            downloadFile(
                                                post_url_home
                                            )
                                        }
                                    }
                                    lastUrlHome = data.post_url
                                }
                            }
                        } else {
                            println("error on home json request: ${response.statusCode}")
                            println(response)
                        }
                        wl.release()
                    }
                linkIdLock = sharedPreferences.getString("IdLock", "0")
                linkUrlLock = "https://walltaker.joi.how/api/links/$linkIdLock.json"
                linkUrlLock!!.httpGet().header("User-Agent" to "Walltaker-Changer/")
                    .responseString { _, response2, result2 ->
                        if (response2.statusCode == 200) {
                            val gson = GsonBuilder().create()
                            val data = gson.fromJson(result2.get(), LinkData::class.java)
                            if (data != null) {
                                post_url_lock = data.post_url
                                set_by_lock = data.set_by

                                if (!post_url_lock.isNullOrEmpty()) {
                                    if (post_url_lock != lastUrlLock) {
                                        if (notifi) {
                                            notifier(context)
                                        }
                                        lateinit var bitmap: Bitmap
                                        val wallpaperManager = WallpaperManager.getInstance(context)
                                        runBlocking {
                                            val job = launch {
                                                val displayMetrics = DisplayMetrics()
                                                val windowsManager =
                                                    context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                                                windowsManager.defaultDisplay.getMetrics(
                                                    displayMetrics
                                                )
                                                val width = displayMetrics.widthPixels
                                                val height = displayMetrics.heightPixels

                                                val futureTarget: FutureTarget<Bitmap> =
                                                    Glide.with(context)
                                                        .asBitmap()
                                                        .load(post_url_lock)
                                                        .fitCenter()
                                                        .submit(width, height)
                                                bitmap = withContext(Dispatchers.IO) {
                                                    futureTarget.get()
                                                }
                                                bitmap = if (iFitL) {
                                                    bitmapResizer(
                                                        bitmap,
                                                        height,
                                                        width,
                                                    )
                                                } else {
                                                    returnBitmap(
                                                        bitmap,
                                                        width,
                                                        height
                                                    )
                                                }
                                                Glide.with(context).clear(futureTarget)
                                            }
                                            job.join()
                                            try {
                                                println("Setting lock")
                                                wallpaperManager.setBitmap(
                                                    bitmap,
                                                    null,
                                                    true,
                                                    WallpaperManager.FLAG_LOCK
                                                )

                                            } catch (e: Exception) {
                                                lastUrlLock = ""
                                            }
                                        }

                                        if (sharedPreferences.getBoolean("download2", false)) {
                                            downloadFile(
                                                post_url_lock
                                            )
                                        }
                                    }
                                    lastUrlLock = data.post_url
                                }


                            }
                        }

                    }

            } else {
                linkId = sharedPreferences.getString("id", "0")
                linkUrl = "https://walltaker.joi.how/api/links/$linkId.json"

                setHome = sharedPreferences.getBoolean("wallpaper", false)
                setLock = sharedPreferences.getBoolean("lockscreen", false)
                linkUrl!!.httpGet().header("User-Agent" to "Walltaker-Changer/")
                    .responseString { _, response, result ->
                        if (response.statusCode == 200) {
                            val gson = GsonBuilder().create()
                            val data = gson.fromJson(result.get(), LinkData::class.java)
                            if (data != null) {
                                post_url = data.post_url
                                set_by = data.set_by

                                if (!post_url.isNullOrEmpty()) {
                                    if (post_url != lastUrl) {
                                        if (notifi) {
                                            notifier(context)
                                        }
                                        lateinit var bitmap: Bitmap
                                        val wallpaperManager =
                                            WallpaperManager.getInstance(context)
                                        runBlocking {
                                            val job = launch {
                                                val displayMetrics = DisplayMetrics()
                                                val windowsManager =
                                                    context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                                                windowsManager.defaultDisplay.getMetrics(
                                                    displayMetrics
                                                )
                                                val width = displayMetrics.widthPixels
                                                val height = displayMetrics.heightPixels

                                                val futureTarget: FutureTarget<Bitmap> =
                                                    Glide.with(context)
                                                        .asBitmap()
                                                        .load(post_url)
                                                        .submit(
                                                            width,
                                                            height
                                                        )

                                                bitmap = withContext(Dispatchers.IO) {
                                                    futureTarget.get()
                                                }
                                                bitmap = if (iFit) {
                                                    bitmapResizer(
                                                        bitmap,
                                                        height,
                                                        width,
                                                    )
                                                } else {
                                                    returnBitmap(
                                                        bitmap,
                                                        width,
                                                        height
                                                    )
                                                }

                                            }

                                            job.join()

                                            try {
                                                if (setHome) {
                                                    println("Setting home")
                                                    wallpaperManager.setBitmap(
                                                        bitmap,
                                                        null,
                                                        true,
                                                        WallpaperManager.FLAG_SYSTEM
                                                    )
                                                }
                                                if (setLock) {
                                                    println("Setting lock")
                                                    wallpaperManager.setBitmap(
                                                        bitmap,
                                                        null,
                                                        true,
                                                        WallpaperManager.FLAG_LOCK
                                                    )
                                                }
                                            } catch (e: Exception) {
                                                lastUrl = ""
                                            }

                                        }
                                        if (sharedPreferences.getBoolean("download", false)) {
                                            downloadFile(
                                                post_url
                                            )
                                        }
                                    }
                                    lastUrl = data.post_url
                                }
                            }
                        } else {
                            println("error on json request: ${response.statusCode}")
                            println(response)
                        }
                        wl.release()
                    }
            }
        }

        private fun notifier(context: Context) {
            val notificationManager =
                context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            val id = "walltaker_changer"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    id,
                    "Walltaker Changer notification",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationManager.createNotificationChannel(channel)
            }

            val builder: NotificationCompat.Builder =
                NotificationCompat.Builder(context, id)
                    .setSmallIcon(R.mipmap.ic_launcher_foreground)
                    .setContentTitle("Setting Wallpaper")
                    .setContentText(
                        ((if (multiMode) {
                            if (livM) {
                                if (live_set_by.isNullOrEmpty() && !set_by_lock.isNullOrEmpty()) {
                                    "Live wallpaper set by an anonymous user | Lockscreen set by $set_by_lock"
                                } else if (!live_set_by.isNullOrEmpty() && set_by_lock.isNullOrEmpty()) {
                                    "Live wallpaper set by $live_set_by | Lockscreen set by an anonymous user"
                                } else if (live_set_by.isNullOrEmpty() && set_by_lock.isNullOrEmpty()) {
                                    "Live wallpaper set by an anonymous user | Lockscreen set by an anonymous user"
                                } else {
                                    "Live wallpaper set by $live_set_by | Lockscreen set by $set_by_lock"
                                }
                            } else {
                                if (set_by_home.isNullOrEmpty() && !set_by_lock.isNullOrEmpty()) {
                                    "Live wallpaper set by an anonymous user | Lockscreen set by $set_by_lock"
                                } else if (!set_by_home.isNullOrEmpty() && set_by_lock.isNullOrEmpty()) {
                                    "Live wallpaper set by $live_set_by | Lockscreen set by an anonymous user"
                                } else if (set_by_home.isNullOrEmpty() && set_by_lock.isNullOrEmpty()) {
                                    "Live wallpaper set by an anonymous user | Lockscreen set by an anonymous user"
                                } else {
                                    "Live wallpaper set by $set_by_home | Lockscreen set by $set_by_lock"
                                }
                            }
                        } else {
                            if (livS) {
                                if (live_set_by.isNullOrEmpty()) {
                                    "Wallpaper set by an anonymous user"
                                } else {
                                    "Wallpaper set by $live_set_by"
                                }
                            } else {
                                if (set_by.isNullOrEmpty()) {
                                    "Wallpaper set by an anonymous user"
                                } else {
                                    "Wallpaper set by $set_by"
                                }
                            }
                        }).toString())
                    )
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            notificationManager.notify(0, builder.build())
        }

        private fun bitmapResizer(bitmap: Bitmap, targetHeight: Int, targetWidth: Int): Bitmap {

            val resizedBitmap =
                Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(resizedBitmap)
            val left = (targetWidth - bitmap.width) / 2f
            val top = (targetHeight - bitmap.height) / 2f
            canvas.drawBitmap(bitmap, left, top, null)
            return resizedBitmap

        }

        private fun returnBitmap(targetBmp: Bitmap, height: Int, width: Int): Bitmap {
            val matrix = Matrix()
            matrix.setRectToRect(
                RectF(0f, 0f, targetBmp.width.toFloat(), targetBmp.height.toFloat()),
                RectF(0f, 0f, width.toFloat(), height.toFloat()),
                Matrix.ScaleToFit.CENTER
            )
            return Bitmap.createBitmap(
                targetBmp,
                0,
                0,
                targetBmp.width,
                targetBmp.height,
                matrix,
                true
            )
        }

        private fun downloadFile(url: String?) {
            if (url != null) {
                val uri = Uri.parse(url)
                val dI = uri.lastPathSegment.toString()
                if (!File(Environment.getExternalStorageDirectory().absolutePath + "/WTchanger/$dI").exists()) {
                    val folder =
                        File(Environment.getExternalStorageDirectory().absolutePath + "/WTchanger")
                    val destinationFile = File(folder, dI)

                    if (!folder.exists()) {
                        folder.mkdirs()
                    }
                    try {
                        Fuel.download(url).destination { _, _ -> destinationFile }
                            .response { _, _, result ->
                                when (result) {
                                    is com.github.kittinunf.result.Result.Success -> {
                                        println("downloaded")
                                    }

                                    is com.github.kittinunf.result.Result.Failure -> {
                                        println("Error")
                                    }
                                }
                            }
                    } catch (e: Exception) {
                        println(e)
                    }
                }
            }
        }
    }
}

