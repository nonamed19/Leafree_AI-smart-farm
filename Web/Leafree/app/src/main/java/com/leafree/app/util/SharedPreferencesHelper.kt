package com.leafree.app.util

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SharedPreferencesHelper(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("leafree_prefs", Context.MODE_PRIVATE)

    private val gson = Gson()

    // 🔐 [자동 로그인 토큰 저장/조회]
    fun saveToken(token: String) {
        prefs.edit().putString("access_token", token).apply()
    }

    fun getToken(): String? {
        return prefs.getString("access_token", null)
    }

    fun clearToken() {
        prefs.edit().remove("access_token").apply()
    }

    // 🔔 [알림 리스트 저장/조회]
    fun saveNotificationList(notifications: List<String>) {
        val json = gson.toJson(notifications)
        prefs.edit().putString("notification_list", json).apply()
    }

    fun getNotificationList(): MutableList<String> {
        val json = prefs.getString("notification_list", null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<String>>() {}.type
            gson.fromJson(json, type)
        } else {
            mutableListOf()
        }
    }

    fun clearNotificationList() {
        prefs.edit().remove("notification_list").apply()
    }
}
