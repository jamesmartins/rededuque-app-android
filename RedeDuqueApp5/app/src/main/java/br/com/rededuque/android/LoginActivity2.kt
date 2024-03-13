package br.com.rededuque.android

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import br.com.rededuque.android.extensions.isValidCPF
import br.com.rededuque.android.extensions.onlyNumbers
import br.com.rededuque.android.extensions.onlyNumbers2
import br.com.rededuque.android.extensions.toBase64
import br.com.rededuque.android.extensions.toast
import br.com.rededuque.android.model.User
import br.com.rededuque.android.model.UserAuthData
import br.com.rededuque.android.parse.Json
import br.com.rededuque.android.services.HttpClient
import br.com.rededuque.android.utils.*
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.onesignal.OneSignal
import okhttp3.Call
import org.json.JSONObject
import java.io.IOException
import javax.security.auth.callback.Callback

class LoginActivity2 : AppCompatActivity(), TextWatcher {

    private val TAG = LoginActivity2::class.java.simpleName
    var btnLogin: Button? = null
    var editLogin: AppCompatEditText? = null
    var editPasswd: AppCompatEditText? = null
    var txtRememberPassword: TextView? = null
    var txtCreateLogin: TextView? = null
    var txtCheckLogin: SwitchMaterial? = null
    private var progressBar: ProgressBar? = null
    private var isConnected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login2)

        isConnected = Utils.isNetworkConnected(applicationContext)

        initViews()

        btnLogin!!.setOnClickListener {
            var login = editLogin!!.text.toString().onlyNumbers2()
            var passwd = editPasswd!!.text.toString().trim()
            // validate
            validate(login, passwd)
            //Do login
            doLogin(login, passwd)
        }

        verifyUserSavedPass()
    }

    private fun verifyUserSavedPass(){
        if (hasDataUserSaved() && hasIduPassDataSaved()!!){
            // Biometric request
            if (initBiometricV2()){
                promptInfo(completion = {
                    if (it){
                        var userIDUUrlpass = Utils.readFromPreferences(applicationContext, "userIDUPassSaved", " ")
                        if (userIDUUrlpass != null){
                            // open activity with webview + url authenticated user pass
                            startActivity(Intent(applicationContext, WebViewActivity::class.java).putExtra("URL_LOAD_CONTENT", userIDUUrlpass.trim()))
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                        } else {
                            toast("Digite novamente seus dados do login!")
                        }
                    }
                })
            }
        }
    }

    override fun onResume() {
        super.onResume()
        readFromAuthCookies()
    }

    fun initViews(){
        var btnManterDadosLogin = findViewById<SwitchMaterial>(R.id.txtCheckLogin)
        btnManterDadosLogin.isChecked = true
        editLogin = findViewById(R.id.edtLogin)
        editLogin!!.addTextChangedListener(this)
        editPasswd = findViewById(R.id.edtPasssword)
        btnLogin = findViewById(R.id.btnLogin)
        txtRememberPassword = findViewById(R.id.txtRememberPassword)
        txtCreateLogin = findViewById(R.id.txtCreateLogin)
        txtCheckLogin = findViewById(R.id.txtCheckLogin)


        //actions
        txtRememberPassword!!.setOnClickListener {
            var mUrl = baseURL + mUrlRecuperacaoSenha
            startActivity(Intent(applicationContext, WebViewActivity::class.java).putExtra("URL_LOAD_CONTENT", mUrl))
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        txtCreateLogin!!.setOnClickListener {
            var mUrl = baseURL + mUrlCadastro
            startActivity(Intent(applicationContext, WebViewActivity::class.java).putExtra("URL_LOAD_CONTENT", mUrl))
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    private fun validate(login : String, passwd: String){
        if (login.isBlank() || login.isEmpty()){
            toast("CPF inválido!!")
            return
        }

        if (passwd.isBlank() || passwd.isEmpty()){
            toast("Campo de senha vazio ou inválida!!")
            return
        }

        if (!login.isValidCPF()) {
            toast("CPF inválido!!")
            return
        }
    }

    private fun doLogin(user : String, passwd: String) {
        if (!isConnected) {
            toast("Falta de Conexão!", Toast.LENGTH_SHORT)
            return
        }

        // saving CPF data
        if (txtCheckLogin!!.isChecked){
            saveDataUser(user, passwd)
        }

        //Do authenticate
        var userAuthLogged: UserAuthData? = null
        var userRD: User? = null
        doAuthenticate(user, passwd,  completion = { success: Boolean, user: UserAuthData? ->
            if (success){
                userAuthLogged = user

                // Get RedeDuque Login User token data
                var IDLkey = userAuthLogged!!.idL
                if (!IDLkey.isNullOrBlank()){
                    //Save Auth Token Cookies
                    saveAuthIDLToken(IDLkey)
                }

                // Get RedeDuque Personal User Logged data
                var IDUkey = userAuthLogged!!.idU
                // Verifying on Rede Duque base if exist on RD and OneSignal
                if (!IDUkey.isNullOrBlank()) {
                    processRedeDuqueUrlKey(IDUkey!!, completion = { success: Boolean, user: User? ->
                        if (success) {
                            userRD = user!!

                            // Get OneSignal data
                            var deviceState = OneSignal.getDeviceState()
                            deviceState.let {
                                userRD!!.RD_TokenCelular = deviceState?.pushToken
                                userRD!!.RD_User_Player_Id = deviceState?.userId
                            }

                            //Get Authentication Cookies Data
                            val emailCookie = userRD!!.RD_userMail
                            val passwdCookie = userRD!!.RD_userpass

                            //Save Auth Cookies
                            saveAuthCookies(emailCookie, passwdCookie)

                            //Send OenSignal Data to RedeDuque
                            sendOneSignalDataToRedeDuque(userRD!!, completion = {
                                if (it) {
                                    Log.d(getString(R.string.Data_Sent_to_RedeDuque), "Dados OneSignal Enviados para Rede Duque!")
                                    var mUrl = mUrl_NOVO_MENU + "?key=" + userAuthLogged!!.key  + "&idU=" + userAuthLogged!!.idU + "&cds=0"

                                    // save url authenticated user pass
                                    saveIduPassData(mUrl, true)

                                    // open activity with webview + url authenticated user pass
                                    startActivity(Intent(applicationContext, WebViewActivity::class.java).putExtra("URL_LOAD_CONTENT", mUrl))
                                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                                }
                            })
                        }
                    })
                }
            }
        })
    }

    private fun doAuthenticate(user: String, passwd: String, completion: (success: Boolean, user: UserAuthData?) -> Unit) {
        val postparams = Json.getAuthUser(user, passwd)

        HttpClient.getInstance.postAsync3(mUrlAuthApp, postparams, object : Callback, okhttp3.Callback {

            override fun onFailure(call: Call, e: IOException) {
                completion(false, null)
                Log.d(this::class.simpleName, "Error Comunication")
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {
                if (response.isSuccessful && response.code == 200) {
                    //get data user
                    var userData = response.peekBody(2048).string()
                    if (userData.isNotEmpty() && userData.isNotBlank()){
                        val obj = JSONObject(userData)
                        if (obj.has("cod_cliente")) {
                            var userLogged = Json.toAuthUser(userData)
                            //get data user from idU Key
                            Log.d(getString(R.string.Success_To_Login),"Login Realizado com Sucesso...")
                            completion(true, userLogged)
                        } else {
                            completion(false, UserAuthData())
                            Log.d("Error_Message","Aconteceu algum problema de dados da RedeDuque...")
                        }
                    } else {
                        completion(false, null)
                        Log.d(getString(R.string.Error_To_Login),"Aconteceu algum problema no Login...")
                    }
                }
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
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

    private fun saveAuthCookies(login: String?, passwd: String?) {
        if (login != null && passwd != null) {
            Utils.saveToPreference(applicationContext, "emailSAVED", login.trim { it <= ' ' })
            Utils.saveToPreference(applicationContext, "passwdSAVED", passwd.trim { it <= ' ' })
        }
    }

    private fun saveDataUser(CPF: String?, passwd : String?) {
        if (CPF != null && passwd != null) {
            Utils.saveToPreference(applicationContext, "cpfSAVED", CPF.trim { it <= ' ' })
            Utils.saveToPreference(applicationContext, "passwdSAVED", passwd.trim { it <= ' ' })
            Utils.saveToPreference(applicationContext, "loggedDataSAVED", true)
        }
    }

    private fun hasDataUserSaved():Boolean{
        var loggedDataUser = Utils.readFromPreferences(applicationContext, "loggedDataSAVED",false)
        return loggedDataUser!!
    }

    private fun saveIduPassData(dataPath : String?, status : Boolean?){
        if (dataPath != null && status != null) {
            Utils.saveToPreference(applicationContext, "userIDUPassSaved", dataPath)
            Utils.saveToPreference(applicationContext, "userHasIDUPass", true)
        }
    }

    private fun hasIduPassDataSaved(): Boolean? =
          Utils.readFromPreferences(applicationContext, "userHasIDUPass",false)

    private fun readFromAuthCookies() {
        var loginCPF = Utils.readFromPreferences(applicationContext, "cpfSAVED"," ")
        loginCPF = applyMask(loginCPF!!)
        editLogin!!.setText(loginCPF!!)
        var loginPasswd = Utils.readFromPreferences(applicationContext, "passwdSAVED"," ")
        editPasswd!!.setText(loginPasswd!!, TextView.BufferType.EDITABLE)
    }

    private fun saveAuthIDLToken(idlToken: String?) {
        if (idlToken != null) {
            Utils.saveToPreference(applicationContext, "tokenSAVED", idlToken.trim { it <= ' ' })
        }
    }

    private fun processRedeDuqueUrlKey(keyValue : String, companyId: Int = PROJECT_ID, completion: (success: Boolean, user: User?) -> Unit) {
        val postparams = Json.getRDLoggedUser(keyValue.toBase64(), companyId)

        HttpClient.getInstance.postAsync(mUrlUserSearchKeyData, postparams, object : Callback, okhttp3.Callback {

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
                            var userLogged = Json.toRDUser(userResult)
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

        HttpClient.getInstance.postAsync(mUrlUserPushDataInformation, postparams, object : Callback, okhttp3.Callback {

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

    private fun promptInfo(completion: (success: Boolean) -> Unit) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autenticação")
            .setSubtitle("Faça Login usando suas credenciais:")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        val executor = ContextCompat.getMainExecutor(this@LoginActivity2)

        val biometricPrompt = BiometricPrompt(this@LoginActivity2, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    // Autenticação biométrica bem-sucedida
                    Toast.makeText(applicationContext, "Autenticação com sucesso!", Toast.LENGTH_SHORT).show()
                    completion(true)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    Toast.makeText(applicationContext, "Falha da autenticação!", Toast.LENGTH_SHORT).show()
                    completion(false)
                }

                override fun onAuthenticationFailed() {
                    Toast.makeText(applicationContext, "Falha da autenticação!", Toast.LENGTH_SHORT).show()
                    completion(false)
                }
            })

        biometricPrompt.authenticate(promptInfo)
    }

    private fun initBiometricV2(): Boolean {
        val biometricManager = BiometricManager.from(this@LoginActivity2)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            0 -> true
            else -> false
        }
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

    override fun afterTextChanged(txt: Editable?) {

    }

    private fun applyMask(text: String): String {
        val formattedText = StringBuilder()
        val cpfLength = 11

        for (i in text.indices) {
            if (i < cpfLength) {
                formattedText.append(text[i])
                if (i == 2 || i == 5) {
                    formattedText.append(".")
                } else if (i == 8) {
                    formattedText.append("-")
                }
            } else {
                formattedText.append(text[i])
                if (i == 1 || i == 4) {
                    formattedText.append(".")
                } else if (i == 7) {
                    formattedText.append("/")
                } else if (i == 11) {
                    formattedText.append("-")
                }
            }
        }

        return formattedText.toString()
    }

}

