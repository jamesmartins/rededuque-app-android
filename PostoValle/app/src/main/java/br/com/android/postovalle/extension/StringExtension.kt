package br.com.android.postovalle.extension

import android.os.Build

import java.util.*
import java.util.regex.Pattern


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


