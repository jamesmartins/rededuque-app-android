package br.com.marka.android.riobel.services

import okhttp3.*
import java.io.IOException


class HttpClientWeb{

    private val client = OkHttpClient()

    val JSON = MediaType.parse("application/json; charset=utf-8")
    companion object {
        val JSONType = MediaType.parse("application/json; charset=utf-8")
    }

    @Throws(IOException::class)
    operator fun get(url: String): String {
        val request = Request.Builder()
            .url(url)
            .build()

        val response = client.newCall(request).execute()
        return response.body()!!.string()
    }

    @Throws(IOException::class)
    fun post(url: String, json: String): String {
        val body = RequestBody.create(JSON, json)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()
        val response = client.newCall(request).execute()
        return response.body()!!.string()
    }

    
//    fun postAsync(method: Int, url: String, json: String, callback: Callback): Call {
//        val body = json.toRequestBody(JSON)
//        val request = Request.Builder()
//            .method(method.toString(), body)
//            .url(url)
//            .post(body)
//            .build()
//        val call = client.newCall(request)
//        call.enqueue(callback)
//        return call
//    }

    @Throws(IOException::class)
    fun postWithHeaders(url : String, authorizationCode: String, cookie : String): String{
        val client = client.newBuilder().build()
        val mediaType = MediaType.parse("text/plain")
        val body = RequestBody.create(mediaType, "")
        val request = Request.Builder()
            .url(url)
            .method("POST", body)
            .addHeader("authorizationCode", authorizationCode)
            .addHeader("Cookie", cookie)
            .build()
        val response = client.newCall(request).execute()
        return response.body()!!.string()
    }
}