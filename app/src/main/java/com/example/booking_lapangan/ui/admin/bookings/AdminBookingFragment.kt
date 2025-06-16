package com.example.booking_lapangan.ui.admin.bookings

import android.app.DatePickerDialog
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.example.booking_lapangan.R
import com.example.booking_lapangan.adapter.AdminBookingAdapter
import com.example.booking_lapangan.api.Booking
import com.example.booking_lapangan.api.BookingCreationData
import com.example.booking_lapangan.api.Session
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AdminBookingFragment : Fragment() {

    private val viewModel: AdminBookingViewModel by viewModels()
    private lateinit var bookingAdapter: AdminBookingAdapter
    private var creationData: BookingCreationData? = null
    private var selectedDate: String = ""
    private var selectedSessions: MutableList<Int> = mutableListOf()
    private var selectedLapanganId: Int? = null

    // Simpan semua sesi untuk lookup jam di adapter
    private var allSessions: List<Session> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_admin_booking, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view_bookings)
        val progressBar: ProgressBar = view.findViewById(R.id.progress_bar)
        val fab: FloatingActionButton = view.findViewById(R.id.fab_add_booking)

        bookingAdapter = AdminBookingAdapter(
            emptyList(),
            onEditClick = { booking -> showEditDialog(booking) },
            onDeleteClick = { booking -> showDeleteConfirmation(booking) },
            allSessions // <-- kirim sessions kosong dulu, akan diupdate setelah fetchCreationData
        )
        recyclerView.adapter = bookingAdapter

        fab.setOnClickListener {
            showCreateBookingDialog()
        }

        setupObservers(progressBar)
        viewModel.fetchBookings()
        viewModel.fetchCreationData()
    }

    private fun setupObservers(progressBar: ProgressBar) {
        viewModel.bookings.observe(viewLifecycleOwner) { bookings ->
            bookingAdapter.updateData(bookings, allSessions)
        }

        viewModel.creationData.observe(viewLifecycleOwner) { data ->
            Log.d("AdminBookingFragment", "creationData: $data")
            Log.d("AdminBookingFragment", "lapangans: ${data.lapangans}")
            creationData = data
            allSessions = data.sessions // update allSessions setiap fetch baru
            bookingAdapter.updateData(viewModel.bookings.value ?: emptyList(), allSessions)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) {
            progressBar.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) {
            it?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
        }

        viewModel.actionSuccess.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                viewModel.fetchBookings()
                viewModel.onActionDone()
            }
        }
    }

    private fun showCreateBookingDialog() {
        val currentCreationData = creationData
        if (currentCreationData == null) {
            Toast.makeText(context, "Sedang memuat data, silakan tunggu...", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentCreationData.users.isNullOrEmpty()) {
            Toast.makeText(context, "Data pengguna tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }
        if (currentCreationData.lapangans.isNullOrEmpty()) {
            Toast.makeText(context, "Data lapangan tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }
        if (currentCreationData.sessions.isNullOrEmpty()) {
            Toast.makeText(context, "Data sesi tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_admin_create_booking, null)

        val usersSpinner: Spinner = dialogView.findViewById(R.id.spinner_users)
        val lapangansSpinner: Spinner = dialogView.findViewById(R.id.spinner_lapangans)
        val selectDateButton: Button = dialogView.findViewById(R.id.button_select_date)
        val selectedDateTextView: TextView = dialogView.findViewById(R.id.text_view_selected_date)
        val selectedSessionsTextView: TextView = dialogView.findViewById(R.id.text_view_selected_sessions)
        val bookingStatusSpinner: Spinner = dialogView.findViewById(R.id.spinner_booking_status)
        val paymentStatusSpinner: Spinner = dialogView.findViewById(R.id.spinner_payment_status)

        // Setup Users Spinner
        val userNames = currentCreationData.users.map { user ->
            "${user.name ?: "Unknown"} (${user.email ?: "No email"})"
        }
        val userAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, userNames)
        userAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        usersSpinner.adapter = userAdapter

        // Setup Lapangans Spinner
        val lapanganNames = currentCreationData.lapangans.map { lapangan ->
            "${lapangan.name ?: "Unknown"} - ${lapangan.category ?: "Unknown"}"
        }
        val lapanganAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, lapanganNames)
        lapanganAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        lapangansSpinner.adapter = lapanganAdapter
        lapangansSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedLapanganId = currentCreationData.lapangans.getOrNull(position)?.id
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        selectedLapanganId = currentCreationData.lapangans.getOrNull(lapangansSpinner.selectedItemPosition)?.id

        // Date Selection
        selectDateButton.setOnClickListener {
            showDatePicker { date ->
                selectedDate = date
                selectedDateTextView.text = "Tanggal dipilih: $date"
                selectedDateTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_700))
                selectedDateTextView.setTypeface(null, Typeface.BOLD)
                selectDateButton.text = "Ubah Tanggal ($date)"
                selectDateButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_700))
                selectDateButton.setTypeface(null, Typeface.BOLD)
                selectedSessions.clear()
                updateSelectedSessionsDisplay(selectedSessionsTextView)
            }
        }
        selectedDateTextView.text = if (selectedDate.isNotEmpty()) "Tanggal dipilih: $selectedDate" else ""
        selectedDateTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_700))
        selectedDateTextView.setTypeface(null, Typeface.BOLD)

        // Sessions Selection
        selectedSessionsTextView.setOnClickListener {
            if (selectedDate.isEmpty()) {
                Toast.makeText(context, "Pilih tanggal terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val lapId = selectedLapanganId
            if (lapId == null) {
                Toast.makeText(context, "Pilih lapangan terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            AdminSessionSelectionDialog.show(
                requireContext(),
                lapanganId = lapId,
                date = selectedDate
            ) { selectedSessionList ->
                selectedSessions.clear()
                selectedSessions.addAll(selectedSessionList.map { it.id })
                updateSelectedSessionsDisplay(selectedSessionsTextView)
            }
        }

        // Setup Booking Status Spinner
        ArrayAdapter.createFromResource(requireContext(), R.array.booking_statuses, android.R.layout.simple_spinner_item)
            .also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                bookingStatusSpinner.adapter = adapter
            }

        // Setup Payment Status Spinner
        ArrayAdapter.createFromResource(requireContext(), R.array.payment_statuses, android.R.layout.simple_spinner_item)
            .also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                paymentStatusSpinner.adapter = adapter
            }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Buat Booking Baru")
            .setPositiveButton("Buat") { _, _ ->
                createBooking(
                    usersSpinner.selectedItemPosition,
                    lapangansSpinner.selectedItemPosition,
                    bookingStatusSpinner.selectedItem.toString().lowercase(),
                    paymentStatusSpinner.selectedItem.toString().lowercase()
                )
            }
            .setNegativeButton("Batal", null)
            .create()

        dialog.show()
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                onDateSelected(dateFormat.format(selectedCalendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun updateSelectedSessionsDisplay(textView: TextView) {
        if (selectedSessions.isEmpty()) {
            textView.text = "Pilih Sesi Jam (Multi-select)"
        } else {
            val currentCreationData = creationData
            if (currentCreationData?.sessions != null) {
                val sessionNames = selectedSessions.mapNotNull { sessionId ->
                    currentCreationData.sessions.find { it.id == sessionId }?.let { session ->
                        "${session.start_time ?: "00:00"} - ${session.end_time ?: "00:00"}"
                    }
                }
                textView.text = if (sessionNames.isNotEmpty()) {
                    "Sesi dipilih: ${sessionNames.joinToString(", ")}"
                } else {
                    "Pilih Sesi Jam (Multi-select)"
                }
            } else {
                textView.text = "Pilih Sesi Jam (Multi-select)"
            }
        }
    }

    private fun createBooking(
        userIndex: Int,
        lapanganIndex: Int,
        bookingStatus: String,
        paymentStatus: String
    ) {
        if (selectedDate.isEmpty()) {
            Toast.makeText(context, "Pilih tanggal terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedSessions.isEmpty()) {
            Toast.makeText(context, "Pilih minimal satu sesi", Toast.LENGTH_SHORT).show()
            return
        }

        val currentCreationData = creationData ?: run {
            Toast.makeText(context, "Data tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedUser = currentCreationData.users.getOrNull(userIndex)
        val selectedLapangan = currentCreationData.lapangans.getOrNull(lapanganIndex)

        if (selectedUser == null) {
            Toast.makeText(context, "User tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedLapangan == null) {
            Toast.makeText(context, "Lapangan tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        try {
            dateFormat.parse(selectedDate)
        } catch (e: Exception) {
            Toast.makeText(context, "Format tanggal tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        val params = JSONObject().apply {
            put("user_id", selectedUser.id)
            put("lapangan_id", selectedLapangan.id)
            put("date", selectedDate)
            put("session_hours_ids", JSONArray(selectedSessions))
            put("status", bookingStatus)
            put("payment_status", paymentStatus)
        }

        android.util.Log.d("AdminBookingFragment", "Creating booking with params: $params")

        viewModel.createBooking(params)
    }

    private fun showDeleteConfirmation(booking: Booking) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Booking")
            .setMessage("Anda yakin ingin menghapus booking #${booking.booking_id} oleh ${booking.user?.name}?")
            .setPositiveButton("Hapus") { _, _ -> viewModel.deleteBooking(booking) }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showEditDialog(booking: Booking) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_admin_edit_booking, null)
        val title: TextView = dialogView.findViewById(R.id.text_view_dialog_title)
        val info: TextView = dialogView.findViewById(R.id.text_view_booking_info)
        val bookingStatusSpinner: Spinner = dialogView.findViewById(R.id.spinner_booking_status)
        val paymentStatusSpinner: Spinner = dialogView.findViewById(R.id.spinner_payment_status)

        title.text = "Edit Booking #${booking.booking_id}"
        info.text = "Oleh: ${booking.user?.name ?: "N/A"}"

        ArrayAdapter.createFromResource(requireContext(), R.array.booking_statuses, android.R.layout.simple_spinner_item)
            .also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                bookingStatusSpinner.adapter = adapter
                val status = booking.status ?: ""
                val currentStatusPosition = adapter.getPosition(
                    status.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                )
                bookingStatusSpinner.setSelection(if (currentStatusPosition >= 0) currentStatusPosition else 0)
            }

        ArrayAdapter.createFromResource(requireContext(), R.array.payment_statuses, android.R.layout.simple_spinner_item)
            .also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                paymentStatusSpinner.adapter = adapter
                val payStatus = booking.payment_status ?: ""
                val currentPaymentPosition = adapter.getPosition(
                    payStatus.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                )
                paymentStatusSpinner.setSelection(if (currentPaymentPosition >= 0) currentPaymentPosition else 0)
            }

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val newStatus = bookingStatusSpinner.selectedItem.toString().lowercase()
                val newPaymentStatus = paymentStatusSpinner.selectedItem.toString().lowercase()
                viewModel.updateBooking(booking, newStatus, newPaymentStatus)
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
