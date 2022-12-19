package br.com.rededuque.android
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.*
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.marka.android.riobel.services.HttpClientWeb
import br.com.rededuque.android.model.UrlServer
import br.com.rededuque.android.model.User
import br.com.rededuque.android.parse.Json
import br.com.rededuque.android.utils.*
import com.google.android.material.snackbar.Snackbar
import com.onesignal.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import com.android.volley.DefaultRetryPolicy
import com.android.volley.toolbox.JsonObjectRequest
import br.com.rededuque.android.extensions.toast

class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName

    private var mWebView: WebView? = null
    private var progressBar: ProgressBar? = null
    private var alarmManager: AlarmManager? = null
    internal var stop: Button? = null
    private var pendingIntent: PendingIntent? = null
    private var userId: String? = null
    private var mUrlServer: UrlServer? = null
    private val okHttpClient = OkHttpClient()
    private val okHttpCustonClient = HttpClientWeb()
    private var latitude : String? = null
    private var longitude : String? = null

    /**
     * Control States:
     * False -> Consumo de webservice para descobrir a url
     * True -> Url estática
     */
    private val ACCESSEDSTATICURL = true
    private var isConnected = false
    private var mAlreadyStartedService = false

    /**
     * Code used in requesting runtime permissions
     */
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34

    //Context Push Notifications Variables
    private var deviceState : OSDeviceState? = null
    private var userOneSignalID : String? = null
    private var pushDeviceToken : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        isConnected = Utils.isNetworkConnected(applicationContext)

        // Load Views
        initViews()

        // Load Url
        doLoadRequest(ACCESSEDSTATICURL)

    }

    override fun onResume() {
        super.onResume()
        //Get OneSignal Device Data Push Notifications
        deviceState = OneSignal.getDeviceState()

        Log.d("Create...", "info push colected...")
    }

    /**
     * Shows a [Snackbar].
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private fun showSnackbar(mainTextStringId: Int, actionStringId: Int, listener: View.OnClickListener) {
        Snackbar.make(findViewById(android.R.id.content),
            getString(mainTextStringId),
            Snackbar.LENGTH_INDEFINITE).setAction(getString(actionStringId), listener).show()
    }


    /**
     * Return the availability of GooglePlayServices
     */
//    private fun isGooglePlayServicesAvailable(): Boolean {
//        val googleApiAvailability = GoogleApiAvailability.getInstance()
//        val status = googleApiAvailability.isGooglePlayServicesAvailable(this)
//        if (status != ConnectionResult.SUCCESS) {
//            if (googleApiAvailability.isUserResolvableError(status)) {
//                googleApiAvailability.getErrorDialog(this, status, 2404).show()
//            }
//            return false
//        }
//        return true
//    }

    private fun doLoadRequest(explicityUrl: Boolean) {
        if (!isConnected) {
            Toast.makeText(this, "Falta de Conexão!", Toast.LENGTH_SHORT).show()
            return
        }

        if (explicityUrl) {
            loadContent(mUrlStatic)
        }
    }

//    private fun doRequestFromUrl() {
//        val url = "http://adm.bunker.mk/wsjson/url_dinamico.do"
//        val isConnected = Utils.isNetworkConnected(applicationContext)
//        if (!isConnected) {
//            Toast.makeText(this, "Falta de Conexão!", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        val request = Request.Builder.url(url).build()
//
//        okHttpClient.newCall(request)
//            .enqueue(object : Callback {
//                override fun onFailure(call: Call, e: IOException) {
//                    // Error
//                    runOnUiThread {
////                        Toast.makeText(this@MainActivity, "Erro de Comunicação!", Toast.LENGTH_SHORT).show();
//                    }
//                }
//
//                @Throws(IOException::class)
//                override fun onResponse(call: Call, response: Response) {
//                    val result = response.body().string()
//                    mUrlServer = Json.toUrlServer(result)
//
//                    runOnUiThread { mUrlServer!!.urlDefault?.let { loadContent(it) } }
//                }
//            })
//    }

//    fun doSendGetRequest(method: Int, url: String, json: String) {
//        Log.d(TAG, "Sending Settings to Duque Server...")
//        val body = RequestBody.create(HttpClientWeb.JSONType, json)
//        val isConnected = Utils.isNetworkConnected(applicationContext)
//
//        if (!isConnected) {
//            Toast.makeText(this, "Falta de Conexão!", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        val request = Request.Builder.url(url).post(body).build()
//
//        okHttpClient.newCall(request).enqueue(object : Callback {
//                override fun onFailure(call: Call, e: IOException) {
//                    // Error
//                    runOnUiThread {
//                        //Toast.makeText(MainActivity.this, "Erro de Comunicação!", Toast.LENGTH_SHORT).show();
//                    }
//                }
//
//                @Throws(IOException::class)
//                override fun onResponse(call: Call, response: Response) {
//                    //TO DO
//                }
//            })
//    }


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
        mWebView!!.settings.loadWithOverviewMode = true
        mWebView!!.settings.useWideViewPort = true
        mWebView!!.settings.builtInZoomControls = false

        mWebView!!.webChromeClient = WebChromeClient()
        mWebView!!.webViewClient = CustomWebViewClientv2()

        //WebView.setWebContentsDebuggingEnabled(true);
    }

    override fun onPointerCaptureChanged(hasCapture: Boolean) {}

    inner class CustomWebViewClient : WebViewClient() {

        private var cookies: String? = null
        private var user: User? = null
        private val userObj: JSONObject? = null

        private fun getCookie(url: String, cookieName: String): String? {
            var CookieValue: String? = null

            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cookieManager.setAcceptThirdPartyCookies(mWebView, true)
            }

            val cookies = cookieManager.getCookie(url)
            val temp = cookies.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (ar1 in temp) {
                if (ar1.trim { it <= ' ' }.indexOf(cookieName) == 0) {
                    val temp1 = ar1.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    CookieValue = temp1[1]
                    break
                }
            }
            return CookieValue
        }

        private fun setAuthCookies(url: String) {

            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cookieManager.setAcceptThirdPartyCookies(mWebView, true)
            }

            val loginValue = Utils.readFromPreferences(applicationContext, "LoginSAVED", "")
            val passwdValue = Utils.readFromPreferences(applicationContext, "passwdSAVED", "")

            val cookieLogin = "login=" + loginValue!!
            val cookiePasswd = "senha=" + passwdValue!!
            cookieManager.setCookie(url, cookieLogin)
            cookieManager.setCookie(url, cookiePasswd)
        }

        // Manipulate Custom REDEDUQUE Cookie
        private fun handleRedeDuqueCookie(cookies: String?) {
            var cookies = cookies
            var mUser : User? = null
            try {
                if (cookies != null) {
                    cookies = URLDecoder.decode(cookies, "UTF-8")
                    mUser = Json.toUser(cookies!!)
                }
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }

            if (mUser != null) {
                OneSignal.sendTags(mUser!!.jsonObject)
                if (!mUser!!.RD_userMail!!.isEmpty() && mUser!!.RD_userMail!!.length > 0)
                    OneSignal.setEmail(user!!.RD_userMail!!)

                Utils.saveToPreference(applicationContext, "UserSAVED", mUser!!.RD_userId!!)
                Log.i("OneSignal", "Tags Sent.......")
            } else {
                Log.i("OneSignal", "Fail Tags Sent......: User NULL")
            }
            user = mUser

        }

        private fun saveAuthCookies(login: String?, passwd: String?) {
            if (login != null && passwd != null) {
                Utils.saveToPreference(applicationContext, "LoginSAVED", login.trim { it <= ' ' })
                Utils.saveToPreference(applicationContext, "passwdSAVED", passwd.trim { it <= ' ' })
            }
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            progressBar!!.setVisibility(View.GONE)
            this@MainActivity.progressBar!!.setProgress(100)

            // Status Logged
            if (url!!.contains("log=1")) {

                //Manipulate Cookie REDE_DUQUE for send data to Rede Duque Servers
                cookies = getCookie(url, "REDE_DUQUE")
                handleRedeDuqueCookie(cookies)

                //Get Authentication Cookies Data
                val loginCookie = getCookie(url, "login")
                val passwdCookie = getCookie(url, "senha")

                //Save Auth Cookies
                saveAuthCookies(loginCookie, passwdCookie)
            }

            // Logon View - Before Logon
            if (url.contains(mUrlStatic + "novoLogin.do")) {
                // Restore Authentication Cookies
                setAuthCookies(url)
            }

            super.onPageFinished(view, url)
        }


        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {

            view.loadUrl(url)

            // Intecept Data Valiables objects
            if (url.contains("waze://")) {
                var intent = Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url))
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url)))
                } else {
                    intent =
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.waze"))
                    startActivity(intent)
                }
                view.stopLoading()
            }
            //  https://maps.google.com/
            if (url.contains("maps.google.com")) {
                var intent = Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url))
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url)))
                } else {
                    intent =
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.waze"))
                    startActivity(intent)
                }
                view.stopLoading()
            }

            return true
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            progressBar!!.setVisibility(View.VISIBLE)
            this@MainActivity.progressBar!!.progress = 0
            super.onPageStarted(view, url, favicon)
        }
    }

    inner class CustomWebViewClientv2 : WebViewClient() {

        private var cookies: String? = null
        private var user: User? = null

//        @Throws(UnsupportedEncodingException::class)
//        fun splitQuery(url: URL): Map<String, String>? {
//            val query_pairs: MutableMap<String, String> = LinkedHashMap()
//            val query: String = url.getQuery()
//            val pairs = query.split("&").toTypedArray()
//            for (pair in pairs) {
//                val idx = pair.indexOf("=")
//                query_pairs[URLDecoder.decode(pair.substring(0, idx), "UTF-8")] =
//                    URLDecoder.decode(pair.substring(idx + 1), "UTF-8")
//            }
//            return query_pairs
//        }

        private fun splitQueryUrl(url : String): String? {
             var url = HttpUrl.parse(url);
             if (url != null) {
                 return url.queryParameter("idC")
             } else return null
        }

        private fun getCookie(url: String, cookieName: String): String? {
            var CookieValue: String? = null

            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cookieManager.setAcceptThirdPartyCookies(mWebView, true)
            }

            val cookies = cookieManager.getCookie(url)

            val temp = cookies.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            while (temp[1] == cookieName) {
                for (ar1 in temp) {
                    if (ar1.trim { it <= ' ' }.indexOf(cookieName) == 0) {
                        val temp1 =
                            ar1.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        CookieValue = temp1[1]
                        break
                    }
                }
            }
            return CookieValue
        }

        private fun setAuthCookies(url: String) {

            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cookieManager.setAcceptThirdPartyCookies(mWebView, true)
            }

            val loginValue = Utils.readFromPreferences(applicationContext, "LoginSAVED", "")
            val passwdValue = Utils.readFromPreferences(applicationContext, "passwdSAVED", "")

            val cookieLogin = "login=" + loginValue!!
            val cookiePasswd = "senha=" + passwdValue!!
            cookieManager.setCookie(url, cookieLogin)
            cookieManager.setCookie(url, cookiePasswd)
        }

        private fun processRedeDuqueUrlKey2(keyValue : String, companyId: Int = 19, completion: (success: Boolean) -> Unit) {
            val postparams = JSONObject()
            postparams.put("RD_userId", keyValue)
            postparams.put("RD_userCompany", companyId)

//            val request = JsonObjectRequest(Request.Method.POST, mUrlUserSearchKeyData, postparams, Response.Listener { response ->
//                if (response == null) {
//                    toast("Houve algum problema na conexão. Tente novamente!")
//                    completion(false)
//                    return@Listener
//                }
//
//                val results = response.getJSONArray("results")
//                val items =
//                    Gson().fromJson<List<Product>>(
//                        results.toString(),
//                        object : TypeToken<List<Product>>() {}.type
//                    )
//
//                for (item in items) {
//                    if (item.title.isNotEmpty()) {
//                        item.titleNormalizer = SettingsUtil.removeAccents(item.title)
//                    }
//                    item.authorNameNormalizer = SettingsUtil.removeAccents(item.author_name)
//                    Repository.setProductSeachedSelected(item)
//                }
//
//                mList = items as ArrayList<Product>?
//
//                completion(true)
//            },
//                Response.ErrorListener { error ->
//                    // TODO: Handle error
//                    toast("Error: " + error.message)
//                    completion(false)
//                }
//            )
//
//            requestAllProdutos.retryPolicy = DefaultRetryPolicy(
//                0,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
//            )
//
//            requestAllProdutos.tag = Constants.SEARCH_ALL_PRODUTOS_TAG
//            MainApp.getInstance()!!.addToRequestQueue(requestAllProdutos)
      }

        private fun processRedeDuqueUrlKey(keyValue : String): User? {
            var mUser: User? = null
            var response: String? = null

                response = okHttpCustonClient.post(mUrlUserSearchData, Json.getLoggedUser(keyValue))

            try {
                if (response != null) {
                    mUser = Json.toUser(response!!)
                }
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
            return mUser
        }

        // Manipulate Custom REDEDUQUE Cookie
        private fun processRedeDuqueCookie(cookies: String?) : User? {
            var cookies = cookies
            var mUser: User? = null
            try {
                if (cookies != null) {
                    cookies = URLDecoder.decode(cookies, "UTF-8")
                    mUser = Json.toUser(cookies!!)
                }
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }

            if (mUser != null) {
                OneSignal.sendTags(mUser!!.jsonObject)
                if (!mUser!!.RD_userMail!!.isEmpty() && mUser!!.RD_userMail!!.length > 0)
                    OneSignal.setEmail(mUser!!.RD_userMail!!)

                Utils.saveToPreference(applicationContext, "UserSAVED", mUser!!.RD_userId!!)
                Log.i("OneSignal", "Tags Sent.......")
            } else {
                Log.i("OneSignal", "Fail Tags Sent......: User NULL")
            }
            user = mUser
            return mUser
        }

        private fun saveAuthCookies(login: String?, passwd: String?) {
            if (login != null && passwd != null) {
                Utils.saveToPreference(applicationContext, "LoginSAVED", login.trim { it <= ' ' })
                Utils.saveToPreference(applicationContext, "passwdSAVED", passwd.trim { it <= ' ' })
            }
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            progressBar!!.setVisibility(View.GONE)
            this@MainActivity.progressBar!!.setProgress(100)
            var response : String? = null

            // Status Logged
            if (url!!.contains("novoMenu.do")) {
                //Manipulate Cookie REDE_DUQUE for send data to Rede Duque Servers
                //cookies = getCookie(url, "REDE_DUQUE")
                //cookies = getCookie(url, "login")
                //cookies1 = getCookie(url, "senha")
                var keyUserID = splitQueryUrl(url)
                var userLogged = processRedeDuqueUrlKey(keyUserID!!)

                //Verify If First Time Loggon
                var userFirstLogged = Utils.readFromPreferences(applicationContext, FIRST_LOGIN_DONE, false)
                if (userFirstLogged!!) {
                    userOneSignalID = deviceState!!.userId
                    pushDeviceToken = deviceState!!.pushToken

                    userLogged!!.pushToken = pushDeviceToken
                    runOnUiThread {
                        response = okHttpCustonClient.post(mUrlUserPushDataInformation, userLogged!!.jsonObject.toString())
                    }
                    Log.d("response user push", response!!)
                }

                //Get Authentication Cookies Data
                val loginCookie = getCookie(url, "login")
                val passwdCookie = getCookie(url, "senha")

                //Save Auth Cookies
                saveAuthCookies(loginCookie, passwdCookie)
            }

            // Logon View - Before Logon
            if (url.contains("app.do")) {
                // Restore Authentication Cookies
                setAuthCookies(url)
            }

            // Detecting address page to active location sends
            // TO DO

            super.onPageFinished(view, url)
        }


        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {

            view.loadUrl(url)
            // Intecept Data Valiables objects
            if (url.contains("waze://")) {
                var intent = Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url))
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url)))
                } else {
                    intent =
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.waze"))
                    startActivity(intent)
                }
                view.stopLoading()
            }
            //  https://maps.google.com/
            if (url.contains("maps.google.com")) {
                var intent = Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url))
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url)))
                } else {
                    intent =
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.waze"))
                    startActivity(intent)
                }
                view.stopLoading()
            }

            return true
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            progressBar!!.setVisibility(View.VISIBLE)
            this@MainActivity.progressBar!!.progress = 0
            super.onPageStarted(view, url, favicon)
        }
    }

    override fun onBackPressed() {
        if (mWebView!!.canGoBack()) {
            mWebView!!.goBack()
            return
        }
        // Otherwise defer to system default behavior.
        super.onBackPressed()
    }
}
