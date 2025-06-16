package com.example.booking_lapangan.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.booking_lapangan.R
import com.example.booking_lapangan.api.Booking
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class MyBookingsAdapter(private var bookings: List<Booking>,
                        private val onItemClick: (Booking) -> Unit) :
    RecyclerView.Adapter<MyBookingsAdapter.BookingViewHolder>() {

    inner class BookingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imageView: ImageView = view.findViewById(R.id.image_view_lapangan)
        private val nameTextView: TextView = view.findViewById(R.id.text_view_lapangan_name)
        private val dateTextView: TextView = view.findViewById(R.id.text_view_date)
        private val priceTextView: TextView = view.findViewById(R.id.text_view_total_price)
        private val statusTextView: TextView = view.findViewById(R.id.text_view_status)

        fun bind(booking: Booking) {
            nameTextView.text = booking.lapangan.name ?: "Nama Lapangan"

            // Format Tanggal dan Waktu
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale("in", "ID"))
            val startTime = booking.start_time ?: "-"
            val endTime = booking.end_time ?: "-"
            val dateRaw = booking.date
            dateTextView.text = if (!dateRaw.isNullOrEmpty()) {
                try {
                    val date = inputFormat.parse(dateRaw)
                    val formattedDate = outputFormat.format(date!!)
                    "$formattedDate, $startTime - $endTime"
                } catch (e: Exception) {
                    "$dateRaw, $startTime - $endTime"
                }
            } else {
                "-, $startTime - $endTime"
            }

            // Format Harga
            val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            format.maximumFractionDigits = 0
            val priceValue = booking.total_price?.toDoubleOrNull() ?: 0.0
            priceTextView.text = format.format(priceValue)

            // Status
            val status = booking.status ?: "-"
            statusTextView.text = status.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            val statusBg = when (status.lowercase(Locale.getDefault())) {
                "confirmed" -> R.drawable.status_background_available
                "pending" -> R.drawable.status_background_pending
                "cancelled" -> R.drawable.status_background_unavailable
                else -> R.drawable.status_background_pending
            }
            statusTextView.background = ContextCompat.getDrawable(itemView.context, statusBg)

            // Gambar
            Glide.with(itemView.context)
                .load(booking.lapangan.photo ?: booking.lapangan.photo_url)
                .placeholder(R.drawable.ic_dashboard_black_24dp)
                .error(R.drawable.ic_dashboard_black_24dp)
                .into(imageView)

            itemView.setOnClickListener {
                onItemClick(booking)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(bookings[position])
    }

    override fun getItemCount() = bookings.size

    fun updateData(newBookings: List<Booking>) {
        bookings = newBookings
        notifyDataSetChanged()
    }
}
