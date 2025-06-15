package com.example.booking_lapangan.api

import android.content.Context
import android.os.Parcelable
import com.example.booking_lapangan.SessionManager
import com.google.gson.Gson
import kotlinx.parcelize.Parcelize

// Data class for the response
data class LapanganResponse(
    val success: Boolean,
    val data: List<Lapangan>
)

// Data class for a single field
@Parcelize
data class Lapangan(
    val id: Int,
    val name: String,
    val category: String?,
    val description: String?,
    val price: Double,
    val photo: String?,
    val status: String,
    val is_fully_booked_on_date: Boolean
) : Parcelable

object LapanganApi {
    private val gson = Gson()

    fun getAvailableLapangans(
        context: Context,
        date: String,
        onSuccess: (List<Lapangan>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        VolleyClient.initialize(context)
        val sessionManager = SessionManager(context)
        val token = sessionManager.fetchAuthToken()

        if (token == null) {
            onError(Exception("User not authenticated"))
            return
        }

        val endpoint = "api/lapangans/available?date=$date"
        val headers = mapOf(
            "Authorization" to "Bearer $token",
            "Accept" to "application/json"
        )

        VolleyClient.get(endpoint, headers,
            onSuccess = { responseString ->
                try {
                    val response = gson.fromJson(responseString, LapanganResponse::class.java)
                    if (response.success) {
                        onSuccess(response.data)
                    } else {
                        onError(Exception("Failed to fetch data"))
                    }
                } catch (e: Exception) {
                    onError(e)
                }
            },
            onError = { error ->
                onError(error)
            }
        )
    }
}
