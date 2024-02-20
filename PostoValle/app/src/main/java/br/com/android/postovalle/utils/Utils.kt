package br.com.android.postovalle.utils

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.webkit.CookieManager
import android.content.SharedPreferences

object Utils {
    private val SP_FILE_NAME = "br.com.android.postovalle"

    val REQUEST_PERMISSION_CODE = 7

    val REQUEST_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isAvailable
            && cm.activeNetworkInfo!!.isConnected
        ) {
            true
        } else false
    }

    fun saveToPreference(context: Context, key: String?, value: String?) {
        val sharedPreferences = context.getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun saveToPreference(context: Context, key: String, value: Boolean) {
        val sharedPreferences = context.getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun readFromPreferences(context: Context, key: String, defaultValue: String): String? {
        val sharedPreferences = context.getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(key, defaultValue)
    }

    fun readFromPreferences(context: Context, key: String, defaultValue: Boolean): Boolean? {
        val sharedPreferences = context.getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(key, defaultValue)
    }
}