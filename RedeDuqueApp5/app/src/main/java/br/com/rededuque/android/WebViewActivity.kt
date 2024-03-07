package br.com.rededuque.android

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import br.com.rededuque.android.model.User


class WebViewActivity : AppCompatActivity() {
    private var mWebView: WebView? = null
    private var progressBar: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        initViews()
    }

    private fun initViews() {
        progressBar = findViewById(R.id.progress)
        mWebView = findViewById(R.id.mwebview)
        mWebView!!.settings.javaScriptEnabled = true
        mWebView!!.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        mWebView!!.settings.loadsImagesAutomatically = true
        mWebView!!.settings.loadWithOverviewMode = true
        mWebView!!.settings.useWideViewPort = true
        mWebView!!.settings.builtInZoomControls = false
        mWebView!!.webChromeClient = WebChromeClient()
        mWebView!!.webViewClient = CustomWebViewClientv2()

        //WebView.setWebContentsDebuggingEnabled(true);
    }

    inner class CustomWebViewClientv2 : WebViewClient() {

        override fun onPageFinished(webview: WebView?, url: String?) {
            progressBar!!.setVisibility(View.GONE)
            this@WebViewActivity.progressBar!!.setProgress(100)
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

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            progressBar!!.setVisibility(View.VISIBLE)
            this@WebViewActivity.progressBar!!.progress = 0
            super.onPageStarted(view, url, favicon)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

}

