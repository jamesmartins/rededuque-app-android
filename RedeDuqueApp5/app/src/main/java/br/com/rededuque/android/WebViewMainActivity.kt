package br.com.rededuque.android

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.webkit.*
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import br.com.rededuque.android.model.User
import br.com.rededuque.android.utils.Utils


class WebViewMainActivity : AppCompatActivity() {
    private var mWebView: WebView? = null
    private var progressBar: ProgressBar? = null
    private lateinit var mToolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        var mUrlLoading = intent.extras!!.getString("URL_LOAD_CONTENT")

        initViews()

        loadContent(mUrlLoading!!)
    }

    private fun loadContent(url: String) {
        val isConnected = Utils.isNetworkConnected(applicationContext)
        if (mWebView != null) {
            if (isConnected) {
                if (url != "")
                    mWebView!!.loadUrl(url.trim { it <= ' ' })
                else
                    Toast.makeText(applicationContext, "Erro no carregamento da página!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(applicationContext, "Sem Conexão!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initViews() {
        progressBar = findViewById(R.id.progress)
        mWebView = findViewById(R.id.mwebview)
        mWebView!!.settings.javaScriptEnabled = true
        mWebView!!.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        mWebView!!.settings.loadsImagesAutomatically = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR_MR1) {
            mWebView!!.settings.loadWithOverviewMode = true
        }
        mWebView!!.settings.useWideViewPort = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
            mWebView!!.settings.builtInZoomControls = false
        }
        mWebView!!.settings.domStorageEnabled = true
        mWebView!!.webChromeClient = WebChromeClient()
        mWebView!!.webViewClient = CustomWebViewClientv2()

        //WebView.setWebContentsDebuggingEnabled(true);
    }

    inner class CustomWebViewClientv2 : WebViewClient() {

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            progressBar!!.setVisibility(View.VISIBLE)
            this@WebViewMainActivity.progressBar!!.progress = 0
            super.onPageStarted(view, url, favicon)

            if (url!!.contains("intro.do")) {
                finish()
            }
        }

        override fun onPageFinished(webview: WebView?, url: String?) {
            progressBar!!.setVisibility(View.GONE)
            this@WebViewMainActivity.progressBar!!.setProgress(100)
            var userLogged: User? = null

            super.onPageFinished(webview, url)
        }

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            // Intecept Data Valiables objects
            if (url.contains("www.waze.com")) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                } catch (ex: ActivityNotFoundException) {
                    // If Waze is not installed, open it in Google Play:
                    val intent =
                        Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.waze"))
                    startActivity(intent)
                }
                view.stopLoading()
            }

            // Share by www.google.com
            if (url.contains("www.google.com/maps?")) {
                try {
                    var intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    intent.setPackage("com.google.android.apps.maps")
                    startActivity(intent)
                } catch (ex : ActivityNotFoundException){
                    val intent =
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.maps&hl=pt_BR&gl=US"))
                    startActivity(intent)
                }

                view.stopLoading()
            }

            // Share by maps.google.com
            if (url.contains("maps.google.com")) {
                try {
                    var intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    intent.setPackage("com.google.android.apps.maps")
                    startActivity(intent)
                } catch (ex : ActivityNotFoundException){
                    val intent =
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.maps&hl=pt_BR&gl=US"))
                    startActivity(intent)
                }
            }
            return true
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    // tela do token: tipoToken.do
    // tela de validar do token: validaDadosToken
    // tela que gera o token: geratoken.do

    // cadastro de veiculos: cadVeiculo.do
    // minhas ofertas: ofertas.do
    // cadastro: cadastro_V2.do
    // extrato relCompras.do

    // mensagens: historicoPush.do
    //endereços: enderecosDuque.php
    //contato: faleConosco.do

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}
