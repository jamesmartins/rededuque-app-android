package br.com.rededuque.android

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import br.com.rededuque.android.R.id
import br.com.rededuque.android.extensions.toast
import br.com.rededuque.android.model.ConfigApp
import br.com.rededuque.android.utils.PROJECT_ID
import br.com.rededuque.android.utils.Utils


class IntroActivity : AppCompatActivity() {
    private val TAG = IntroActivity::class.java.simpleName
    private var isConnected = false
//    private lateinit var btnLoginMenu: Button
//    private lateinit var btnLoginMenu: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        isConnected = Utils.isNetworkConnected(applicationContext)

        // Load Views
        initViews()
    }

    fun initViews(){
        var btnLoginMenu = findViewById<View>(id.btnLoginMenu)
        btnLoginMenu.setOnClickListener {
            startActivity(Intent(applicationContext, LoginActivity2::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }
    }

}