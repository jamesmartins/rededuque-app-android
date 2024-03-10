package br.com.rededuque.android

import android.R
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import br.com.rededuque.android.R.id
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
        val txtCadastro = findViewById<TextView>(R.id.txtCadastro)
        val txtFaleConosco = findViewById<TextView>(R.id.txtFaleConosco)
        val txtParceiro = findViewById<TextView>(R.id.txtParceiro)
        btnLoginMenu.setOnClickListener {
            startActivity(Intent(applicationContext, LoginActivity2::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }
    }

}