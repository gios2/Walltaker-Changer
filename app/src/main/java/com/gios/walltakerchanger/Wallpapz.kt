package com.gios.walltakerchanger

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import android.view.View
import android.webkit.WebView
import java.util.Timer
import java.util.TimerTask
import kotlin.system.exitProcess


class Wallpapz : WallpaperService() {


    override fun onCreateEngine(): Engine {
        return WallpaperEngine()
    }

    private inner class WallpaperEngine : Engine() {

        private var mVisible = false

        private val webViewW = WebView(this@Wallpapz)

        @SuppressLint("SetJavaScriptEnabled")
        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)

            val pm = this@Wallpapz.getSystemService(Context.POWER_SERVICE) as PowerManager
            wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WalltakerChanger:WAKEUP")
            wl.acquire(10 * 60 * 1000L /*10 minutes*/)
            webViewW.settings.javaScriptEnabled = true
            webViewW.setBackgroundColor(0)
            webViewW.setLayerType(View.LAYER_TYPE_HARDWARE, null)
            println(post_url!!.split(".").last())

            webViewW.loadData(
                "<html><head><style type=text/css>body{margin:auto auto;text-align:center;} body{background-image:url(${lastUrl})!important; background-attachment:fixed; background-position:center; background-size: cover;}  img{display: block; margin: 0 auto; width: 100%;} </style></head><body></body></html>",
                "text/html",
                "UTF-8"
            )

            var isSurfaceLocked = false
            val timer = Timer()

            val task: TimerTask = object : TimerTask() {
                override fun run() {
                    synchronized(holder) {
                        try {
                            if (mVisible && !isSurfaceLocked) {
                                val handler = Handler(Looper.getMainLooper())
                                handler.post {
                                    val canvas = holder.lockCanvas()
                                    if (canvas != null) {
                                        isSurfaceLocked = true
                                        webViewW.draw(canvas)
                                        if (new) {
                                            webViewW.loadData(
                                                "<html><head><style type=text/css>body{margin:auto auto;text-align:center;} body{background-image:url(${lastUrl})!important; background-attachment:fixed; background-position:center; background-size: cover;}  img{display: block; margin: 0 auto; width: 100%; height: 100%;} </style></head><body></body></html>",
                                                "text/html",
                                                "UTF-8"
                                            )
                                            new = false
                                        }
                                        holder.unlockCanvasAndPost(canvas)
                                        isSurfaceLocked = false
                                    }
                                }
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        if (clos) {
                            exitProcess(0)
                        }
                    }
                }
            }
            timer.scheduleAtFixedRate(task, 0, 15)
            wl.release()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            mVisible = visible
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            webViewW.layout(0, 0, width, height)
            new = true
        }


        override fun onDestroy() {
            super.onDestroy()
            webViewW.destroy()
        }
    }
}
