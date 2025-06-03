package com.leafree.app.util

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun saveToken(token: String){
        prefs.edit().putString("access_token",token).apply()
    }

    fun getToken(): String? = prefs.getString("access_token",null)

}