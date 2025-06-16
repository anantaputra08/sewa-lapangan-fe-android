package com.example.booking_lapangan.ui.bookingDetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.booking_lapangan.R
import com.example.booking_lapangan.api.Booking
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class BookingDetailFragment : Fragment() {

    private var booking: Booking? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            booking = it.getParcelable("booking")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_booking_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(view)
        booking?.let { populateView(view, it) }
    }

    private fun setupToolbar(view: View) {
        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        toolbar.title = "Detail Pemesanan"
        toolbar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_arrow_back_24)
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun populateView(view: View, booking: Booking) {
        val imageView: ImageView = view.findViewById(R.id.image_view_lapangan_detail)
        val nameTextView: TextView = view.findViewById(R.id.text_view_lapangan_name_detail)
        val dateTextView: TextView = view.findViewById(R.id.text_view_date_detail)
        val timeTextView: TextView = view.findViewById(R.id.text_view_time_detail)
        val priceTextView: TextView = view.findViewById(R.id.text_view_price_detail)

        Glide.with(this)
            .load(booking.lapangan.photo ?: booking.lapangan.photo_url)
            .into(imageView)

        nameTextView.text = booking.lapangan.name ?: "-"

        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale("in", "ID"))
        val dateRaw = booking.date
        dateTextView.text = if (!dateRaw.isNullOrEmpty()) {
            try {
                val dateObj = inputFormat.parse(dateRaw)
                outputFormat.format(dateObj!!)
            } catch (e: Exception) {
                dateRaw
            }
        } else {
            "-"
        }

        timeTextView.text = "${booking.start_time ?: "-"} - ${booking.end_time ?: "-"}"

        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        format.maximumFractionDigits = 0
        val priceValue = booking.total_price?.toDoubleOrNull() ?: 0.0
        priceTextView.text = format.format(priceValue)
    }

    companion object {
        private const val ARG_BOOKING = "arg_booking"
        @JvmStatic
        fun newInstance(booking: Booking) =
            BookingDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_BOOKING, booking)
                }
            }
    }
}
