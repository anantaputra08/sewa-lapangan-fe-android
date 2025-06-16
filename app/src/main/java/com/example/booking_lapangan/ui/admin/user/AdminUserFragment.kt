package com.example.booking_lapangan.ui.admin.user

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.booking_lapangan.R
import com.example.booking_lapangan.adapter.AdminUserAdapter
import com.example.booking_lapangan.api.User
import com.example.booking_lapangan.api.VolleyMultipartRequest
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.IOException

class AdminUserFragment : Fragment() {

    private val viewModel: AdminUserViewModel by viewModels()
    private lateinit var userAdapter: AdminUserAdapter
    private var selectedImageUri: Uri? = null
    private var editDialog: AlertDialog? = null

    // Launcher untuk memilih gambar dari galeri
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            // Tampilkan pratinjau di dalam dialog yang sedang terbuka
            editDialog?.findViewById<ImageView>(R.id.image_view_photo_preview)?.let { preview ->
                Glide.with(this).load(it).circleCrop().into(preview)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_admin_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view_users)
        val progressBar: ProgressBar = view.findViewById(R.id.progress_bar)
        val fab: FloatingActionButton = view.findViewById(R.id.fab_add_user)

        setupRecyclerView(recyclerView)
        setupObservers(progressBar)

        fab.setOnClickListener { showUserDialog(null) }

        viewModel.fetchUsers()
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        userAdapter = AdminUserAdapter(emptyList(),
            onEditClick = { user -> showUserDialog(user) },
            onDeleteClick = { user -> showDeleteConfirmation(user) }
        )
        recyclerView.adapter = userAdapter
        recyclerView.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
    }

    private fun setupObservers(progressBar: ProgressBar) {
        viewModel.users.observe(viewLifecycleOwner) { users ->
            userAdapter.updateData(users)
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
                viewModel.fetchUsers() // Muat ulang daftar setelah aksi berhasil
                viewModel.onActionDone() // Reset status agar pesan tidak muncul lagi
            }
        }
    }

    private fun showDeleteConfirmation(user: User) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Pengguna")
            .setMessage("Anda yakin ingin menghapus ${user.name}?")
            .setPositiveButton("Hapus") { _, _ -> viewModel.deleteUser(user) }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showUserDialog(user: User?) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_admin_edit_user, null)
        val title = dialogView.findViewById<TextView>(R.id.text_view_dialog_title)
        val name = dialogView.findViewById<EditText>(R.id.edit_text_name)
        val email = dialogView.findViewById<EditText>(R.id.edit_text_email)
        val password = dialogView.findViewById<EditText>(R.id.edit_text_password)
        val roleSpinner = dialogView.findViewById<Spinner>(R.id.spinner_role)
        val photoPreview = dialogView.findViewById<ImageView>(R.id.image_view_photo_preview)
        val selectPhotoButton = dialogView.findViewById<Button>(R.id.button_select_photo)

        // Setup Spinner
        ArrayAdapter.createFromResource(requireContext(), R.array.user_roles, android.R.layout.simple_spinner_item)
            .also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                roleSpinner.adapter = adapter
            }

        title.text = if (user == null) "Tambah Pengguna" else "Edit Pengguna"
        if (user != null) { // Mode Edit
            name.setText(user.name)
            email.setText(user.email)
            roleSpinner.setSelection(if (user.role.equals("admin", ignoreCase = true)) 0 else 1)
            Glide.with(this).load(user.photoUrl).circleCrop().placeholder(R.drawable.ic_baseline_person_24).into(photoPreview)
        }

        selectPhotoButton.setOnClickListener { galleryLauncher.launch("image/*") }

        editDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Simpan", null) // Set listener-nya ke null dulu
            .setNegativeButton("Batal") { d, _ -> d.cancel() }
            .setOnDismissListener {
                selectedImageUri = null // Reset URI saat dialog ditutup
                editDialog = null
            }
            .create()

        // Tampilkan dialog, lalu atur listener untuk tombol "Simpan"
        editDialog?.show()
        editDialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {

            val newName = name.text.toString()
            val newEmail = email.text.toString()
            val newPassword = password.text.toString()

            // Validasi input
            if (newName.isBlank() || newEmail.isBlank()) {
                Toast.makeText(context, "Nama dan Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validasi password minimal 8 karakter HANYA jika diisi
            if (newPassword.isNotEmpty() && newPassword.length < 8) {
                Toast.makeText(context, "Password minimal harus 8 karakter", Toast.LENGTH_SHORT).show()
                password.error = "Minimal 8 karakter"
                return@setOnClickListener // Mencegah dialog tertutup
            }

            // Jika validasi lolos, lanjutkan proses
            val userData = mutableMapOf<String, String>()
            userData["name"] = newName
            userData["email"] = newEmail
            userData["role"] = roleSpinner.selectedItem.toString().lowercase()
            if (newPassword.isNotEmpty()) {
                userData["password"] = newPassword
            }

            var photoDataPart: VolleyMultipartRequest.DataPart? = null
            selectedImageUri?.let { uri ->
                try {
                    val inputStream = requireContext().contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()
                    if (bytes != null) {
                        photoDataPart = VolleyMultipartRequest.DataPart("photo.jpg", bytes)
                    }
                } catch (e: IOException) { e.printStackTrace() }
            }

            if (user == null) { // Create
                viewModel.createUser(userData, photoDataPart)
            } else { // Update
                viewModel.updateUser(user, userData, photoDataPart)
            }

            editDialog?.dismiss()
        }
    }
}
