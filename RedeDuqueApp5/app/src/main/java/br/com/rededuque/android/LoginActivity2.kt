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
    }

    fun initViews(){
        var btnManterDadosLogin = findViewById<SwitchMaterial>(R.id.txtCheckLogin)
        btnManterDadosLogin.isChecked = true
        editLogin = findViewById<TextInputEditText>(R.id.edtLogin)
        editPasswd = findViewById<TextInputEditText>(R.id.edtPasssword)

    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

}

