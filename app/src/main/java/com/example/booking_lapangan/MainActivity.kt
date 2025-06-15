package com.example.booking_lapangan

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.booking_lapangan.api.AuthApi
import com.example.booking_lapangan.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi SessionManager
        sessionManager = SessionManager(this)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_dashboard, R.id.navigation_my_bookings, R.id.navigation_profile
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate menu; ini menambahkan item ke action bar.
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Tangani klik pada item menu
        return when (item.itemId) {
            R.id.action_logout -> {
                handleLogout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun handleLogout() {
        val token = sessionManager.fetchAuthToken()

        if (token == null) {
            // Jika tidak ada token, langsung clear sesi dan pindah activity
            clearSessionAndNavigateToLogin()
            return
        }

        // Panggil API logout
        AuthApi.logout(this, "Bearer $token",
            onSuccess = {
                Toast.makeText(this, "Logout berhasil", Toast.LENGTH_SHORT).show()
                clearSessionAndNavigateToLogin()
            },
            onError = { exception ->
                // Tetap logout dari sisi client meskipun API gagal
                Toast.makeText(this, "Sesi lokal dihapus", Toast.LENGTH_SHORT).show()
                clearSessionAndNavigateToLogin()
            }
        )
    }

    private fun clearSessionAndNavigateToLogin() {
        // Hapus sesi
        sessionManager.clearSession()

        // Pindah ke LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
