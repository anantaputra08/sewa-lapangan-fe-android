package com.example.booking_lapangan.api

import android.content.Context
import com.android.volley.Response
import com.android.volley.Request
import com.example.booking_lapangan.SessionManager
import com.google.gson.Gson
import org.json.JSONObject

object AdminUserApi {
    private val gson = Gson()

    fun getUsers(
        context: Context,
        onSuccess: (List<User>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val sessionManager = SessionManager(context)
        val token = sessionManager.fetchAuthToken()
        if (token == null) {
            onError(Exception("Admin not authenticated")); return
        }

        val endpoint = "api/admin/users"
        val headers = mapOf("Authorization" to "Bearer $token")

        VolleyClient.get(endpoint, headers,
            onSuccess = { responseString ->
                try {
                    val response = gson.fromJson(responseString, UserListResponse::class.java)
                    if (response.success) {
                        onSuccess(response.data)
                    } else {
                        onError(Exception("Failed to fetch users"))
                    }
                } catch (e: Exception) {
                    onError(e)
                }
            },
            onError = onError
        )
    }

    fun createUser(
        context: Context,
        userData: Map<String, String>,
        photoData: VolleyMultipartRequest.DataPart?,
        onSuccess: (String) -> Unit, onError: (Exception) -> Unit
    ) {
        handleUserAction(
            context, "api/admin/users",
            Request.Method.POST,
            null,
            userData,
            photoData,
            onSuccess,
            onError
        )
    }

    fun updateUser(
        context: Context,
        user: User,
        userData: Map<String, String>,
        photoData: VolleyMultipartRequest.DataPart?,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        handleUserAction(context, "api/admin/users/${user.id}", Request.Method.POST, user, userData, photoData, onSuccess, onError)
    }

    fun deleteUser(context: Context, user: User, onSuccess: (String) -> Unit, onError: (Exception) -> Unit) {
        val sessionManager = SessionManager(context)
        val token = sessionManager.fetchAuthToken()
        if (token == null) { onError(Exception("Admin not authenticated")); return }

        val endpoint = "api/admin/users/${user.id}"
        val headers = mapOf("Authorization" to "Bearer $token")

        VolleyClient.delete(endpoint, headers,
            onSuccess = { responseString ->
                try {
                    val response = JSONObject(responseString)
                    onSuccess(response.getString("message"))
                } catch (e: Exception) { onError(e) }
            },
            onError = onError)
    }

    private fun handleUserAction(context: Context, endpoint: String, method: Int, user: User?, userData: Map<String, String>, photoData: VolleyMultipartRequest.DataPart?, onSuccess: (String) -> Unit, onError: (Exception) -> Unit) {
        val sessionManager = SessionManager(context)
        val token = sessionManager.fetchAuthToken()
        if (token == null) { onError(Exception("Admin not authenticated")); return }

        val headers = mapOf("Authorization" to "Bearer $token")
        val fileParams = photoData?.let { mapOf("photo" to it) }

        val multipartRequest = VolleyMultipartRequest(method, VolleyClient.BASE_URL + endpoint,
            Response.Listener { networkResponse ->
                try {
                    val responseString = String(networkResponse.data)
                    val response = JSONObject(responseString)
                    if (response.getBoolean("success")) {
                        onSuccess(response.getString("message"))
                    } else {
                        val errors = response.optJSONObject("errors")?.toString() ?: "Unknown error"
                        onError(Exception("Validation failed: $errors"))
                    }
                } catch (e: Exception) { onError(e) }
            },
            Response.ErrorListener { error -> onError(Exception(error.message)) },
            headers = headers,
            params = userData,
            fileParams = fileParams
        )
        VolleyClient.requestQueue?.add(multipartRequest)
    }
}
data class UserListResponse(val success: Boolean, val data: List<User>)
