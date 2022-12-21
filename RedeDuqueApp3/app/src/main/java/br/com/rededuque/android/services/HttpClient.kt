package br.com.rededuque.android.services

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class HttpClient {

    private var client: OkHttpClient = OkHttpClient()

    companion object {
        val getInstance: HttpClient by lazy { HolderLazy.INSTANCE }
        val JSON: MediaType = "application/json; charset=utf-8".toMediaTypeOrNull()!!
    }

    private object HolderLazy {
        val INSTANCE = HttpClient()
    }

    fun postAsync(method: Int, url: String, json: String, callback: Callback): Call {
        val body = json.toRequestBody(JSON)
        val request = Request.Builder()
            .method(method.toString(), body)
            .url(url)
            .post(body)
            .build()
        val call = client.newCall(request)
        call.enqueue(callback)
        return call
    }
}