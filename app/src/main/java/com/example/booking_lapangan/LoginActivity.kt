package com.example.booking_lapangan

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.booking_lapangan.api.AuthApi
import com.example.booking_lapangan.api.LoginRequest

class LoginActivity : AppCompatActivity() {

    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var sessionManager: SessionManager
    private lateinit var textViewRegisterLink: TextView
    private val TAG = "LoginActivity" // Tag untuk Logcat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inisialisasi SessionManager
        sessionManager = SessionManager(this)

        // Jika sudah login, langsung ke MainActivity
        if (sessionManager.isLoggedIn()) {
            Log.d(TAG, "User already logged in. Navigating to MainActivity.")
            navigateToMain()
            return // Hentikan eksekusi onCreate lebih lanjut
        }

        // Binding views
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        progressBar = findViewById(R.id.progressBar)
        textViewRegisterLink = findViewById(R.id.textViewRegisterLink)

        // Set listener untuk tombol login
        buttonLogin.setOnClickListener {
            handleLogin()
        }
        // Set listener untuk link ke RegisterActivity
        textViewRegisterLink.setOnClickListener {
            Log.d(TAG, "Navigating to RegisterActivity.")
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun handleLogin() {
        val email = editTextEmail.text.toString().trim()
        val password = editTextPassword.text.toString().trim()

        if (email.isEmpty()) {
            editTextEmail.error = "Email tidak boleh kosong"
            editTextEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            editTextPassword.error = "Password tidak boleh kosong"
            editTextPassword.requestFocus()
            return
        }

        // Tampilkan loading dan nonaktifkan tombol
        showLoading(true)
        Log.d(TAG, "Attempting to login with email: $email")

        val loginRequest = LoginRequest(email, password)

        AuthApi.login(this, loginRequest,
            onSuccess = { loginResponse ->
                showLoading(false)
                Log.d(TAG, "Login successful. Response: $loginResponse")
                Toast.makeText(this, loginResponse.message, Toast.LENGTH_SHORT).show()

                // Simpan token dan data user
                sessionManager.saveAuthToken(loginResponse.data.token)
                sessionManager.saveUserDetails(loginResponse.data.user)
                Log.d(TAG, "Token and user details saved.")

                // Pindah ke MainActivity
                navigateToMain()
            },
            onError = { exception ->
                showLoading(false)
                // Gunakan Log.e untuk error
                Log.e(TAG, "Login failed. Error: ${exception.message}", exception)
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            buttonLogin.visibility = View.GONE
        } else {
            progressBar.visibility = View.GONE
            buttonLogin.visibility = View.VISIBLE
        }
    }

    private fun navigateToMain() {
        Log.d(TAG, "Navigating to MainActivity.")
        val intent = Intent(this, MainActivity::class.java)
        // Hapus semua activity sebelumnya dari stack
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Tutup LoginActivity
    }
}
