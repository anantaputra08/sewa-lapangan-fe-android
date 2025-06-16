package com.example.booking_lapangan.ui.admin.bookings

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booking_lapangan.R
import com.example.booking_lapangan.adapter.SessionAdapter
import com.example.booking_lapangan.api.Session
import com.example.booking_lapangan.api.VolleyClient
import org.json.JSONObject

/**
 * Dialog untuk memilih sesi yang tersedia pada tanggal dan lapangan tertentu.
 */
class AdminSessionSelectionDialog(
    context: Context,
    private val lapanganId: Int,
    private val date: String,
    private val onSessionsSelected: (List<Session>) -> Unit
) : Dialog(context) {

    private lateinit var progressBar: ProgressBar
    private lateinit var sessionRecyclerView: RecyclerView
    private lateinit var infoTextView: TextView
    private lateinit var selectButton: Button

    private var sessionAdapter: SessionAdapter? = null
    private var sessions: List<Session> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_admin_select_session, null)
        setContentView(view)
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        infoTextView = view.findViewById(R.id.text_view_session_info)
        progressBar = view.findViewById(R.id.progress_bar_sessions)
        sessionRecyclerView = view.findViewById(R.id.recycler_view_sessions)
        selectButton = view.findViewById(R.id.button_select_sessions)

        // Pastikan layout manager sudah benar
        sessionRecyclerView.layoutManager = LinearLayoutManager(context)
        sessionAdapter = SessionAdapter(emptyList()) {
            // Seleksi handled di adapter, tidak perlu aksi di sini
        }
        sessionRecyclerView.adapter = sessionAdapter

        selectButton.setOnClickListener {
            val selectedIds = sessionAdapter?.getSelectedSessions() ?: emptySet()
            val selectedSessions = sessions.filter { selectedIds.contains(it.id) }
            if (selectedSessions.isEmpty()) {
                Toast.makeText(context, "Pilih minimal satu sesi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            onSessionsSelected(selectedSessions)
            dismiss()
        }

        loadAvailableSessions()
    }

    private fun loadAvailableSessions() {
        progressBar.visibility = android.view.View.VISIBLE
        infoTextView.text = "Memuat sesi..."

        val sessionManager = com.example.booking_lapangan.SessionManager(context)
        val token = sessionManager.fetchAuthToken()
        if (token == null) {
            progressBar.visibility = android.view.View.GONE
            infoTextView.text = "Admin belum login"
            selectButton.isEnabled = false
            return
        }
        val headers = mapOf("Authorization" to "Bearer $token")

        val endpoint = "api/admin/bookings/available-sessions?lapangan_id=$lapanganId&date=$date"

        VolleyClient.get(endpoint, headers,
            onSuccess = { responseString ->
                progressBar.visibility = android.view.View.GONE
                try {
                    val response = JSONObject(responseString)
                    val success = response.optBoolean("success", false)
                    if (success) {
                        val data = response.getJSONObject("data")
                        val allSessionsArray = data.optJSONArray("all_sessions") ?: data.optJSONArray("sessions")
                        if (allSessionsArray != null) {
                            val sessionList = mutableListOf<Session>()
                            for (i in 0 until allSessionsArray.length()) {
                                val item = allSessionsArray.getJSONObject(i)
                                sessionList.add(
                                    Session(
                                        id = item.optInt("id"),
                                        start_time = item.optString("start_time"),
                                        end_time = item.optString("end_time"),
                                        price = item.optDouble("price"),
                                        is_available = item.optBoolean("is_available"),
                                        price_formatted = item.optString("price_formatted"),
                                        description = item.optString("description", null)
                                    )
                                )
                            }
                            sessions = sessionList
                            sessionAdapter?.updateData(sessionList)
                            infoTextView.text = if (sessionList.isEmpty()) "Tidak ada sesi ditemukan" else ""
                            selectButton.isEnabled = sessionList.any { it.is_available }
                        } else {
                            infoTextView.text = "Data sesi kosong"
                            selectButton.isEnabled = false
                        }
                    } else {
                        val message = response.optString("message", "Gagal memuat sesi")
                        infoTextView.text = message
                        selectButton.isEnabled = false
                    }
                } catch (e: Exception) {
                    infoTextView.text = "Format data sesi tidak valid"
                    selectButton.isEnabled = false
                }
            },
            onError = {
                progressBar.visibility = android.view.View.GONE
                infoTextView.text = "Gagal memuat sesi: ${it.message}"
                selectButton.isEnabled = false
            }
        )
    }

    companion object {
        /**
         * Static helper untuk menampilkan dialog dan mengembalikan sesi yang dipilih.
         */
        fun show(
            context: Context,
            lapanganId: Int,
            date: String,
            onSessionsSelected: (List<Session>) -> Unit
        ) {
            val dialog = AdminSessionSelectionDialog(context, lapanganId, date, onSessionsSelected)
            dialog.show()
        }
    }
}
