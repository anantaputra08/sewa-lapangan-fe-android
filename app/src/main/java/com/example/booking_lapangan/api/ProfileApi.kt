package com.example.booking_lapangan.api

import android.content.Context
import com.android.volley.Request
import com.android.volley.Response
import com.example.booking_lapangan.SessionManager
import com.google.gson.Gson
import org.json.JSONObject

object ProfileApi {
    private val gson = Gson()

    fun getProfile(
        context: Context,
        onSuccess: (User) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val sessionManager = SessionManager(context)
        val token = sessionManager.fetchAuthToken()

        if (token == null) {
            onError(Exception("User not authenticated"))
            return
        }

        val endpoint = "api/profile"
        val headers = mapOf("Authorization" to "Bearer $token")

        VolleyClient.get(endpoint, headers,
            onSuccess = { responseString ->
                try {
                    val response = gson.fromJson(responseString, UserResponse::class.java)
                    if (response.success) {
                        onSuccess(response.data)
                    } else {
                        onError(Exception("Failed to fetch profile"))
                    }
                } catch (e: Exception) {
                    onError(e)
                }
            },
            onError = { onError(it) }
        )
    }

    fun updateProfile(
        context: Context,
        profileData: Map<String, String>,
        onSuccess: (User) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val sessionManager = SessionManager(context)
        val token = sessionManager.fetchAuthToken()
        if (token == null) {
            onError(Exception("User not authenticated")); return
        }

        val endpoint = "api/profile"
        val headers = mapOf("Authorization" to "Bearer $token")

        // Membuat JSON body dari Map
        val params = JSONObject()
        profileData.forEach { (key, value) ->
            params.put(key, value)
        }

        VolleyClient.post(endpoint, params, headers,
            onSuccess = { responseString ->
                try {
                    val response = gson.fromJson(responseString, UserResponse::class.java)
                    // Menangani kemungkinan error validasi dari server
                    if (response.success) {
                        onSuccess(response.data)
                    } else {
                        // Jika ada pesan error dari server (misal: "email sudah ada")
                        val errorBody = JSONObject(responseString)
                        val errorMessage = errorBody.optString("message", "Update failed")
                        onError(Exception(errorMessage))
                    }
                } catch (e: Exception) {
                    onError(e)
                }
            },
            onError = { onError(it) }
        )
    }

    fun updateProfile(
        context: Context,
        profileData: Map<String, String>,
        photoData: VolleyMultipartRequest.DataPart?,
        onSuccess: (User) -> Unit,
        onError: (Exception) -> Unit
    ) {
        VolleyClient.initialize(context)
        val sessionManager = SessionManager(context)
        val token = sessionManager.fetchAuthToken()
        if (token == null) {
            onError(Exception("User not authenticated")); return
        }

        val endpoint = "api/profile"
        val headers = mapOf("Authorization" to "Bearer $token")

        // Buat file parameters
        val fileParams = photoData?.let { mapOf("photo" to it) }

        // Karena Laravel butuh _method untuk PUT/PATCH di form multipart,
        // kita kirim sebagai parameter teks
        val textParams = profileData.toMutableMap()
        textParams["_method"] = "POST" // Laravel akan membaca ini sbg method POST

        val multipartRequest = VolleyMultipartRequest(
            Request.Method.POST,
            VolleyClient.BASE_URL + endpoint,
            Response.Listener { networkResponse ->
                try {
                    val responseString = String(networkResponse.data)
                    val response = gson.fromJson(responseString, UserResponse::class.java)
                    if (response.success) {
                        onSuccess(response.data)
                    } else {
                        val errorBody = JSONObject(responseString)
                        val errorMessage = errorBody.optString("message", "Update failed")
                        onError(Exception(errorMessage))
                    }
                } catch (e: Exception) {
                    onError(e)
                }
            },
            Response.ErrorListener { error ->
                onError(Exception(error.message))
            },
            headers = headers,
            params = textParams,
            fileParams = fileParams
        )
        VolleyClient.requestQueue?.add(multipartRequest)
    }

    fun logout(
        context: Context,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val sessionManager = SessionManager(context)
        val token = sessionManager.fetchAuthToken()

        if (token == null) {
            onError(Exception("User not authenticated"))
            return
        }

        val endpoint = "api/logout"
        val headers = mapOf("Authorization" to "Bearer $token")

        VolleyClient.post(endpoint, org.json.JSONObject(), headers,
            onSuccess = {
                sessionManager.clearSession() // Hapus token dari SharedPreferences
                onSuccess()
            },
            onError = { onError(it) }
        )
    }
}


data class UserResponse(
    val success: Boolean,
    val data: User
)
