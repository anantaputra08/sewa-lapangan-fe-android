package com.example.booking_lapangan

import android.content.Context
import android.content.SharedPreferences
import com.example.booking_lapangan.api.User
import com.google.gson.Gson

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_session", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = prefs.edit()
    private val gson = Gson()

    companion object {
        const val USER_TOKEN = "user_token"
        const val USER_DETAILS = "user_details"
    }

    /**
     * Menyimpan token otentikasi.
     */
    fun saveAuthToken(token: String) {
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }

    /**
     * Mengambil token otentikasi.
     */
    fun fetchAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    /**
     * Menyimpan detail pengguna dalam format JSON.
     */
    fun saveUserDetails(user: User) {
        val userJson = gson.toJson(user)
        editor.putString(USER_DETAILS, userJson)
        editor.apply()
    }

    /**
     * Mengambil detail pengguna.
     */
    fun fetchUserDetails(): User? {
        val userJson = prefs.getString(USER_DETAILS, null)
        return gson.fromJson(userJson, User::class.java)
    }

    /**
     * Menghapus semua data sesi (untuk logout).
     */
    fun clearSession() {
        editor.clear()
        editor.apply()
    }

    /**
     * Memeriksa apakah pengguna sudah login.
     */
    fun isLoggedIn(): Boolean {
        return fetchAuthToken() != null
    }
}
