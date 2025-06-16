package com.example.booking_lapangan.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.booking_lapangan.R
import com.example.booking_lapangan.api.Booking
import com.example.booking_lapangan.api.Session
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class AdminBookingAdapter(
    private var bookings: List<Booking>,
    private val onEditClick: (Booking) -> Unit,
    private val onDeleteClick: (Booking) -> Unit,
    private var allSessions: List<Session> = emptyList()
) : RecyclerView.Adapter<AdminBookingAdapter.BookingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(bookings[position])
    }

    override fun getItemCount() = bookings.size

    fun updateData(newBookings: List<Booking>, sessions: List<Session> = allSessions) {
        this.bookings = newBookings
        if (sessions.isNotEmpty()) {
            this.allSessions = sessions
        }
        notifyDataSetChanged()
    }

    inner class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lapanganName: TextView = itemView.findViewById(R.id.text_view_lapangan_name)
        private val userName: TextView = itemView.findViewById(R.id.text_view_user_name)
        private val bookingId: TextView = itemView.findViewById(R.id.text_view_booking_id)
        private val dateTime: TextView = itemView.findViewById(R.id.text_view_date_time)
        private val totalPrice: TextView = itemView.findViewById(R.id.text_view_total_price)
        private val bookingStatus: TextView = itemView.findViewById(R.id.text_view_booking_status)
        private val paymentStatus: TextView = itemView.findViewById(R.id.text_view_payment_status)
        private val editButton: ImageButton = itemView.findViewById(R.id.button_edit)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.button_delete)

        fun bind(booking: Booking) {
            lapanganName.text = booking.lapangan.name ?: "-"
            userName.text = "Dipesan oleh: ${booking.user?.name ?: "N/A"}"
            bookingId.text = "#BOOK${booking.booking_id}"

            // Format tanggal dari ISO8601, handle null
            val outputDateFormat = SimpleDateFormat("d MMMM yyyy", Locale("in", "ID"))
            val inputDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            inputDateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val dateStr = booking.date?.substringBefore(".")
            val tanggal = if (!dateStr.isNullOrEmpty()) {
                try {
                    outputDateFormat.format(inputDateFormat.parse(dateStr)!!)
                } catch (e: Exception) {
                    booking.date ?: "-"
                }
            } else {
                "-"
            }

            // Ambil jam dari sessions (jika ada), fallback ke start_time-end_time
            val sessionTimes = booking.sessions?.mapNotNull {
                val start = it.start_time ?: "-"
                val end = it.end_time ?: "-"
                "$start - $end"
            } ?: emptyList()
            val jamGabung = if (sessionTimes.isNotEmpty()) {
                sessionTimes.joinToString(", ")
            } else {
                val s = booking.start_time ?: "-"
                val e = booking.end_time ?: "-"
                "$s - $e"
            }

            dateTime.text = "$tanggal, $jamGabung"

            val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            format.maximumFractionDigits = 0
            val priceVal = booking.total_price?.toDoubleOrNull() ?: 0.0
            totalPrice.text = format.format(priceVal)

            val status = booking.status ?: "-"
            bookingStatus.text = status.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            val bookingStatusBg = when (status.lowercase(Locale.getDefault())) {
                "confirmed", "completed" -> R.drawable.status_background_available
                "cancelled" -> R.drawable.status_background_unavailable
                else -> R.drawable.status_background_pending
            }
            bookingStatus.background = ContextCompat.getDrawable(itemView.context, bookingStatusBg)

            val payStatus = booking.payment_status ?: "-"
            paymentStatus.text = payStatus.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            val paymentStatusBg = when (payStatus.lowercase(Locale.getDefault())) {
                "paid" -> R.drawable.status_background_available
                "unpaid" -> R.drawable.status_background_unavailable
                else -> R.drawable.status_background_pending
            }
            paymentStatus.background = ContextCompat.getDrawable(itemView.context, paymentStatusBg)

            editButton.setOnClickListener { onEditClick(booking) }
            deleteButton.setOnClickListener { onDeleteClick(booking) }
        }
    }
}
