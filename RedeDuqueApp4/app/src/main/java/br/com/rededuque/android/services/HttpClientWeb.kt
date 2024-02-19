package br.com.rededuque.android.services

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException


class HttpClientWeb{

    private val client = OkHttpClient()

    val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
    companion object {
        val JSONType = "application/json; charset=utf-8".toMediaTypeOrNull()
    }

    @Throws(IOException::class)
    operator fun get(url: String): String {
        val request = Request.Builder()
            .url(url)
            .build()

        val response = client.newCall(request).execute()
        return response.body!!.string()
    }

    @Throws(IOException::class)
    fun post(url: String, json: String): String {
        val body = RequestBody.create(JSON, json)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()
        val response = client.newCall(request).execute()
        return response.body!!.string()
    }


    @Throws(IOException::class)
    fun postWithHeaders(url : String, authorizationCode: String, cookie : String): String{
        val client = client.newBuilder().build()
        val mediaType = "text/plain".toMediaTypeOrNull()
        val body = RequestBody.create(mediaType, "")
        val request = Request.Builder()
            .url(url)
            .method("POST", body)
            .addHeader("authorizationCode", authorizationCode)
            .addHeader("Cookie", cookie)
            .build()
        val response = client.newCall(request).execute()
        return response.body!!.string()
    }
}