package br.com.rededuque.android

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import br.com.rededuque.android.helper.PermissionManager
import br.com.rededuque.android.utils.Utils
import br.com.rededuque.android.utils.baseURL
import br.com.rededuque.android.utils.mUrlCadastro
import br.com.rededuque.android.utils.mUrlFaleConosco
import br.com.rededuque.android.utils.mUrlParceiro
import com.onesignal.Continue.with
import com.onesignal.ContinueResult
import com.onesignal.OneSignal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.function.Consumer


class IntroActivity2 : AppCompatActivity() {
    private val TAG = IntroActivity2::class.java.simpleName
    private var isConnected = false
    private var txtCadastro: TextView? = null
    private var txtFaleConosco: TextView? = null
    private var txtParceiro: TextView? = null
    private val PERMISSION_REQUEST_CODE = 100
    private lateinit var permissionManager: PermissionManager

    val launcher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted){
            //Toast.makeText(this, "Notifications permission granted", Toast.LENGTH_SHORT).show()
            Log.i("Post Notifications","Notifications permission granted")
        } else {
             //Toast.makeText(
             //this, "FCM can't post notifications without POST_NOTIFICATIONS permission",
             //Toast.LENGTH_LONG
             //).show()
            Log.i("Post Notifications","Notifications not permission granted")
        }
    }

    val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) {
                // O usuário concedeu permissão de notificação
                // Você pode executar ações necessárias aqui
            } else {
                // O usuário ainda não concedeu permissão de notificação
                // Mostrar um diálogo para o usuário
                showNotificationPermissionDialog()
            }
        }

    fun checkNotificationPermission() {
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            showNotificationPermissionDialogA2()
        }
    }

    private fun showNotificationPermissionDialogA2() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Permissão de Notificação")
            .setMessage(R.string.notification_permission_body)
            .setPositiveButton("Sim") { _, _ ->
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, applicationContext.packageName)
                requestNotificationPermission.launch(intent)
            }
            .setNegativeButton("Não") { _, _ ->
                // Lidar com a recusa do usuário, se necessário
            }
            .show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro2)

        isConnected = Utils.isNetworkConnected(applicationContext)

        // Load Views
        initViews()
        askNotificationPermissionV3()
    }

    private fun askNotificationPermissionV2(){
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED){
            Log.i("Post Notifications","Notifications permission granted")
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)){
                // show rationale and then launch launcher to request permission
                showNotificationPermissionDialog()
            } else {
                // first request or forever denied case
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    checkNotificationPermission()
                }
            }
        }
    }

    private fun askNotificationPermissionV3(){
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                //       Display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
                showNotificationPermissionDialog()
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            checkNotificationPermission()
        }
    }

    private fun showNotificationPermissionDialog(){
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.notification_permission_title))
            .setMessage(getString(R.string.notification_permission_body))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            .setNegativeButton(getString(R.string.no)) { _, _ -> }
            .show()
    }

    //// ---- ////

    private fun askNotificationPermission() {
        // This is only necessary for API Level > 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED) {
                // FCM SDK (and your app) can post notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            requestPermissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Notifications permission granted", Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(
                this, "FCM can't post notifications without POST_NOTIFICATIONS permission",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun initViews(){
        var btnLoginMenu = findViewById<View>(R.id.btnLoginMenu)
        txtCadastro = findViewById(R.id.txtCadastro)
        txtFaleConosco = findViewById(R.id.txtFaleConosco)
        txtParceiro = findViewById(R.id.txtParceiro)
        btnLoginMenu.setOnClickListener {
            startActivity(Intent(applicationContext, LoginActivity2::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        // Open Cadastro page webview
        txtCadastro!!.setOnClickListener {
            var mUrl = baseURL + mUrlCadastro
            startActivity(Intent(applicationContext, WebViewActivity::class.java)
                .putExtra("URL_LOAD_CONTENT", mUrl)
                .putExtra("URL_LOAD_TITLE","Cadastro"))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        // Open Fale Conosco
        txtFaleConosco!!.setOnClickListener {
            var mUrl = baseURL + mUrlFaleConosco
            startActivity(Intent(applicationContext, WebViewActivity::class.java)
                .putExtra("URL_LOAD_CONTENT", mUrl)
                .putExtra("URL_LOAD_TITLE","Contato"))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        // Open Parceiro
        txtParceiro!!.setOnClickListener {
            var mUrl = baseURL + mUrlParceiro
            startActivity(Intent(applicationContext, WebViewActivity::class.java)
                .putExtra("URL_LOAD_CONTENT", mUrl)
                .putExtra("URL_LOAD_TITLE","Parceiro"))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }
}