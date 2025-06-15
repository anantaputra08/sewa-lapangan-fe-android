package com.example.booking_lapangan.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.booking_lapangan.R
import com.example.booking_lapangan.api.Session
import java.text.NumberFormat
import java.util.Locale

class SessionAdapter(
    private var sessions: List<Session>,
    private val onSessionClick: (Session) -> Unit
) : RecyclerView.Adapter<SessionAdapter.SessionViewHolder>() {

    private val selectedSessionIds = mutableSetOf<Int>()

    fun getSelectedSessions(): Set<Int> {
        return selectedSessionIds
    }

    fun updateData(newSessions: List<Session>) {
        sessions = newSessions
        selectedSessionIds.clear()
        notifyDataSetChanged()
    }

    class SessionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val timeTextView: TextView = view.findViewById(R.id.text_view_session_time)
        val statusTextView: TextView = view.findViewById(R.id.text_view_session_status)
        val priceTextView: TextView = view.findViewById(R.id.text_view_session_price)
        val layout: LinearLayout = view.findViewById(R.id.linear_layout_session)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_session, parent, false)
        return SessionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        val session = sessions[position]
        holder.timeTextView.text = "${session.start_time} - ${session.end_time}"

        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        format.maximumFractionDigits = 0
        holder.priceTextView.text = format.format(session.price)


        if (session.is_available) {
            holder.statusTextView.text = "Tersedia"
            holder.statusTextView.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_green_dark))
            holder.itemView.isClickable = true

            if (selectedSessionIds.contains(session.id)) {
                holder.layout.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, android.R.color.darker_gray))
            } else {
                holder.layout.setBackgroundColor(Color.WHITE)
            }

            holder.itemView.setOnClickListener {
                if (selectedSessionIds.contains(session.id)) {
                    selectedSessionIds.remove(session.id)
                } else {
                    selectedSessionIds.add(session.id)
                }
                notifyItemChanged(position)
                onSessionClick(session)
            }
        } else {
            holder.statusTextView.text = "Dipesan"
            holder.statusTextView.setTextColor(Color.RED)
            holder.layout.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, android.R.color.background_light))
            holder.itemView.isClickable = false
        }
    }

    override fun getItemCount() = sessions.size
}
