package br.com.rededuque.android.persistent

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

object JsonDBUtil {
    private var prefs: SharedPreferences? = null
    private lateinit var mPref: SharedPreferences
    private val DEFAULT_DATA_KEY = "br.com.rededuque.android.dbjson"
    private lateinit var calendar: Calendar
    private lateinit var dateFormat: SimpleDateFormat

    const val VIEW_INTRO_HOME = "VIEW_INTRO_HOME"
    const val VIEW_LOGIN = "VIEW_LOGIN"

    fun init(context: Context) {
        if (prefs == null)
            prefs = context.getSharedPreferences(DEFAULT_DATA_KEY, Context.MODE_PRIVATE)
    }

    fun setIntroHome(keyName: String = VIEW_INTRO_HOME, data: String) {
        if (prefs != null) {
            val editor: SharedPreferences.Editor = prefs!!.edit()
            val gson = Gson()
            val dataJson = gson.toJson(data)
            editor.putString(keyName, dataJson)
            editor.commit()
        }
    }

    fun getIntoHome(): String {
        if (prefs != null) {
            val jsonSaved = prefs!!.getString(VIEW_INTRO_HOME, "")
            val gson = Gson()
            return gson.fromJson(jsonSaved, object : TypeToken<String>() {}.type)
        }
        return ""
    }

    fun setLogin(keyName: String = VIEW_LOGIN, data: String) {
        if (prefs != null) {
            val editor: SharedPreferences.Editor = prefs!!.edit()
            val gson = Gson()
            val dataJson = gson.toJson(data)
            editor.putString(keyName, dataJson)
            editor.commit()
        }
    }

    fun getLogin(): String {
        if (prefs != null) {
            val jsonSaved = prefs!!.getString(VIEW_LOGIN, "")
            val gson = Gson()
            return gson.fromJson(jsonSaved, object : TypeToken<String>() {}.type)
        }
        return ""
    }

}