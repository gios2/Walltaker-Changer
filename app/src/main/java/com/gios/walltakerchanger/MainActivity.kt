@file:Suppress("DEPRECATION")

package com.gios.walltakerchanger

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
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
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.dcastalia.localappupdate.DownloadApk
import com.github.kittinunf.fuel.httpGet
import com.google.gson.GsonBuilder
import kotlin.system.exitProcess

@SuppressLint("StaticFieldLeak")
lateinit var imageHome: ImageView

@SuppressLint("StaticFieldLeak")
lateinit var imageLock: ImageView

@SuppressLint("StaticFieldLeak")
lateinit var textHome: TextView

@SuppressLint("StaticFieldLeak")
lateinit var textLock: TextView

@SuppressLint("StaticFieldLeak")
lateinit var panic: Button

@SuppressLint("StaticFieldLeak")
lateinit var start: Button

@SuppressLint("StaticFieldLeak")
lateinit var stop: Button

@SuppressLint("StaticFieldLeak")
lateinit var update: Button

@SuppressLint("StaticFieldLeak")
lateinit var div: View

var linkUrl: String? = null
var linkUrlHome: String? = null
var linkUrlLock: String? = null
var linkId: String? = null
var linkIdHome: String? = null
var linkIdLock: String? = null
var setHome: Boolean = false
var setLock: Boolean = false
var id: String? = null

// --Commented out by Inspection (06/07/2023 01:25):var expires: String? = null
var username: String? = null
var terms: String? = null
var blacklist: String? = null
var post_url: String? = null
var post_url_home: String? = null
var post_url_lock: String? = null
var post_thumbnail_url: String? = null
var post_description: String? = null

// --Commented out by Inspection (06/07/2023 01:25):var created_at: String? = null
var updated_at: String? = null
var set_by: String? = null
var response_type: String? = null
var response_text: String? = null

// --Commented out by Inspection (06/07/2023 01:25):var online: Boolean = false
lateinit var wl: WakeLock
var receiver: BroadcastReceiver? = null
var multiMode = false

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storagePerm()
        ignoreBatteryOptimization()
        orientation()
        theme()
        obtainWallpaper()
        configureReceiver()



        div = findViewById(R.id.divider)
        imageHome = findViewById(R.id.imageHome)
        textHome = findViewById(R.id.textHome)
        imageLock = findViewById(R.id.imageLock)
        textLock = findViewById(R.id.textLock)
        textHome.movementMethod = ScrollingMovementMethod()
        textLock.movementMethod = ScrollingMovementMethod()
        stop = findViewById(R.id.stop)
        panic = findViewById(R.id.panic)
        update = findViewById(R.id.update)
        start = findViewById(R.id.start)
        reset()
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
            reset()
            obtainWallpaper()
            reset()
            obtainWallpaper()
        }
    }

    private fun reset() {
        if (multiMode) {
            imageHome.visibility = View.GONE
            imageLock.visibility = View.GONE
            div.visibility = View.GONE
            textHome.text = ""
            imageHome.setImageResource(0)
            textLock.text = ""
            imageLock.setImageResource(0)
            imageHome.visibility = View.VISIBLE
            imageLock.visibility = View.VISIBLE
            div.visibility = View.VISIBLE
            textLock.visibility = View.VISIBLE

        } else {
            imageHome.visibility = View.GONE
            imageLock.visibility = View.GONE
            textLock.visibility = View.GONE
            div.visibility = View.GONE
            textHome.text = ""
            imageHome.setImageResource(0)
            imageHome.visibility = View.VISIBLE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun obtainWallpaper() {


        val sharedPreferences: SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(this)

        multiMode = sharedPreferences.getBoolean("multimode", false)
        linkId = sharedPreferences.getString("id", "0")
        linkUrl = "https://walltaker.joi.how/api/links/$linkId.json"
        linkIdHome = sharedPreferences.getString("IdHome", "0")
        linkUrlHome = "https://walltaker.joi.how/api/links/$linkIdHome.json"
        linkIdLock = sharedPreferences.getString("IdLock", "0")
        linkUrlLock = "https://walltaker.joi.how/api/links/$linkIdLock.json"
        println(linkId)
        runOnUiThread {
            if (multiMode) {
                linkUrlHome!!.httpGet().header("User-Agent" to "Walltaker-Changer/")
                    .responseString { _, response, result ->
                        if (response.statusCode == 200) {
                            val gson = GsonBuilder().create()
                            val data = gson.fromJson(result.get(), LinkData::class.java)
                            post_url_home = null
                            if (data != null) {
                                id = data.id
                                //expires = data.expires
                                username = data.username
                                terms = data.terms
                                blacklist = data.blacklist
                                post_url_home = data.post_url
                                post_thumbnail_url = data.post_thumbnail_url
                                post_description = data.post_description
                                //created_at = data.created_at
                                updated_at = data.updated_at
                                set_by = data.set_by
                                response_type = data.response_type
                                response_text = data.response_text
                                //online = data.online
                                if (post_description != "") {
                                    textHome.text =
                                        "HomeScreen Link\nYou are using $username's id $id\n\nThe wallpaper has been set by $set_by\n\nThe post description is $post_description\n\nThe link terms are: $terms\n\nThe blacklist tags are: $blacklist"
                                } else {
                                    textHome.text =
                                        "HomeScreen Link\nYou are using $username's id $id\n\nThe wallpaper has been set by $set_by\n\nThe link terms are: $terms\n\nThe blacklist tags are: $blacklist"
                                }
                                println(post_url_home)
                                if (post_url_home != null) {
                                    val handler = Handler(Looper.getMainLooper())
                                    handler.post {
                                        imageHome.scaleType = ImageView.ScaleType.CENTER_INSIDE
                                        Glide.with(this@MainActivity).load(post_url_home)
                                            .fitCenter()
                                            .into(imageHome)
                                        handler.removeCallbacksAndMessages(null)
                                    }
                                }


                            }
                        }
                        linkUrlLock!!.httpGet().header("User-Agent" to "Walltaker-Changer/")
                            .responseString { _, response2, result2 ->
                                if (response2.statusCode == 200) {
                                    val gson = GsonBuilder().create()
                                    val data = gson.fromJson(result2.get(), LinkData::class.java)
                                    post_url_lock = null
                                    if (data != null) {
                                        id = data.id
                                        //expires = data.expires
                                        username = data.username
                                        terms = data.terms
                                        blacklist = data.blacklist
                                        post_url_lock = data.post_url
                                        post_thumbnail_url = data.post_thumbnail_url
                                        post_description = data.post_description
                                        //created_at = data.created_at
                                        updated_at = data.updated_at
                                        set_by = data.set_by
                                        response_type = data.response_type
                                        response_text = data.response_text
                                        //online = data.online
                                        if (post_description != "") {
                                            textLock.text =
                                                "Lockscreen Link\nYou are using $username's id $id\n\nThe wallpaper has been set by $set_by\n\nThe post description is $post_description\n\nThe link terms are: $terms\n\nThe blacklist tags are: $blacklist"
                                        } else {
                                            textLock.text =
                                                "Lockscreen Link\nYou are using $username's id $id\n\nThe wallpaper has been set by $set_by\n\nThe link terms are: $terms\n\nThe blacklist tags are: $blacklist"
                                        }
                                        println(post_url_lock)
                                        if (post_url_lock != null) {
                                            val handler = Handler(Looper.getMainLooper())
                                            handler.post {
                                                imageHome.scaleType =
                                                    ImageView.ScaleType.CENTER_INSIDE
                                                Glide.with(this@MainActivity).load(post_url_lock)
                                                    .fitCenter()
                                                    .into(imageLock)
                                                handler.removeCallbacksAndMessages(null)
                                            }
                                        }


                                    }
                                }
                            }
                    }

            } else {
                linkUrl!!.httpGet().header("User-Agent" to "Walltaker-Changer/")
                    .responseString { _, response, result ->
                        if (response.statusCode == 200) {
                            val gson = GsonBuilder().create()
                            val data = gson.fromJson(result.get(), LinkData::class.java)
                            post_url = null
                            if (data != null) {
                                id = data.id
                                //expires = data.expires
                                username = data.username
                                terms = data.terms
                                blacklist = data.blacklist
                                post_url = data.post_url
                                post_thumbnail_url = data.post_thumbnail_url
                                post_description = data.post_description
                                //created_at = data.created_at
                                updated_at = data.updated_at
                                set_by = data.set_by
                                response_type = data.response_type
                                response_text = data.response_text
                                //online = data.online
                                if (post_description != "") {
                                    textHome.text =
                                        "You are using $username's id $id\n\nThe wallpaper has been set by $set_by\n\nThe post description is $post_description\n\nThe link terms are: $terms\n\nThe blacklist tags are: $blacklist"
                                } else {
                                    textHome.text =
                                        "You are using $username's id $id\n\nThe wallpaper has been set by $set_by\n\nThe link terms are: $terms\n\nThe blacklist tags are: $blacklist"
                                }
                                println(post_url)
                                if (post_url != null) {
                                    val handler = Handler(Looper.getMainLooper())
                                    handler.post {
                                        imageHome.scaleType = ImageView.ScaleType.CENTER_INSIDE
                                        Glide.with(this@MainActivity).load(post_url)
                                            .fitCenter()
                                            .into(imageHome)
                                        handler.removeCallbacksAndMessages(null)
                                    }
                                }
                            }
                        }
                    }
            }
        }
    }


    private fun orientation() {
        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.landscape_multi)
        } else {
            setContentView(R.layout.portrait_multi)
        }
    }

    private fun storagePerm() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_DENIED
        ) {
            val requestPermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { _: Boolean ->

            }
            requestPermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:" + this.packageName)
                this.startActivity(intent)
            }

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
        } else if (multiMode && (linkIdHome == "0" || linkIdLock == "0")) {
            Toast.makeText(this, "Set a id for multi mode", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Stopping Walltaker task...", Toast.LENGTH_SHORT).show()
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(this, BroadcastReceiver::class.java)
            val pendingIntent =
                PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            alarmManager.cancel(pendingIntent)
            unregisterReceiver(receiver)
            stopService(Intent(this, Service::class.java))
            finishAndRemoveTask()
            exitProcess(0)
        }
    }

    private fun panic() {
        stopService(Intent(this, Service::class.java))
        val wallpaperManager = WallpaperManager.getInstance(this)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, BroadcastReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pendingIntent)
        unregisterReceiver(receiver)
        wallpaperManager.clear()
        finishAndRemoveTask()
        exitProcess(0)
    }


    @SuppressLint("ScheduleExactAlarm")
    private fun start() {
        if (linkId!!.toInt() == 0) {
            Toast.makeText(this, "Set a id", Toast.LENGTH_SHORT).show()
        } else if (multiMode && (linkIdHome == "0" || linkIdLock == "0")) {
            Toast.makeText(this, "Set a id for multi mode", Toast.LENGTH_SHORT).show()
        } else {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(this, BroadcastReceiver::class.java)
            val pendingIntent =
                PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 1000,
                pendingIntent
            )
            startService(Intent(this, Service::class.java))
            finishAndRemoveTask()
            exitProcess(0)
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

            R.id.action_upApp -> {
                val url =
                    "https://github.com/gios2/Walltaker-Changer/raw/main/app/release/app-release.apk"
                val downloadApk = DownloadApk(this@MainActivity)
                downloadApk.startDownloadingApk(url)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun settings() {
        val intent = Intent(applicationContext, SettingsActivity::class.java)
        startActivity(intent)
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun configureReceiver() {
        val filter = IntentFilter()
        filter.addAction("com.gios.walltakerchanger")
        receiver = BroadcastReceiver()
        registerReceiver(receiver, filter)
    }
}

@Suppress("PropertyName")
class LinkData(
    var id: String,
    //var expires: String,
    var username: String,
    var terms: String,
    var blacklist: String,
    var post_url: String,
    var post_thumbnail_url: String,
    var post_description: String,
    //var created_at: String,
    var updated_at: String,
    var set_by: String,
    var response_type: String,
    var response_text: String,
    //var online: Boolean
)
