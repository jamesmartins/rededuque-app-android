package br.com.rededuque.android

import android.app.Application
import com.onesignal.OneSignal

const val ONESIGNAL_APP_ID = "3761ab4e-4c3b-432e-a69e-f8b792543e44"

class InitApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Logging set to help debug issues, remove before releasing your app.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)

        // OneSignal Initialization
        OneSignal.initWithContext(this)
        OneSignal.setAppId(ONESIGNAL_APP_ID)
    }
}