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
import br.com.rededuque.android.extensions.toBase64
import br.com.rededuque.android.services.HttpClientWeb
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
import br.com.rededuque.android.services.HttpClient
import com.android.volley.Request
import com.android.volley.Response
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.net.URL
import javax.security.auth.callback.Callback

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
//        //Get OneSignal Device Data Push Notifications
//        deviceState = OneSignal.getDeviceState()

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
        //Get OneSignal Device Data Push Notifications

        @Throws(UnsupportedEncodingException::class)
        fun splitQuery(url: URL): Map<String, String>? {
            val query_pairs: MutableMap<String, String> = LinkedHashMap()
            val query: String = url.getQuery()
            val pairs = query.split("&").toTypedArray()
            for (pair in pairs) {
                val idx = pair.indexOf("=")
                query_pairs[URLDecoder.decode(pair.substring(0, idx), "UTF-8")] =
                    URLDecoder.decode(pair.substring(idx + 1), "UTF-8")
            }
            return query_pairs
        }

        private fun splitQueryUrl(url : String): String? {
             var url = url.toHttpUrlOrNull()
             if (url != null) {
                 return url.queryParameter("idU")
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

        private fun processRedeDuqueUrlKey(keyValue : String, companyId: Int = PROJECT_ID, completion: (success: Boolean, user: User) -> Unit) {
            val postparams = Json.getLoggedUser(keyValue.toBase64(), companyId)

            HttpClient.getInstance.postAsync(Request.Method.POST, mUrlUserSearchKeyData, postparams, object : Callback, okhttp3.Callback {

                override fun onFailure(call: Call, e: IOException) {
                    Log.d(this::class.simpleName, "Error Comunication")
                }

                override fun onResponse(call: Call, response: okhttp3.Response) {
                    if (response.isSuccessful && response.code == 200) {
                        //get data user from idU Key
                        var userResult = response.peekBody(2048).string()
                        var userLogged = Json.toUser(userResult)
                        completion(true, userLogged)

                    } else {
                        completion(false, null!!)
                        Log.d(getString(R.string.Error_With_RedeDuque),"Aconteceu algum problema na conexão...")
                    }
                }
            })
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            progressBar!!.setVisibility(View.GONE)
            this@MainActivity.progressBar!!.setProgress(100)
            var response : String? = null


            // Status Logged
            if (url!!.contains("novoMenu.do")) {
                var userLogged: User? = null
                var deviceState = OneSignal.getDeviceState()
                var userFirstLogged = Utils.readFromPreferences(applicationContext, FIRST_LOGIN_DONE, false)
                pushDeviceToken = deviceState!!.pushToken
                userOneSignalID = deviceState!!.userId
                if (!userFirstLogged!!) {
                    var keyUserID = splitQueryUrl(url)
                    processRedeDuqueUrlKey(keyUserID!!, completion = { success: Boolean, user: User ->
                            if (success){
                                userLogged = user
                            }
                    })
                }

                //Verify If First Time Loggon
//                if (!userFirstLogged!!) {
//                    pushDeviceToken = deviceState!!.pushToken
//                    userOneSignalID = deviceState!!.userId
//
//                    userLogged!!.pushToken = pushDeviceToken
//                    runOnUiThread {
//                        response = okHttpCustonClient.post(mUrlUserPushDataInformation, userLogged!!.jsonObject.toString())
//                    }
//                    Log.d("response user push", response!!)
//                }

                //Get Authentication Cookies Data
//                val loginCookie = getCookie(url, "login")
//                val passwdCookie = getCookie(url, "senha")

                //Save Auth Cookies
//                saveAuthCookies(loginCookie, passwdCookie)
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

    private fun processRedeDuqueUrlKey2(keyValue : String, companyId: Int = 19, completion: (success: Boolean, user: User) -> Unit) {
        val postparams = JSONObject()
        postparams.put("RD_userId", keyValue)
        postparams.put("RD_userCompany", companyId)

        val requestKeyidC = JsonObjectRequest(Request.Method.POST, mUrlUserSearchKeyData, postparams, com.android.volley.Response.Listener { response ->
            if (response == null) {
                toast("Houve algum problema na conexão. Tente novamente!")
                completion(false, null!!)
                return@Listener
            }

            val user = Json.toLoggedUser(response.toString())

            completion(true, user)
        },
            Response.ErrorListener { error ->
                toast("Error: " + error.message)
                completion(false, null!!)
            }
        )

//        requestKeyidC.retryPolicy = DefaultRetryPolicy(
//            0,
//            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
//        )

        requestKeyidC.tag = VERIFY_KEY_CUSTOMER
        InitApplication.getInstance()!!.addToRequestQueue(requestKeyidC)
    }

    override fun onStop() {
        super.onStop()
        InitApplication.getInstance()!!.cancelPendingRequests(VERIFY_KEY_CUSTOMER)
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
