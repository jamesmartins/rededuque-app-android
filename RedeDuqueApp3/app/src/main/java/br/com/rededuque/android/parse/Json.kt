package br.com.rededuque.android.parse


import br.com.rededuque.android.model.UrlServer
import br.com.rededuque.android.model.User
import br.com.rededuque.android.utils.PROJECT_ID
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONStringer




object Json {
    fun toUser(json: String): User {
        try {
            val obj = JSONObject(json)
            val user = User()
            user.RD_userId = obj.getString("RD_userId")
            user.RD_userCompany = obj.getString("RD_userCompany")
            user.RD_userMail = obj.getString("RD_userMail")
            user.RD_userName = obj.getString("RD_userName")
            user.RD_userType = obj.getString("RD_userType")
            user.jsonObject = obj
            return user
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    fun getLoggedUser(RD_userId : String): String{
        try {
            var json = JSONStringer()
                .`object`()
                    .key("RD_userId").value(RD_userId)
                    .key("RD_userCompany").value(PROJECT_ID)
                .endObject()
                .toString()
        return json
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    fun toLoggedUser(json: String): User {
        try {
            val obj = JSONObject(json)
            val user = User()
            user.RD_userId = obj.getString("RD_userId")
            user.RD_userCompany = obj.getString("RD_userCompany")
            user.RD_userMail = obj.getString("RD_userMail")
            user.RD_userName = obj.getString("RD_userName")
            user.RD_userType = obj.getString("RD_userpass")
            user.jsonObject = obj
            return user
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    fun toJsonObject(json: String): JSONObject? {
        try {
            return JSONObject(json)
        } catch (e: JSONException) {
            e.printStackTrace()
            return null
        }
    }

    fun toUrlServer(json: String): UrlServer {
        try {
            val obj = JSONObject(json)
            val urlServer = UrlServer()
            urlServer.urlDefault = obj.getString("URL1")
            urlServer.url2 = obj.getString("URL2")
            urlServer.url3 = obj.getString("URL3")
            urlServer.url4 = obj.getString("URL4")
            return urlServer
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }

    }
}