package com.example.booking_lapangan.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.booking_lapangan.R
import com.example.booking_lapangan.adapter.SessionAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SessionBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var viewModel: SessionViewModel
    private lateinit var sessionAdapter: SessionAdapter
    private lateinit var bookButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView

    private var lapanganId: Int = -1
    private var selectedDate: String = ""

    companion object {
        const val TAG = "SessionBottomSheetFragment"
        private const val ARG_LAPANGAN_ID = "lapangan_id"
        private const val ARG_SELECTED_DATE = "selected_date"

        fun newInstance(lapanganId: Int, date: String): SessionBottomSheetFragment {
            val fragment = SessionBottomSheetFragment()
            val args = Bundle()
            args.putInt(ARG_LAPANGAN_ID, lapanganId)
            args.putString(ARG_SELECTED_DATE, date)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            lapanganId = it.getInt(ARG_LAPANGAN_ID)
            selectedDate = it.getString(ARG_SELECTED_DATE, "")
        }
        viewModel = ViewModelProvider(this).get(SessionViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_session_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi semua view
        recyclerView = view.findViewById(R.id.recycler_view_sessions)
        progressBar = view.findViewById(R.id.progress_bar_sessions)
        bookButton = view.findViewById(R.id.button_book_session)

        // Lakukan semua setup
        setupRecyclerView()
        setupObservers()
        setupBookingButton()

        // Ambil data dari API
        if (lapanganId != -1 && selectedDate.isNotEmpty()) {
            viewModel.fetchSessions(lapanganId, selectedDate)
        } else {
            Toast.makeText(context, "Data tidak valid", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    private fun setupRecyclerView() {
        // Inisialisasi adapter HANYA SATU KALI
        sessionAdapter = SessionAdapter(emptyList()) {
            // Lambda ini dipanggil setiap kali sesi diklik.
            // Ini akan mengaktifkan/menonaktifkan tombol booking.
            bookButton.isEnabled = sessionAdapter.getSelectedSessions().isNotEmpty()
        }
        recyclerView.adapter = sessionAdapter
    }

    private fun setupObservers() {
        // Observer untuk data sesi
        viewModel.sessions.observe(viewLifecycleOwner) { sessions ->
            // Perbarui data adapter, jangan buat adapter baru
            sessionAdapter.updateData(sessions)
        }

        // Observer untuk loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observer untuk error
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
        }

        // Observer untuk hasil booking
        viewModel.bookingResult.observe(viewLifecycleOwner) { result ->
            progressBar.visibility = View.GONE
            bookButton.text = "Booking Sekarang"
            bookButton.isEnabled = sessionAdapter.getSelectedSessions().isNotEmpty()

            result.onSuccess { message ->
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                dismiss() // Tutup bottom sheet jika berhasil
            }.onFailure { error ->
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupBookingButton() {
        bookButton.setOnClickListener {
            val selectedIds = sessionAdapter.getSelectedSessions().toList()
            if (selectedDate.isNotEmpty() && selectedIds.isNotEmpty()) {
                // Tampilkan state loading
                it.isEnabled = false
                bookButton.text = "MEMPROSES..."
                progressBar.visibility = View.VISIBLE

                // Panggil ViewModel untuk membuat booking
                viewModel.createBooking(lapanganId, selectedDate, selectedIds)
            }
        }
    }
}
