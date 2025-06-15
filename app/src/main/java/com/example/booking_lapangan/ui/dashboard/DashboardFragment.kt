package com.example.booking_lapangan.ui.dashboard

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.booking_lapangan.R
import com.example.booking_lapangan.adapter.LapanganAdapter
import com.example.booking_lapangan.api.Lapangan
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DashboardFragment : Fragment() {

    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var lapanganAdapter: LapanganAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyTextView: TextView
    private lateinit var datePickerButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)

        dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)

        recyclerView = root.findViewById(R.id.recycler_view_lapangan)
        progressBar = root.findViewById(R.id.progress_bar)
        emptyTextView = root.findViewById(R.id.text_view_empty)
        datePickerButton = root.findViewById(R.id.button_date_picker)

        setupRecyclerView()
        setupObservers()
        setupDatePicker()

        return root
    }

    private fun setupRecyclerView() {
        // Inisialisasi adapter dengan lambda untuk handle klik
        lapanganAdapter = LapanganAdapter(emptyList()) { lapangan ->
            // Aksi saat item diklik
            showSessionBottomSheet(lapangan)
        }
        recyclerView.adapter = lapanganAdapter
    }

    private fun showSessionBottomSheet(lapangan: Lapangan) {
        val selectedDate = dashboardViewModel.selectedDate.value
        if (selectedDate == null) {
            Toast.makeText(context, "Silakan pilih tanggal terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val bottomSheet = SessionBottomSheetFragment.newInstance(lapangan.id, selectedDate)
        bottomSheet.show(childFragmentManager, SessionBottomSheetFragment.TAG)
    }

    private fun setupDatePicker() {
        datePickerButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val selectedCalendar = Calendar.getInstance()
                    selectedCalendar.set(year, month, dayOfMonth)
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val formattedDate = dateFormat.format(selectedCalendar.time)
                    dashboardViewModel.setSelectedDate(formattedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            // Batasi tanggal agar tidak bisa memilih tanggal yang sudah lewat
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
            datePickerDialog.show()
        }
    }

    private fun setupObservers() {
        dashboardViewModel.lapangans.observe(viewLifecycleOwner) { lapangans ->
            if (lapangans.isEmpty()) {
                recyclerView.visibility = View.GONE
                emptyTextView.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                emptyTextView.visibility = View.GONE
                lapanganAdapter.updateData(lapangans)
            }
        }

        dashboardViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        dashboardViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
        }

        dashboardViewModel.selectedDate.observe(viewLifecycleOwner) { date ->
            val displayFormat = SimpleDateFormat("dd MMMM yyyy", Locale("in", "ID"))
            val apiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateObj = apiFormat.parse(date)
            datePickerButton.text = displayFormat.format(dateObj!!)
        }
    }
}
