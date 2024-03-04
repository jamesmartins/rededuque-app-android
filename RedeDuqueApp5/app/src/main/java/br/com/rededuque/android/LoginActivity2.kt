package br.com.rededuque.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import br.com.rededuque.android.extensions.isValidCPF
import br.com.rededuque.android.extensions.toBase64
import br.com.rededuque.android.extensions.toast
import br.com.rededuque.android.model.User
import br.com.rededuque.android.parse.Json
import br.com.rededuque.android.services.HttpClient
import br.com.rededuque.android.utils.*
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import okhttp3.Call
import org.json.JSONObject
import java.io.IOException
import javax.security.auth.callback.Callback

class LoginActivity2 : AppCompatActivity() {

    private val TAG = LoginActivity2::class.java.simpleName
    var btnLogin: Button? = null
    var editLogin: TextInputEditText? = null
    var editPasswd: TextInputEditText? = null
    private var progressBar: ProgressBar? = null
    private var isConnected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login2)

        isConnected = Utils.isNetworkConnected(applicationContext)

        initViews()

        btnLogin!!.setOnClickListener {
            var login = editLogin!!.text.toString().trim()
            var passwd = editPasswd!!.text.toString().trim()

            // validate
            validate(login, passwd)

            //Do login
            doLogin(login, passwd)
        }
    }

    fun initViews(){
        var btnManterDadosLogin = findViewById<SwitchMaterial>(R.id.txtCheckLogin)
        btnManterDadosLogin.isChecked = true
        editLogin = findViewById(R.id.edtLogin)
        editPasswd = findViewById(R.id.edtPasssword)
        btnLogin = findViewById(R.id.btnLogin)
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

        if (login.isValidCPF()) {
            toast("CPF inválido!!")
            return
        }
    }

    private fun doLogin(user : String, passwd: String) {
        if (!isConnected) {
            toast("Falta de Conexão!", Toast.LENGTH_SHORT)
            return
        }

        //Do authenticate
        doAuthenticate(user, passwd, completion = {

        })
    }

    private fun doAuthenticate(user: String, passwd: String, completion: (success: Boolean) -> Unit) {
        val postparams = Json.getAuthUser(user, passwd)

        HttpClient.getInstance.postAsync(mUrlAuthApp, postparams, object : Callback, okhttp3.Callback {

            override fun onFailure(call: Call, e: IOException) {
                completion(false)
                Log.d(this::class.simpleName, "Error Comunication")
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {
                if (response.isSuccessful && response.code == 200) {
                    //get data user from idU Key
                    Log.d(getString(R.string.Success_To_Login),"Login Realizado com Sucesso...")
                    completion(true)

                } else {
                    completion(false)
                    Log.d(getString(R.string.Error_To_Login),"Aconteceu algum problema no Login...")
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

    private fun saveAuthIDLToken(idlToken: String?) {
        if (idlToken != null) {
            Utils.saveToPreference(applicationContext, "tokenSAVED", idlToken.trim { it <= ' ' })
        }
    }

    private fun processRedeDuqueUrlKey(keyValue : String, companyId: Int = PROJECT_ID, completion: (success: Boolean, user: User?) -> Unit) {
        val postparams = Json.getLoggedUser(keyValue.toBase64(), companyId)

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

}

