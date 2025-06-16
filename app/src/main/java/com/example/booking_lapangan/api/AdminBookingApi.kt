package com.example.booking_lapangan.api

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.booking_lapangan.SessionManager
import com.example.booking_lapangan.api.VolleyClient.BASE_URL
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.json.JSONObject

object AdminBookingApi {
    private val gson = Gson()
    private const val TAG = "AdminBookingApi"

    fun getBookings(context: Context, onSuccess: (List<Booking>) -> Unit, onError: (Exception) -> Unit) {
        val sessionManager = SessionManager(context)
        val token = sessionManager.fetchAuthToken()
        if (token == null) { onError(Exception("Admin not authenticated")); return }

        val endpoint = "api/admin/bookings"
        val headers = mapOf("Authorization" to "Bearer $token")

        VolleyClient.get(endpoint, headers,
            onSuccess = { responseString ->
                try {
                    val response = gson.fromJson(responseString, MyBookingsResponse::class.java)
                    if (response.success) onSuccess(response.data)
                    else onError(Exception("Gagal memuat data booking"))
                } catch (e: Exception) { onError(e) }
            },
            onError = onError
        )
    }

    fun getCreationData(
        context: Context,
        onSuccess: (BookingCreationData) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val sessionManager = SessionManager(context)
        val token = sessionManager.fetchAuthToken()
        if (token == null) {
            onError(Exception("Admin not authenticated"))
            return
        }

        val endpoint = "api/admin/bookings/creation-data"
        val headers = mapOf("Authorization" to "Bearer $token")

        VolleyClient.get(endpoint, headers,
            onSuccess = { responseString ->
                try {
                    val response = gson.fromJson(responseString, CreationDataResponse::class.java)
                    if (response.success) {
                        onSuccess(response.data)
                    } else {
                        onError(Exception("Gagal memuat data creation"))
                    }
                } catch (e: Exception) {
                    onError(Exception("Error parsing creation data: ${e.message}"))
                }
            },
            onError = { exception ->
                onError(Exception("Network error: ${exception.message}"))
            }
        )
    }

    fun createBooking(
        context: Context,
        params: JSONObject,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val sessionManager = SessionManager(context)
        val token = sessionManager.fetchAuthToken()
        if (token == null) {
            onError(Exception("Admin not authenticated"))
            return
        }

        // Log the request parameters for debugging
        Log.d(TAG, "Creating booking with params: $params")

        val endpoint = "api/admin/bookings"
        val headers = mapOf(
            "Authorization" to "Bearer $token",
            "Content-Type" to "application/json",
            "Accept" to "application/json"
        )

        VolleyClient.post(endpoint, params, headers,
            onSuccess = { responseString ->
                Log.d(TAG, "Create booking success response: $responseString")
                try {
                    val response = JSONObject(responseString)
                    val success = response.optBoolean("success", false)
                    if (success) {
                        val message = response.optString("message", "Booking berhasil dibuat")
                        onSuccess(message)
                    } else {
                        val message = response.optString("message", "Failed to create booking")
                        val errors = response.optJSONObject("errors")
                        val errorMessage = if (errors != null) {
                            "Validation errors: $errors"
                        } else {
                            message
                        }
                        onError(Exception(errorMessage))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing create booking response", e)
                    onError(Exception("Error parsing response: ${e.message}"))
                }
            },
            onError = { exception ->
                Log.e(TAG, "Create booking network error", exception)
                onError(Exception("Failed to create booking: ${exception.message}"))
            }
        )
    }

    fun updateBooking(context: Context, booking: Booking, status: String, paymentStatus: String, onSuccess: (String) -> Unit, onError: (Exception) -> Unit) {
        val sessionManager = SessionManager(context)
        val token = sessionManager.fetchAuthToken()
        if (token == null) { onError(Exception("Admin not authenticated")); return }

        val endpoint = "api/admin/bookings/update/${booking.booking_id}"
        val headers = mapOf("Authorization" to "Bearer $token")
        val params = JSONObject().apply {
            put("status", status)
            put("payment_status", paymentStatus)
        }

        // PERBAIKAN: Ganti VolleyClient.put menjadi VolleyClient.post
        VolleyClient.post(endpoint, params, headers,
            onSuccess = { responseString ->
                try {
                    val response = JSONObject(responseString)
                    onSuccess(response.getString("message"))
                } catch (e: Exception) { onError(e) }
            },
            onError = onError
        )
    }

    fun deleteBooking(context: Context, booking: Booking, onSuccess: (String) -> Unit, onError: (Exception) -> Unit) {
        val sessionManager = SessionManager(context)
        val token = sessionManager.fetchAuthToken()
        if (token == null) { onError(Exception("Admin not authenticated")); return }

        val endpoint = "api/admin/bookings/${booking.booking_id}"
        val headers = mapOf("Authorization" to "Bearer $token")

        VolleyClient.delete(endpoint, headers,
            onSuccess = { responseString ->
                try {
                    val response = JSONObject(responseString)
                    onSuccess(response.getString("message"))
                } catch (e: Exception) { onError(e) }
            },
            onError = onError
        )
    }
}

data class BookingCreationData(
    @SerializedName("users")
    val users: List<User> = emptyList(),

    @SerializedName("lapangans")
    val lapangans: List<Lapangan> = emptyList(),

    @SerializedName("sessions")
    val sessions: List<Session> = emptyList()
)

data class CreationDataResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("data")
    val data: BookingCreationData = BookingCreationData()
)
