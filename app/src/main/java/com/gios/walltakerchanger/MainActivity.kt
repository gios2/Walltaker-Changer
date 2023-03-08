@file:Suppress("DEPRECATION")

package com.gios.walltakerchanger

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.net.ConnectivityManager.*
import android.net.Uri
import android.os.*
import android.os.PowerManager.WakeLock
import android.provider.Settings
import android.text.Html
import android.text.method.ScrollingMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.github.kittinunf.fuel.httpGet
import com.google.gson.GsonBuilder
import kotlin.system.exitProcess

@SuppressLint("StaticFieldLeak")
lateinit var image: ImageView

@SuppressLint("StaticFieldLeak")
lateinit var text: TextView

@SuppressLint("StaticFieldLeak")
lateinit var panic: Button

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
lateinit var wl: WakeLock
const val verNr = "v0.5"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ignoreBatteryOptimization()
        orientation()
        theme()
        obtainWallpaper()

        image = findViewById(R.id.image)
        text = findViewById(R.id.textView)
        stop = findViewById(R.id.stop)
        panic = findViewById(R.id.panic)
        update = findViewById(R.id.update)
        start = findViewById(R.id.start)
        text.movementMethod = ScrollingMovementMethod()

        start.setOnClickListener {
            start()
        }
        stop.setOnClickListener {
            stop()
        }
        panic.setOnClickListener {
            panic()
        }
        update.setOnClickListener {
            text.text = ""
            image.setImageResource(0)
            println(id)
            obtainWallpaper()
        }
    }


    @SuppressLint("SetTextI18n")
    private fun obtainWallpaper() {
        val sharedPreferences: SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(this)

        linkId = sharedPreferences.getString("Id", "0")
        linkUrl = "https://walltaker.joi.how/api/links/$linkId.json"

        linkUrl!!.httpGet().header("User-Agent" to "Walltaker-Changer/$verNr")
            .responseString { _, response, result ->

                if (response.statusCode == 200) {
                    val gson = GsonBuilder().create()
                    val data = gson.fromJson(result.get(), LinkData::class.java)
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
                                Glide.with(this@MainActivity).load(post_url).fitCenter().into(image)
                                handler.removeCallbacksAndMessages(null)
                            }
                        }
                    }
                } else {
                    println("error on json request: ${response.statusCode}")
                }
            }
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

    @SuppressLint("BatteryLife")
    private fun ignoreBatteryOptimization() {
        val intent = Intent()
        val packageName = packageName
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }
    }

    private fun stop() {
        if (linkId!!.toInt() == 0) {
            Toast.makeText(this, "Set a id", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Stopping Walltaker task...", Toast.LENGTH_SHORT).show()
            stopService(Intent(this, Service::class.java))
            exitProcess(-1)
        }
    }

    private fun panic() {
        stopService(Intent(this, Service::class.java))
        val wallpaperManager = WallpaperManager.getInstance(this)
        wallpaperManager.clear()
        finishAndRemoveTask()
        exitProcess(-1)
    }


    private fun start() {
        if (linkId!!.toInt() == 0) {
            Toast.makeText(this, "Set a id", Toast.LENGTH_SHORT).show()
        } else {
            startService(Intent(this, Service::class.java))
            finishAndRemoveTask()
            exitProcess(-1)
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
