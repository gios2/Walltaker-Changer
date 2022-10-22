package com.gios.walltakerchanger


import android.app.WallpaperManager
import android.content.Context
import android.content.SharedPreferences
import android.os.HandlerThread
import android.os.Process
import androidx.preference.PreferenceManager
import com.google.gson.GsonBuilder
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import okhttp3.*
import java.io.IOException

class Updater {

    companion object {
        var lastUrl = ""

        fun updateWallpaper(context: Context) {
            val sharedPreferences: SharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context)


            linkId = sharedPreferences.getString("Id", "0")
            linkUrl = "https://walltaker.joi.how/api/links/$linkId.json"

            setHome = sharedPreferences.getBoolean("wallpaper", false)
            setLock = sharedPreferences.getBoolean("lockscreen", false)




            val request = Request.Builder().url(linkUrl!!).addHeader("Walltaker-Changer", verNr)
                .addHeader("User-Agent", "Walltaker-Changer/$verNr").build()

            val client = OkHttpClient()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    println("error on json request: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val body = response.body()?.string()
                        println("(Updater)Response: $body")

                        val gson = GsonBuilder().create()
                        val data = gson.fromJson(body, LinkData::class.java)

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
                                println("post_url: $post_url")
                                if (post_url != lastUrl) {


                                    val picasso = Picasso.Builder(context).build()
                                    val thread = HandlerThread(
                                        "ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND
                                    )
                                    thread.start()

                                    val bitmap =
                                        picasso.load(post_url).memoryPolicy(MemoryPolicy.NO_CACHE)
                                            .noFade().get()
                                    thread.quitSafely()

                                    val wallpaperManager = WallpaperManager.getInstance(context)

                                    if (setHome) {
                                        println("setting home")
                                        wallpaperManager.setBitmap(
                                            bitmap, null, true, WallpaperManager.FLAG_SYSTEM
                                        )
                                    }
                                    if (setLock) {
                                        println("setting lock")
                                        wallpaperManager.setBitmap(
                                            bitmap, null, true, WallpaperManager.FLAG_LOCK
                                        )
                                    }
                                }
                                lastUrl = data.post_url
                            }
                            println(
                                "Last is $lastUrl, post is $post_url" + if (lastUrl == post_url) {
                                    " they are same"
                                } else {
                                    "they are different"
                                }
                            )

                        }

                    } catch (e: Exception) {
                        println("error while getting Image: ${e.message}")
                    }
                }
            })
        }


    }
}


