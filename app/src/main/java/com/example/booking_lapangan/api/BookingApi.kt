package com.example.booking_lapangan.api

import android.content.Context
import android.os.Parcelable
import com.example.booking_lapangan.SessionManager
import com.google.gson.Gson
import kotlinx.parcelize.Parcelize
import org.json.JSONArray
import org.json.JSONObject

object BookingApi {
    private val gson = Gson()

    fun checkAvailability(
        context: Context,
        lapanganId: Int,
        date: String,
        onSuccess: (List<Session>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val sessionManager = SessionManager(context)
        val token = sessionManager.fetchAuthToken()

        if (token == null) {
            onError(Exception("User not authenticated"))
            return
        }

        // Endpoint dari controller Laravel Anda
        val endpoint = "api/bookings/check-availability?lapangan_id=$lapanganId&date=$date"
        val headers = mapOf(
            "Authorization" to "Bearer $token",
            "Accept" to "application/json"
        )

        VolleyClient.get(endpoint, headers,
            onSuccess = { responseString ->
                try {
                    val response = gson.fromJson(responseString, SessionResponse::class.java)
                    if (response.success) {
                        onSuccess(response.data)
                    } else {
                        onError(Exception("Failed to fetch session availability"))
                    }
                } catch (e: Exception) {
                    onError(e) // JSON parsing error
                }
            },
            onError = { error ->
                onError(error) // Volley error
            }
        )
    }

    fun createBooking(
        context: Context,
        lapanganId: Int,
        date: String,
        sessionIds: List<Int>,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val sessionManager = SessionManager(context)
        val token = sessionManager.fetchAuthToken()

        if (token == null) {
            onError(Exception("User not authenticated"))
            return
        }

        val endpoint = "api/bookings"
        val headers = mapOf(
            "Authorization" to "Bearer $token",
            "Accept" to "application/json"
        )

        // Membuat body JSON untuk request
        val params = JSONObject().apply {
            put("lapangan_id", lapanganId)
            put("date", date)
            put("session_hours_ids", JSONArray(sessionIds))
        }

        VolleyClient.post(endpoint, params, headers,
            onSuccess = { responseString ->
                try {
                    val response = JSONObject(responseString)
                    if (response.getBoolean("success")) {
                        onSuccess(response.getString("message"))
                    } else {
                        val message = response.optString("message", "Gagal membuat booking.")
                        onError(Exception(message))
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
    fun getMyBookings(
        context: Context,
        onSuccess: (List<Booking>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val sessionManager = SessionManager(context)
        val token = sessionManager.fetchAuthToken()

        if (token == null) {
            onError(Exception("User not authenticated"))
            return
        }

        val endpoint = "api/bookings"
        val headers = mapOf(
            "Authorization" to "Bearer $token",
            "Accept" to "application/json"
        )

        VolleyClient.get(endpoint, headers,
            onSuccess = { responseString ->
                try {
                    val response = gson.fromJson(responseString, MyBookingsResponse::class.java)
                    if (response.success) {
                        onSuccess(response.data)
                    } else {
                        onError(Exception("Failed to fetch bookings"))
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

// Data class untuk response dari check-availability
data class SessionResponse(
    val success: Boolean,
    val data: List<Session>
)

// Data class untuk satu sesi, dibuat Parcelable jika perlu dikirim antar fragment/activity
@Parcelize
data class Session(
    val id: Int,
    val description: String?,
    val start_time: String,
    val end_time: String,
    val is_available: Boolean,
    val price: Double
) : Parcelable

// Data class untuk menampung response dari API
data class MyBookingsResponse(
    val success: Boolean,
    val data: List<Booking>
)

// Data class untuk satu item booking
@Parcelize
data class Booking(
    val booking_id: Int,
    val lapangan: LapanganInfo,
    val date: String,
    val start_time: String,
    val end_time: String,
    val total_price: String,
    val status: String,
    val payment_status: String,
    val created_at: String
) : Parcelable

@Parcelize
data class LapanganInfo(
    val id: Int?,
    val name: String?,
    val photo: String?
) : Parcelable
