package com.example.booking_lapangan.api

import android.content.Context
import android.os.Parcelable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

object AuthApi {
    private val gson = Gson()

    /**
     * Login menggunakan Volley dan mengembalikan LoginResponse (data class)
     */
    fun login(
        context: Context,
        loginRequest: LoginRequest,
        onSuccess: (LoginResponse) -> Unit,
        onError: (Exception) -> Unit
    ) {
        VolleyClient.initialize(context)
        val params = JSONObject().apply {
            put("email", loginRequest.email)
            put("password", loginRequest.password)
        }
        VolleyClient.post(
            endpoint = "api/login",
            params = params,
            onSuccess = { response ->
                try {
                    // Pastikan respons dari server Anda sesuai dengan struktur LoginResponse
                    val loginResponse = gson.fromJson(response, LoginResponse::class.java)
                    onSuccess(loginResponse)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            onError = onError
        )
    }

    /**
     * Logout menggunakan Volley dan mengembalikan LogoutResponse (data class)
     * Token harus dalam format "Bearer {token}"
     */
    fun logout(
        context: Context,
        token: String,
        onSuccess: (LogoutResponse) -> Unit,
        onError: (Exception) -> Unit
    ) {
        VolleyClient.initialize(context)
        VolleyClient.post(
            endpoint = "api/logout",
            params = JSONObject(), // body kosong
            headers = mapOf(
                "Authorization" to token,
                "Accept" to "application/json"
            ),
            onSuccess = { response ->
                try {
                    val logoutResponse = gson.fromJson(response, LogoutResponse::class.java)
                    onSuccess(logoutResponse)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            onError = onError
        )
    }

    /**
     * Register user baru menggunakan Volley dan mengembalikan RegisterResponse (data class)
     */
    fun register(
        context: Context,
        request: RegisterRequest,
        onSuccess: (RegisterResponse) -> Unit,
        onError: (Exception) -> Unit
    ) {
        VolleyClient.initialize(context)
        val params = JSONObject().apply {
            put("name", request.name)
            put("email", request.email)
            put("password", request.password)
            put("password_confirmation", request.password_confirmation)
            request.phone?.let { put("phone", it) }
            request.address?.let { put("address", it) }
        }
        VolleyClient.post(
            endpoint = "api/register",
            params = params,
            headers = mapOf("Accept" to "application/json"),
            onSuccess = { response ->
                try {
                    val registerResponse = gson.fromJson(response, RegisterResponse::class.java)
                    onSuccess(registerResponse)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            onError = onError
        )
    }
}

data class LoginRequest(
    val email: String,
    val password: String
)
data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: LoginData
)

data class LoginData(
    val user: User,
    val token: String
)

data class LogoutResponse(
    val success: Boolean,
    val message: String
)

data class RegisterResponse(
    val success: Boolean,
    val message: String,
    val user: User
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val password_confirmation: String,
    val phone: String? = null,
    val address: String? = null
)

//@Parcelize
//data class User(
//    val id: Int,
//    val name: String?,
//    val email: String?,
//    val email_verified_at: String? = null,
//    val role: String,
//    val phone: String?,
//    val address: String?,
//    @SerializedName("photo_url")
//    val photoUrl: String?,
//    val created_at: String? = null,
//    val updated_at: String? = null
//): Parcelable

// Data class untuk User - TAMBAHKAN INI JIKA BELUM ADA
@Parcelize
data class User(
    val id: Int,
    val name: String?,
    val email: String?,
    val email_verified_at: String? = null,
    val role: String,
    val phone: String?,
    val address: String?,
    @SerializedName("photo_url")
    val photoUrl: String?,
    val created_at: String? = null,
    val updated_at: String? = null
) : Parcelable {
    // Override hashCode untuk menangani nullable fields
    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + (phone?.hashCode() ?: 0)
        result = 31 * result + (created_at?.hashCode() ?: 0)
        result = 31 * result + (updated_at?.hashCode() ?: 0)
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (id != other.id) return false
        if (name != other.name) return false
        if (email != other.email) return false
        if (phone != other.phone) return false
        if (created_at != other.created_at) return false
        if (updated_at != other.updated_at) return false

        return true
    }
}
