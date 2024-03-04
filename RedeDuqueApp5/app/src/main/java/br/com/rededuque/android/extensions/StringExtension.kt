package br.com.rededuque.android.extensions

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.text.TextUtils
import android.util.ArraySet
import android.util.Base64.encodeToString

import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import java.lang.NumberFormatException
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList


/**
 * Created by james.martins on 17/01/18.
 */


fun String.safelyLimitedTo(len: Int): String {
    if (this.count() <= len) return this
    return substring(0, len)
}

fun String.onlyNumbers(): String {
    val p = Pattern.compile("-?\\d+")
    val m = p.matcher(this)
    var result = ""
    while (m.find()) {
        result+= m.group()
    }
    return result
}

fun String.onlyNumbers2(): String {
    val result = this.replace("[^-?0-9]+".toRegex(), " ").replace(" ", "" )
    return result.trim()
}

fun String.fromBase64() : String? {
    val decodedString: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        String(Base64.getDecoder().decode(this))
    } else {
        TODO("VERSION.SDK_INT < O")
    }
    return decodedString
}


fun String.toBase64() : String {
    val encodedString: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Base64.getEncoder().encodeToString(this.toByteArray())
    } else {
        TODO("VERSION.SDK_INT < O")
    }
    return encodedString
}

fun String.isValidCPF(): Boolean {
    if (TextUtils.isEmpty(this)) return false

    val numbers = arrayListOf<Int>()

    this.filter { it.isDigit() }.forEach {
        numbers.add(it.toString().toInt())
    }

    if (numbers.size != 11) return false

    //repeticao
    (0..9).forEach { n ->
        val digits = arrayListOf<Int>()
        (0..10).forEach { _ -> digits.add(n) }
        if (numbers == digits) return false
    }

    //digito 1
    val dv1 = ((0..8).sumOf { (it + 1) * numbers[it] }).rem(11).let {
        if (it >= 10) 0 else it
    }

    val dv2 = ((0..8).sumOf { it * numbers[it] }.let { (it + (dv1 * 9)).rem(11) }).let {
        if (it >= 10) 0 else it
    }

    return numbers[9] == dv1 && numbers[10] == dv2
}


