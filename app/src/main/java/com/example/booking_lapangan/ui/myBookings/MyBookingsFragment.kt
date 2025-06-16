package com.example.booking_lapangan.ui.myBookings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.booking_lapangan.R
import com.example.booking_lapangan.adapter.MyBookingsAdapter
import com.example.booking_lapangan.api.Booking
import com.example.booking_lapangan.ui.bookingDetail.BookingDetailFragment

class MyBookingsFragment : Fragment() {

    private lateinit var viewModel: MyBookingsViewModel
    private lateinit var bookingsAdapter: MyBookingsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_my_bookings, container, false)

        viewModel = ViewModelProvider(this).get(MyBookingsViewModel::class.java)

        recyclerView = root.findViewById(R.id.recycler_view_my_bookings)
        progressBar = root.findViewById(R.id.progress_bar)
        emptyTextView = root.findViewById(R.id.text_view_empty)

        setupRecyclerView()
        setupObservers()

        return root
    }

    private fun setupRecyclerView() {
        bookingsAdapter = MyBookingsAdapter(emptyList()) { booking ->
            // Validasi booking sebelum navigasi
            if (isBookingValid(booking)) {
                val bundle = Bundle().apply {
                    putParcelable("booking", booking)
                }
                findNavController().navigate(R.id.action_myBookings_to_bookingDetail, bundle)
            } else {
                Toast.makeText(context, "Data booking tidak lengkap", Toast.LENGTH_SHORT).show()
            }
        }
        recyclerView.adapter = bookingsAdapter
    }

    private fun isBookingValid(booking: Booking): Boolean {
        return try {
            // Test apakah booking bisa di-hash tanpa error
            booking.hashCode()
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun setupObservers() {
        viewModel.bookings.observe(viewLifecycleOwner) { bookings ->
            if (bookings.isNullOrEmpty()) {
                recyclerView.visibility = View.GONE
                emptyTextView.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                emptyTextView.visibility = View.GONE
                bookingsAdapter.updateData(bookings)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, "Error: $it", Toast.LENGTH_LONG).show()
                emptyTextView.text = "Gagal memuat data."
                emptyTextView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Muat ulang data setiap kali fragmen ditampilkan untuk data yang lebih segar
        viewModel.fetchMyBookings()
    }
}
