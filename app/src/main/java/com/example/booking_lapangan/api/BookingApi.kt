package com.example.booking_lapangan.api

import android.content.Context
import android.os.Parcelable
import com.example.booking_lapangan.SessionManager
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
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
    val description: String? = null,
    val start_time: String = "",
    val end_time: String = "",
    val is_available: Boolean = false,
    val price_formatted: String? = null,
    val price: Double = 0.0
) : Parcelable

// Data class untuk menampung response dari API
data class MyBookingsResponse(
    val success: Boolean,
    val data: List<Booking>
)

// Data class untuk satu item booking - DIPERBAIKI
@Parcelize
data class Booking(
    val booking_id: Int = 0,
    val user: User? = null,
    val lapangan: LapanganInfo = LapanganInfo(),
    val date: String = "",
    val start_time: String = "",
    val end_time: String = "",
    val total_price: String = "0",
    val status: String = "",
    val payment_status: String = "",
    val created_at: String? = null,
    @SerializedName("sessions")
    val sessions: List<Session>? = null
) : Parcelable {
    // Override hashCode untuk menangani nullable fields
    override fun hashCode(): Int {
        var result = booking_id
        result = 31 * result + (user?.hashCode() ?: 0)
        result = 31 * result + lapangan.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + start_time.hashCode()
        result = 31 * result + end_time.hashCode()
        result = 31 * result + total_price.hashCode()
        result = 31 * result + status.hashCode()
        result = 31 * result + payment_status.hashCode()
        result = 31 * result + (created_at?.hashCode() ?: 0)
        result = 31 * result + (sessions?.hashCode() ?: 0)
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Booking

        if (booking_id != other.booking_id) return false
        if (user != other.user) return false
        if (lapangan != other.lapangan) return false
        if (date != other.date) return false
        if (start_time != other.start_time) return false
        if (end_time != other.end_time) return false
        if (total_price != other.total_price) return false
        if (status != other.status) return false
        if (payment_status != other.payment_status) return false
        if (created_at != other.created_at) return false
        if (sessions != other.sessions) return false

        return true
    }
}

@Parcelize
data class LapanganInfo(
    val id: Int = 0,
    val name: String = "",
    val photo: String? = null,
    val photo_url: String? = null
) : Parcelable {
    // Override hashCode untuk menangani nullable fields
    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        result = 31 * result + (photo?.hashCode() ?: 0)
        result = 31 * result + (photo_url?.hashCode() ?: 0)
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LapanganInfo

        if (id != other.id) return false
        if (name != other.name) return false
        if (photo != other.photo) return false
        if (photo_url != other.photo_url) return false

        return true
    }
}
