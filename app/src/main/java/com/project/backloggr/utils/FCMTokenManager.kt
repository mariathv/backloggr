package com.project.backloggr.utils

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.messaging.FirebaseMessaging
import com.project.backloggr.BuildConfig
import org.json.JSONObject

object FCMTokenManager {
    private const val TAG = "FCMTokenManager"
    private const val PREF_NAME = "MyAppPrefs"
    private const val KEY_FCM_TOKEN = "fcm_token"
    private const val KEY_TOKEN_SENT = "fcm_token_sent"

    fun saveToken(context: Context, token: String) {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPref.edit().apply {
            putString(KEY_FCM_TOKEN, token)
            putBoolean(KEY_TOKEN_SENT, false)
            apply()
        }
        Log.d(TAG, "FCM token saved locally")
    }

    fun getToken(context: Context): String? {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPref.getString(KEY_FCM_TOKEN, null)
    }

    fun isTokenSent(context: Context): Boolean {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPref.getBoolean(KEY_TOKEN_SENT, false)
    }

    fun markTokenAsSent(context: Context) {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPref.edit().putBoolean(KEY_TOKEN_SENT, true).apply()
    }

    fun sendTokenToServer(context: Context, token: String, authToken: String?) {
        val url = "${BuildConfig.BASE_URL}api/notifications/fcm-token"

        val jsonObject = JSONObject().apply {
            put("fcm_token", token)
        }

        val request = object : JsonObjectRequest(
            Request.Method.POST,
            url,
            jsonObject,
            { response ->
                Log.d(TAG, "FCM token sent to server successfully")
                markTokenAsSent(context)
            },
            { error ->
                Log.e(TAG, "Failed to send FCM token to server", error)
            }
        ) {
            override fun getHeaders() = hashMapOf(
                "Authorization" to "Bearer $authToken",
                "Content-Type" to "application/json"
            )
        }

        Volley.newRequestQueue(context).add(request)
    }

    fun retrieveAndSendToken(context: Context, authToken: String?) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            Log.d(TAG, "FCM Token retrieved: $token")

            saveToken(context, token)
            sendTokenToServer(context, token, authToken)
        }
    }

    fun deleteTokenFromServer(context: Context, authToken: String) {
        val url = "${BuildConfig.BASE_URL}api/notifications/fcm-token"

        val request = object : JsonObjectRequest(
            Request.Method.DELETE,
            url,
            null,
            { response ->
                Log.d(TAG, "FCM token deleted from server successfully")
                // Clear local token
                val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                sharedPref.edit().apply {
                    remove(KEY_FCM_TOKEN)
                    putBoolean(KEY_TOKEN_SENT, false)
                    apply()
                }
            },
            { error ->
                Log.e(TAG, "Failed to delete FCM token from server", error)
            }
        ) {
            override fun getHeaders() = hashMapOf(
                "Authorization" to "Bearer $authToken",
                "Content-Type" to "application/json"
            )
        }

        Volley.newRequestQueue(context).add(request)
    }
}
