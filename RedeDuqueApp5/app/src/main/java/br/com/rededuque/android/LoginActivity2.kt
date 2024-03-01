package br.com.rededuque.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.lifecycle.LiveData
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText

class LoginActivity2 : AppCompatActivity() {

    var btnLogin: Button? = null
    var editLogin: TextInputEditText? = null
    var editPasswd: TextInputEditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login2)

        initViews()

        btnLogin!!.setOnClickListener {
            var login = editLogin!!.text.toString().trim()
            var passwd = editPasswd!!.text.toString().trim()
            if (login.length > 11 || login.length < 11 )

                return

        }
    }

    fun initViews(){
        var btnManterDadosLogin = findViewById<SwitchMaterial>(R.id.txtCheckLogin)
        btnManterDadosLogin.isChecked = true
        editLogin = findViewById(R.id.edtLogin)
        editPasswd = findViewById(R.id.edtPasssword)
        btnLogin = findViewById(R.id.btnLogin)
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

}

