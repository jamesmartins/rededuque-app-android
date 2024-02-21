package br.com.android.postovalle

import android.app.Application
import android.text.TextUtils
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.onesignal.OneSignal

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

        // Logging set to help debug issues, remove before releasing your app.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)

        // OneSignal Initialization
        OneSignal.initWithContext(this)
        OneSignal.setAppId(ONESIGNAL_APP_ID)
    }
}