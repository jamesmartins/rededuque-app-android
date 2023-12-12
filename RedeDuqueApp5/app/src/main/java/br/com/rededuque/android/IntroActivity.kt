package br.com.rededuque.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import br.com.rededuque.android.model.ConfigApp
import br.com.rededuque.android.model.User
import br.com.rededuque.android.utils.PROJECT_ID
import br.com.rededuque.android.utils.Utils

class IntroActivity : AppCompatActivity() {
    private val TAG = IntroActivity::class.java.simpleName
    private var isConnected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        isConnected = Utils.isNetworkConnected(applicationContext)

        // Load Views
//        initViews()

        // Load Url
//        doLoadConfig(ACCESSEDSTATICURL)
    }

    fun doLoadConfig() {

    }

    private fun loadConfigFromRedeDuque(url: String,companyId: Int = PROJECT_ID, completion: (success: Boolean, configApp: ConfigApp?) -> Unit) {

    }
}