package com.example.booking_lapangan

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.booking_lapangan.api.AuthApi
import com.example.booking_lapangan.api.RegisterRequest
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject

class RegisterActivity : AppCompatActivity() {

    private lateinit var editTextName: TextInputEditText
    private lateinit var editTextEmail: TextInputEditText
    private lateinit var editTextPassword: TextInputEditText
    private lateinit var editTextPasswordConfirm: TextInputEditText
    private lateinit var buttonRegister: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var loginLink: TextView
    private val TAG = "RegisterActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Binding views
        editTextName = findViewById(R.id.editTextName)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        editTextPasswordConfirm = findViewById(R.id.editTextPasswordConfirm)
        buttonRegister = findViewById(R.id.buttonRegister)
        progressBar = findViewById(R.id.progressBar)
        loginLink = findViewById(R.id.textViewLoginLink)

        buttonRegister.setOnClickListener {
            handleRegister()
        }

        loginLink.setOnClickListener {
            finish() // Kembali ke LoginActivity
        }
    }

    private fun handleRegister() {
        val name = editTextName.text.toString().trim()
        val email = editTextEmail.text.toString().trim()
        val password = editTextPassword.text.toString().trim()
        val passwordConfirm = editTextPasswordConfirm.text.toString().trim()

        // Validasi input
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Semua field wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != passwordConfirm) {
            editTextPasswordConfirm.error = "Konfirmasi password tidak cocok"
            editTextPasswordConfirm.requestFocus()
            return
        }

        showLoading(true)
        val request = RegisterRequest(name, email, password, passwordConfirm)

        AuthApi.register(this, request,
            onSuccess = { response ->
                showLoading(false)
                Log.d(TAG, "Register successful: ${response.message}")
                Toast.makeText(this, response.message, Toast.LENGTH_LONG).show()
                // Kembali ke LoginActivity setelah berhasil
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            },
            onError = { exception ->
                showLoading(false)
                val errorMessage = parseVolleyError(exception)
                Log.e(TAG, "Register failed: $errorMessage", exception)
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun parseVolleyError(error: Exception): String {
        val networkResponse = (error as? com.android.volley.VolleyError)?.networkResponse
        if (networkResponse?.data != null) {
            val jsonError = String(networkResponse.data, Charsets.UTF_8)
            return try {
                val errorObj = JSONObject(jsonError)
                val message = errorObj.optString("message", "Terjadi kesalahan")
                val errors = errorObj.optJSONObject("data")
                if (errors != null) {
                    val errorMessages = StringBuilder("$message:\n")
                    errors.keys().forEach { key ->
                        val fieldErrors = errors.getJSONArray(key)
                        for (i in 0 until fieldErrors.length()) {
                            errorMessages.append("- ${fieldErrors.getString(i)}\n")
                        }
                    }
                    errorMessages.toString().trim()
                } else {
                    message
                }
            } catch (e: Exception) {
                jsonError
            }
        }
        return error.message ?: "Terjadi kesalahan jaringan"
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            buttonRegister.visibility = View.GONE
        } else {
            progressBar.visibility = View.GONE
            buttonRegister.visibility = View.VISIBLE
        }
    }
}
