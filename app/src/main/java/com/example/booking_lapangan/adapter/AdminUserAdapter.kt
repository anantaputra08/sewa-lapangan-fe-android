package com.example.booking_lapangan.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.booking_lapangan.R
import com.example.booking_lapangan.api.User

class AdminUserAdapter(
    private var users: List<User>,
    private val onEditClick: (User) -> Unit,
    private val onDeleteClick: (User) -> Unit
) : RecyclerView.Adapter<AdminUserAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount() = users.size

    fun updateData(newUsers: List<User>) {
        this.users = newUsers
        notifyDataSetChanged()
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val photo: ImageView = itemView.findViewById(R.id.image_view_user_photo)
        private val name: TextView = itemView.findViewById(R.id.text_view_user_name)
        private val email: TextView = itemView.findViewById(R.id.text_view_user_email)
        private val editButton: ImageButton = itemView.findViewById(R.id.button_edit)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.button_delete)

        fun bind(user: User) {
            name.text = user.name
            email.text = user.email
            Glide.with(itemView.context)
                .load(user.photoUrl)
                .placeholder(R.drawable.ic_baseline_person_24)
                .error(R.drawable.ic_baseline_person_24)
                .circleCrop()
                .into(photo)

            editButton.setOnClickListener { onEditClick(user) }
            deleteButton.setOnClickListener { onDeleteClick(user) }
        }
    }
}
