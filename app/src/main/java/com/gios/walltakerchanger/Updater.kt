package com.gios.walltakerchanger

import android.app.WallpaperManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.os.PowerManager
import android.util.DisplayMetrics
import android.view.WindowManager
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

            if (multiMode) {
                linkIdHome = sharedPreferences.getString("IdHome", "0")
                linkUrlHome = "https://walltaker.joi.how/api/links/$linkIdHome.json"
                linkUrlHome!!.httpGet().header("User-Agent" to "Walltaker-Changer/")
                    .responseString { _, response, result ->
                        if (response.statusCode == 200) {
                            val gson = GsonBuilder().create()
                            val data = gson.fromJson(result.get(), LinkData::class.java)
                            if (data != null) {
                                post_url_home = data.post_url

                                if (post_url_home != "null" && !post_url_home.isNullOrEmpty()) {
                                    if (post_url_home != lastUrlHome) {
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
                                                    Glide.with(context).asBitmap()
                                                        .load(post_url_home)
                                                        .fitCenter().submit(width, height)
                                                bitmap = withContext(Dispatchers.IO) {
                                                    futureTarget.get()
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
                                    lastUrlHome = data.post_url
                                    if (sharedPreferences.getBoolean("download1", false)) {
                                        downloadFile(
                                            post_url_home!!
                                        )
                                    }
                                }

                            }
                        } else {
                            println("error on home json request: ${response.statusCode}")
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

                                if (post_url_lock != "null" && !post_url_lock.isNullOrEmpty()) {
                                    if (post_url_lock != lastUrlLock) {
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
                                                    Glide.with(context).asBitmap().load(
                                                        post_url_lock
                                                    )
                                                        .fitCenter().submit(width, height)
                                                bitmap = withContext(Dispatchers.IO) {
                                                    futureTarget.get()
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
                                                lastUrl = ""
                                            }
                                        }
                                        if (sharedPreferences.getBoolean("download2", false)) {
                                            downloadFile(
                                                post_url_lock!!
                                            )
                                        }
                                    }
                                    lastUrl = data.post_url
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

                                if (post_url != "null" && !post_url.isNullOrEmpty()) {
                                    if (post_url != lastUrl) {
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
                                                    Glide.with(context).asBitmap()
                                                        .load(post_url)
                                                        .fitCenter()
                                                        .submit(width, height)
                                                bitmap = withContext(Dispatchers.IO) {
                                                    futureTarget.get()
                                                }
                                                Glide.with(context).clear(futureTarget)
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
                                                post_url!!
                                            )
                                        }
                                    }
                                    lastUrl = data.post_url
                                }
                            }
                        } else {
                            println("error on json request: ${response.statusCode}")
                        }
                        wl.release()
                    }
            }
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

