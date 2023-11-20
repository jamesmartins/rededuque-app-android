package br.com.rededuque.android.utils

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.webkit.CookieManager

object Utils {
    private val SP_FILE_NAME = "br.com.rededuque.android"

    val REQUEST_PERMISSION_CODE = 7

    fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return (cm.activeNetworkInfo != null
                && cm.activeNetworkInfo!!.isAvailable
                && cm.activeNetworkInfo!!.isConnected)
    }


    @Suppress("DEPRECATION")
//    fun isInternetAvailable(context: Context): Boolean {
//        var result = false
//        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            cm?.run {
//                cm.getNetworkCapabilities(cm.activeNetwork)?.run {
//                    result = when {
//                        hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
//                        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
//                        hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
//                        else -> false
//                    }
//                }
//            }
//        } else {
//            cm?.run {
//                cm.activeNetworkInfo?.run {
//                    if (type == ConnectivityManager.TYPE_WIFI) {
//                        result = true
//                    } else if (type == ConnectivityManager.TYPE_MOBILE) {
//                        result = true
//                    }
//                }
//            }
//        }
//        return result
//    }

    fun getCookie(siteName: String, CookieName: String): String? {
        var CookieValue: String? = null

        val cookieManager = CookieManager.getInstance()
        val cookies = cookieManager.getCookie(siteName)
        val temp = cookies.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (ar1 in temp) {
            if (ar1.contains(CookieName)) {
                val temp1 = ar1.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                CookieValue = temp1[1]
                break
            }
        }
        return CookieValue
    }

    fun saveToPreference(context: Context, key: String, value: String) {
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