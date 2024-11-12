package br.com.rededuque.android

import android.annotation.SuppressLint
import android.app.Application
import com.onesignal.Continue.none
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors


const val ONESIGNAL_APP_ID = "3761ab4e-4c3b-432e-a69e-f8b792543e44"

val TAG = InitApplication::class.java.simpleName

class InitApplication : Application() {
    companion object {
        private var mInstance: InitApplication? = null

        @Synchronized
        fun getInstance(): InitApplication? {
            return mInstance
        }
    }

    override fun onCreate() {
        super.onCreate()
        mInstance = this

        // Version 4.x
//        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
        // Version 5.x
        OneSignal.Debug.logLevel = LogLevel.VERBOSE

        // OneSignal Initialization

        // Version 4.x
        // OneSignal.initWithContext(this)
        // OneSignal.setAppId(ONESIGNAL_APP_ID)
        // Version 5.x
        OneSignal.initWithContext(this, ONESIGNAL_APP_ID)

        // requestPermission will show the native Android notification permission prompt.
        // NOTE: It's recommended to use a OneSignal In-App Message to prompt instead.
//        CoroutineScope(Dispatchers.IO).launch {
//            OneSignal.Notifications.requestPermission(true)
//        }
    }
}