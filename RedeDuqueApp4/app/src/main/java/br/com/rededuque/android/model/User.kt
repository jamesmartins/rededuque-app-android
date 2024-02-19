package br.com.rededuque.android.model

import org.json.JSONObject

class User(
    var RD_userId: String? = null,
    var RD_userCompany: String? = null,
    var RD_userMail: String? = null,
    var RD_userName: String? = null,
    var RD_userpass: String? = null,
    var RD_userType: String? = null,
    var RD_TokenCelular: String? = null,
    var RD_User_Player_Id: String? = null,
    var RD_Versao: String? = null,
    var jsonObject: JSONObject? = null) {
}