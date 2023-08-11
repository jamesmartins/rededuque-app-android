package br.com.rededuque.android

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.*
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import br.com.rededuque.android.extensions.toBase64
import br.com.rededuque.android.model.UrlServer
import br.com.rededuque.android.model.User
import br.com.rededuque.android.parse.Json
import br.com.rededuque.android.services.HttpClient
import br.com.rededuque.android.services.HttpClientWeb
import br.com.rededuque.android.utils.*
import com.android.volley.Request
import com.google.android.material.snackbar.Snackbar
import com.onesignal.*
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONObject
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URL
import java.net.URLDecoder
import java.util.concurrent.Executor
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

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

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

    inner class CustomWebViewClientv2 : WebViewClient() {

        private var cookies: String? = null
        private var user: User? = null
        private lateinit var executor: Executor
        private lateinit var biometricPrompt: BiometricPrompt
        private lateinit var promptInfo: BiometricPrompt.PromptInfo
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

        private fun splitSearchStrFromQueryUrl(url : String, key: String): String? {
             var url = url.toHttpUrlOrNull()
            return if (url != null) {
                url.queryParameter(key)
            } else null
        }

        private fun saveAuthCookies(login: String?, passwd: String?) {
            if (login != null && passwd != null) {
                Utils.saveToPreference(applicationContext, "emailSAVED", login.trim { it <= ' ' })
                Utils.saveToPreference(applicationContext, "passwdSAVED", passwd.trim { it <= ' ' })
            }
        }

        private fun saveAuthIDLToken(idlToken: String?) {
            if (idlToken != null) {
                Utils.saveToPreference(applicationContext, "tokenSAVED", idlToken.trim { it <= ' ' })
            }
        }

        private fun processRedeDuqueUrlKey(keyValue : String, companyId: Int = PROJECT_ID, completion: (success: Boolean, user: User?) -> Unit) {
            val postparams = Json.getLoggedUser(keyValue.toBase64(), companyId)

            HttpClient.getInstance.postAsync(Request.Method.POST, mUrlUserSearchKeyData, postparams, object : Callback, okhttp3.Callback {

                override fun onFailure(call: Call, e: IOException) {
                    completion(false,  User())
                    Log.d(this::class.simpleName, "Error Comunication")
                }

                override fun onResponse(call: Call, response: okhttp3.Response) {
                    if (response.isSuccessful && response.code == 200) {
                        //get data user from idU Key
                        var userResult = response.peekBody(2048).string()
                        if (userResult.isNotEmpty() && userResult.isNotBlank()){
                            val obj = JSONObject(userResult)
                            if (obj.has("RD_userId")) {
                                var userLogged = Json.toUser(userResult)
                                completion(true, userLogged)
                            } else {
                                completion(false, User())
                                Log.d("Error_Message","Aconteceu algum problema de dados da RedeDuque...")
                            }
                        } else {
                            completion(false, User())
                            Log.d("Error_Message","Aconteceu algum problema de dados da RedeDuque...")
                        }
                    } else {
                        completion(false, User())
                        Log.d(getString(R.string.Error_With_RedeDuque),"Aconteceu algum problema na conexão...")
                    }
                }
            })
        }

        private fun sendOneSignalDataToRedeDuque(userLogged : User, completion: (success: Boolean) -> Unit) {
            val postparams = Json.getUserOneSignalData(userLogged)

            HttpClient.getInstance.postAsync(Request.Method.POST, mUrlUserPushDataInformation, postparams, object : Callback, okhttp3.Callback {

                override fun onFailure(call: Call, e: IOException) {
                    completion(false)
                    Log.d(this::class.simpleName, "Error Comunication")
                }

                override fun onResponse(call: Call, response: okhttp3.Response) {
                    if (response.isSuccessful && response.code == 200) {
                        //get data user from idU Key
                        Log.d(getString(R.string.Success_To_RedeDuque),"Enviados dados OneSignal com sucesso...")
                        completion(true)

                    } else {
                        completion(false)
                        Log.d(getString(R.string.Error_With_RedeDuque),"Aconteceu algum problema na conexão...")
                    }
                }
            })
        }

        private fun promptInfo() {
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Entre com sua biometria")
                .setSubtitle("Faça Login usando seuas  ")
                .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
                .build()

            val executor = ContextCompat.getMainExecutor(this@MainActivity)

            val biometricPrompt = BiometricPrompt(this@MainActivity, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        // Autenticação biométrica bem-sucedida
                        Toast.makeText(applicationContext, "Authentication succeeded!", Toast.LENGTH_SHORT).show()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        Toast.makeText(applicationContext, "Authentication Error!", Toast.LENGTH_SHORT).show()
                    }

                    override fun onAuthenticationFailed() {
                        Toast.makeText(applicationContext, "Authentication Failed!", Toast.LENGTH_SHORT).show()
                    }
                })

            biometricPrompt.authenticate(promptInfo)
        }

        private fun initBiometric() {
            val biometricManager = BiometricManager.from(this@MainActivity)
            return when (biometricManager.canAuthenticate()) {
                BiometricManager.BIOMETRIC_SUCCESS -> {
                    promptInfo()
                }
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                    // O dispositivo não possui hardware de autenticação biométrica.
                }
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                    // O hardware de autenticação biométrica não está disponível no momento.
                }
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    // Não há impressões digitais/faces cadastradas no dispositivo.
                }
                BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                }
                BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                }
                BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                }
                else -> {}
            }
        }

        private fun initBiometricV2(): Boolean {
            val biometricManager = BiometricManager.from(this@MainActivity)
            return when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
                0 -> true
                else -> false
            }
        }

        private fun initBiometricV3(): Boolean {
            val biometricManager = BiometricManager.from(this@MainActivity)
            return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
                0 -> true
                else -> false
            }
        }        

        override fun onPageFinished(webview: WebView?, url: String?) {
            progressBar!!.setVisibility(View.GONE)
            this@MainActivity.progressBar!!.setProgress(100)
            var userLogged: User? = null

            // Status User Logged
            if (url!!.contains("novoMenu.do") or url!!.contains("cadastro_V2.do")) {
                // Get RedeDuque Personal User Logged data
                var IDUkey = splitSearchStrFromQueryUrl(url, "idU")
                if (!IDUkey.isNullOrBlank()){
                    processRedeDuqueUrlKey(IDUkey!!, completion = { success: Boolean, user: User? ->
                        if (success){
                            userLogged = user!!

                            // Get OneSignal data
                            var deviceState = OneSignal.getDeviceState()
                            deviceState.let {
                                userLogged!!.RD_TokenCelular = deviceState?.pushToken
                                userLogged!!.RD_User_Player_Id = deviceState?.userId
                            }

                            //Get Authentication Cookies Data
                            val emailCookie = userLogged!!.RD_userMail
                            val passwdCookie = userLogged!!.RD_userpass

                            //Save Auth Cookies
                            saveAuthCookies(emailCookie, passwdCookie)

                            //Send OenSignal Data to RedeDuque
                            sendOneSignalDataToRedeDuque(userLogged!!, completion = {
                                if (it) Log.d(getString(R.string.Data_Sent_to_RedeDuque), "Dados OneSignal Enviados para Rede Duque!")
                            })
                        }
                    })
                }

                // Get RedeDuque Login User token data
                var IDLkey = splitSearchStrFromQueryUrl(url, "idL")
                if (!IDLkey.isNullOrBlank()){
                    //Save Auth Cookies
                    saveAuthIDLToken(IDLkey)
                }
            }

            // Logon View - Before Logon
            if (url.contains("app.do") and !url.contains("idL=")) {
                // Restore Authentication token data
                var idlToken = Utils.readFromPreferences(applicationContext, "tokenSAVED","")
                if (!idlToken.isNullOrBlank()){
                    //Adding idL token inside URL
                    webview!!.stopLoading()
                    var urlString = "$url&idL=$idlToken".toHttpUrlOrNull()
                    webview.loadUrl(urlString.toString())

                    // Biometric request
                    if (initBiometricV2()){
                        Toast.makeText(applicationContext, "Biometria Positiva", Toast.LENGTH_SHORT).show()

                        webview.evaluateJavascript("javascript:login()", ValueCallback{
                            Log.d("Login dipastched...", "Login");
                        })

//                        webview.evaluateJavascript("(function() { \$('#btLogin').click(); return 'test'; })();",
//                            ValueCallback<String?> { s ->
//                                Log.d("LogName", s!!) // Prints the string 'null' NOT Java null
//                            })
                    } else {
//                        webview!!.stopLoading()
//                        var urlString = "$url&idL=$idlToken".toHttpUrlOrNull()
//                        webview.loadUrl(urlString.toString())
                    }
                }
            }
            super.onPageFinished(webview, url)
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

    override fun onStop() {
        super.onStop()
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
