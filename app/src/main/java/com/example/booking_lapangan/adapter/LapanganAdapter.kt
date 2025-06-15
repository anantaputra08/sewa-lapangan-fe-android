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
import com.example.booking_lapangan.api.Lapangan
import java.text.NumberFormat
import java.util.Locale

class LapanganAdapter(
    private var lapangans: List<Lapangan>,
    private val onItemClicked: (Lapangan) -> Unit // Listener
) :
    RecyclerView.Adapter<LapanganAdapter.LapanganViewHolder>() {

    // Ubah inner class menjadi non-static untuk akses ke listener
    inner class LapanganViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imageView: ImageView = view.findViewById(R.id.image_view_lapangan)
        private val nameTextView: TextView = view.findViewById(R.id.text_view_lapangan_name)
        private val categoryTextView: TextView = view.findViewById(R.id.text_view_category)
        private val priceTextView: TextView = view.findViewById(R.id.text_view_price)
        private val statusTextView: TextView = view.findViewById(R.id.text_view_status)

        fun bind(lapangan: Lapangan) {
            nameTextView.text = lapangan.name
            categoryTextView.text = lapangan.category

            val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            priceTextView.text = "${format.format(lapangan.price)} / jam"

            Glide.with(itemView.context)
                .load(lapangan.photo)
                .placeholder(R.drawable.ic_dashboard_black_24dp)
                .error(R.drawable.ic_dashboard_black_24dp)
                .into(imageView)

            if (lapangan.is_fully_booked_on_date) {
                statusTextView.text = "Penuh"
                statusTextView.background = ContextCompat.getDrawable(itemView.context, R.drawable.status_background_unavailable)
                itemView.isClickable = false // Tidak bisa diklik jika penuh
                itemView.alpha = 0.6f
            } else {
                statusTextView.text = "Tersedia"
                statusTextView.background = ContextCompat.getDrawable(itemView.context, R.drawable.status_background_available)
                itemView.isClickable = true
                itemView.alpha = 1.0f
                // Set listener di sini
                itemView.setOnClickListener {
                    onItemClicked(lapangan)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LapanganViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lapangan, parent, false)
        return LapanganViewHolder(view)
    }

    override fun onBindViewHolder(holder: LapanganViewHolder, position: Int) {
        holder.bind(lapangans[position])
    }

    override fun getItemCount() = lapangans.size

    fun updateData(newLapangans: List<Lapangan>) {
        lapangans = newLapangans
        notifyDataSetChanged()
    }
}
