package br.com.rededuque.android

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import br.com.rededuque.android.model.ConfigApp
import br.com.rededuque.android.utils.PROJECT_ID

class SplashActivity : AppCompatActivity() {
    private val SPLASH_TIME_OUT: Long = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({
            if (!isFinishing) {
                startActivity(Intent(applicationContext, IntroActivity::class.java))
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                finish()
            }
        }, SPLASH_TIME_OUT)
    }

    private fun loadConfigFromRedeDuque(url: String, companyId: Int = PROJECT_ID, completion: (success: Boolean, configApp: ConfigApp?) -> Unit) {

    }
}