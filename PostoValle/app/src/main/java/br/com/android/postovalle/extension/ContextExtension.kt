package br.com.android.postovalle.extension

import android.content.Context
import android.net.ConnectivityManager
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * Created by james.martins on 17/01/18.
 */


fun Context.toast(text: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, duration).show()
}

fun Context.isNetworkConnected(): Boolean {
    val service = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return (service.activeNetworkInfo != null &&
            service.activeNetworkInfo!!.isConnected)
}





