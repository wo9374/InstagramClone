package com.example.instagramclone.util

import android.util.Log
import com.example.instagramclone.BuildConfig
import com.example.instagramclone.BuildConfig.SIGNIN_TOKEN
import com.example.instagramclone.model.PushDTO
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException

object FcmPush {
    private val TAG = FcmPush::class.simpleName

    private val url = "https://fcm.googleapis.com/fcm/send"
    private val json = "application/json; charset=utf-8".toMediaTypeOrNull()
    private val gson = Gson()
    private val httpClient = OkHttpClient()

    fun sendMessage(destinationUid: String, title: String, message: String) {
        Firebase.firestore
            .collection("pushtokens")
            .document(destinationUid)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result?.get("pushToken").toString()

                    val pushDto = PushDTO(
                        to = token,
                        notification = PushDTO.Notification(title = title, body = message)
                    )

                    val body = gson.toJson(pushDto).toRequestBody(json)
                    val request = Request.Builder()
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Authorization", "key=${BuildConfig.SERVER_KEY}")
                        .url(url)
                        .post(body)
                        .build()

                    httpClient.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            e.printStackTrace()
                        }

                        override fun onResponse(call: Call, response: Response) {
                            Log.d(TAG, "onResponse: ${response.body.string()}")
                        }
                    })
                }
            }
    }
}