package br.com.rededuque.android

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.widget.TextView
import br.com.rededuque.android.utils.Utils
import br.com.rededuque.android.utils.baseURL
import br.com.rededuque.android.utils.mUrlCadastro
import br.com.rededuque.android.utils.mUrlFaleConosco
import br.com.rededuque.android.utils.mUrlParceiro
import br.com.rededuque.android.utils.mUrl_NOVO_MENU

class IntroActivity2 : AppCompatActivity() {
    private val TAG = IntroActivity2::class.java.simpleName
    private var isConnected = false
    private var txtCadastro: TextView? = null
    private var txtFaleConosco: TextView? = null
    private var txtParceiro: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro2)

        isConnected = Utils.isNetworkConnected(applicationContext)

        // Load Views
        initViews()
    }

    fun initViews(){
        var btnLoginMenu = findViewById<View>(R.id.btnLoginMenu)
        txtCadastro = findViewById(R.id.txtCadastro)
        txtFaleConosco = findViewById(R.id.txtFaleConosco)
        txtParceiro = findViewById(R.id.txtParceiro)
        btnLoginMenu.setOnClickListener {
            startActivity(Intent(applicationContext, LoginActivity2::class.java))
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
//            finish()
        }

        // Open Cadastro page webview
        txtCadastro!!.setOnClickListener {
            var mUrl = baseURL + mUrlCadastro
            startActivity(Intent(applicationContext, WebViewActivity::class.java).putExtra("URL_LOAD_CONTENT", mUrl))
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        // Open Fale Conosco
        txtFaleConosco!!.setOnClickListener {
            var mUrl = baseURL + mUrlFaleConosco
            startActivity(Intent(applicationContext, WebViewActivity::class.java).putExtra("URL_LOAD_CONTENT", mUrl))
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        // Open Parceiro
        txtParceiro!!.setOnClickListener {
            var mUrl = baseURL + mUrlParceiro
            startActivity(Intent(applicationContext, WebViewActivity::class.java).putExtra("URL_LOAD_CONTENT", mUrl))
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }
}