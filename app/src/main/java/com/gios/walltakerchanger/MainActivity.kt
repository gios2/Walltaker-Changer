@file:Suppress("DEPRECATION")

package com.gios.walltakerchanger

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.*
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.gson.GsonBuilder
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import okhttp3.*
import java.io.File
import java.io.IOException


@SuppressLint("StaticFieldLeak")
lateinit var image: ImageView

@SuppressLint("StaticFieldLeak")
lateinit var text: TextView

@SuppressLint("StaticFieldLeak")
lateinit var start: Button

@SuppressLint("StaticFieldLeak")
lateinit var stop: Button

@SuppressLint("StaticFieldLeak")
lateinit var update: Button

var linkId: String? = null
var linkUrl: String? = null
var setHome: Boolean = false
var setLock: Boolean = false
var id: String? = null
var expires: String? = null
var username: String? = null
var terms: String? = null
var blacklist: String? = null
var post_url: String? = null
var post_thumbnail_url: String? = null
var post_description: String? = null
var created_at: String? = null
var updated_at: String? = null
var set_by: String? = null
var response_type: String? = null
var response_text: String? = null
var online: Boolean = false
const val verNr = "v0.1"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        orientation()
        theme()

        obtainWallpaper()
        image = findViewById(R.id.image)
        text = findViewById(R.id.textView)
        stop = findViewById(R.id.stop)
        update = findViewById(R.id.update)
        start = findViewById(R.id.start)

        start.setOnClickListener {
            startService(Intent(this, Service::class.java))
            start()
        }
        stop.setOnClickListener {
            stopService(Intent(this, Service::class.java))
            File(this.cacheDir.path).deleteRecursively()
            stop()

        }
        update.setOnClickListener {
            File(this.cacheDir.path).deleteRecursively()
            text.text = ""
            image.setImageResource(0)
            println(id)
            obtainWallpaper()

        }
    }


    private fun obtainWallpaper() {
        val sharedPreferences: SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(this)

        linkId = sharedPreferences.getString("Id", "0")
        linkUrl = "https://walltaker.joi.how/api/links/$linkId.json"


        val request = Request.Builder().url(linkUrl!!).addHeader("Walltaker-Changer", verNr)
            .addHeader("User-Agent", "Walltaker-Changer/$verNr").build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("error on json request: ${e.message}")
            }

            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call, response: Response) {
                try {
                    val body = response.body()?.string()
                    println("(HomeFragment)Response: $body")

                    val gson = GsonBuilder().create()
                    val data = gson.fromJson(body, LinkData::class.java)
                    post_url = null

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

                        text.text =
                            "Your id is $id\n\nYour username is $username\n\n$terms\nI don't like $blacklist\n\nThe wallpaper has been set by $set_by"
                        println(post_url)
                        if (post_url != null) {


                            val handler = Handler(Looper.getMainLooper())
                            handler.post {
                                image.scaleType = ImageView.ScaleType.CENTER_INSIDE
                                Picasso.get()
                                    .load(post_url)
                                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                                    .networkPolicy(NetworkPolicy.NO_CACHE)
                                    .noFade()
                                    .into(image)
                                handler.removeCallbacksAndMessages(null)

                            }
                        }
                    }
                } catch (e: Exception) {
                    println("error while getting Image")
                }
            }
        })
    }


    private fun orientation() {
        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.landscape)
        } else {
            setContentView(R.layout.portrait)
        }
    }

    private fun theme() {
        if (isDarkThemeOn()) {
            this.supportActionBar!!.title =
                Html.fromHtml("<font color='#FFB300'>Walltaker Changer</font>")
        } else {
            this.supportActionBar!!.title =
                Html.fromHtml("<font color='#0D47A1'>Walltaker Changer</font>")
        }
    }

    private fun stop() {
        if (linkId!!.toInt() == 0) {
            Toast.makeText(this, "Set a id", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Stopping Walltaker task...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun start() {
        if (linkId!!.toInt() == 0) {
            Toast.makeText(this, "Set a id", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Starting Walltaker task...", Toast.LENGTH_SHORT).show()
            Updater.updateWallpaper(this)
        }
    }

    private fun isDarkThemeOn(): Boolean {
        return resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                settings()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

    private fun settings() {
        val intent = Intent(applicationContext, SettingsActivity::class.java)
        startActivity(intent)
    }

}

class LinkData(
    var id: String,
    var expires: String,
    var username: String,
    var terms: String,
    var blacklist: String,
    var post_url: String,
    var post_thumbnail_url: String,
    var post_description: String,
    var created_at: String,
    var updated_at: String,
    var set_by: String,
    var response_type: String,
    var response_text: String,
    var online: Boolean
)
