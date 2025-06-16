package com.example.booking_lapangan.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.booking_lapangan.LoginActivity
import com.example.booking_lapangan.R
import com.example.booking_lapangan.api.VolleyMultipartRequest
import java.io.IOException

class ProfileFragment : Fragment() {

    private lateinit var viewModel: ProfileViewModel

    // Deklarasikan semua view yang akan digunakan
    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var phoneTextView: TextView
    private lateinit var profileImageView: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var logoutButton: Button
    private lateinit var editButton: ImageButton

    // Variabel untuk menyimpan URI gambar yang dipilih dan referensi dialog
    private var selectedImageUri: Uri? = null
    private var editDialog: AlertDialog? = null

    // Launcher baru untuk memilih konten (gambar) dari galeri
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            // Tampilkan pratinjau gambar yang dipilih di dalam dialog
            editDialog?.findViewById<ImageView>(R.id.image_view_edit_photo_preview)?.let { preview ->
                Glide.with(this).load(it).circleCrop().into(preview)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        // Hubungkan view dari layout XML
        nameTextView = view.findViewById(R.id.text_view_name)
        emailTextView = view.findViewById(R.id.text_view_email)
        phoneTextView = view.findViewById(R.id.text_view_phone)
        profileImageView = view.findViewById(R.id.image_view_profile)
        progressBar = view.findViewById(R.id.progress_bar)
        logoutButton = view.findViewById(R.id.button_logout)
        editButton = view.findViewById(R.id.button_edit_profile)

        setupObservers()
        logoutButton.setOnClickListener { showLogoutConfirmationDialog() }
        editButton.setOnClickListener { showEditProfileDialog() }
    }

    private fun showEditProfileDialog() {
        val currentUser = viewModel.user.value ?: return
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_profile, null)

        // Inisialisasi view di dalam dialog
        val nameEditText = dialogView.findViewById<EditText>(R.id.edit_text_name)
        val phoneEditText = dialogView.findViewById<EditText>(R.id.edit_text_phone)
        val emailEditText = dialogView.findViewById<EditText>(R.id.edit_text_email)
        val addressEditText = dialogView.findViewById<EditText>(R.id.edit_text_address)
        val photoPreview = dialogView.findViewById<ImageView>(R.id.image_view_edit_photo_preview)
        val changePhotoButton = dialogView.findViewById<Button>(R.id.button_change_photo)

        // Isi data saat ini ke dalam dialog
        nameEditText.setText(currentUser.name)
        emailEditText.setText(currentUser.email)
        phoneEditText.setText(currentUser.phone)
        addressEditText.setText(currentUser.address ?: "")
        Glide.with(this).load(currentUser.photoUrl).placeholder(R.drawable.ic_baseline_person_24).circleCrop().into(photoPreview)

        // Tambahkan listener untuk tombol ubah foto
        changePhotoButton.setOnClickListener {
            galleryLauncher.launch("image/*") // Buka galeri
        }

        editDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Edit Profil")
            .setPositiveButton("Simpan") { _, _ ->
                val profileData = mutableMapOf<String, String>()
                profileData["name"] = nameEditText.text.toString()
                profileData["email"] = currentUser.email ?: "Email tidak tersedia"
                profileData["phone"] = phoneEditText.text.toString()
                profileData["address"] = currentUser.address ?: "" // Tambahkan alamat jika ada

                var photoDataPart: VolleyMultipartRequest.DataPart? = null
                selectedImageUri?.let { uri ->
                    try {
                        // Ubah URI gambar menjadi byte array untuk dikirim
                        val inputStream = requireContext().contentResolver.openInputStream(uri)
                        val bytes = inputStream?.readBytes()
                        inputStream?.close()
                        if (bytes != null) {
                            photoDataPart = VolleyMultipartRequest.DataPart("profile.jpg", bytes, "image/jpeg")
                        }
                    } catch (e: IOException) {
                        Toast.makeText(context, "Gagal memproses gambar", Toast.LENGTH_SHORT).show()
                        e.printStackTrace()
                    }
                }

                // Panggil ViewModel dengan data teks dan data gambar (jika ada)
                viewModel.updateProfile(profileData, photoDataPart)
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
                selectedImageUri = null // Reset URI jika dibatalkan
            }
            .show()
    }

    private fun setupObservers() {
        // Mengamati data user
        viewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                nameTextView.text = it.name
                emailTextView.text = it.email
                phoneTextView.text = it.phone ?: "Belum ditambahkan"

                Glide.with(this)
                    .load(it.photoUrl)
                    .placeholder(R.drawable.ic_baseline_person_24)
                    .error(R.drawable.ic_baseline_person_24)
                    .circleCrop()
                    .into(profileImageView)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
        }
        viewModel.logoutSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                val intent = Intent(activity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                activity?.finish()
            }
        }
        viewModel.updateSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(context, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                viewModel.onUpdateHandled()
            }
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Konfirmasi Logout")
            .setMessage("Apakah Anda yakin ingin keluar?")
            .setPositiveButton("Ya") { _, _ -> viewModel.logout() }
            .setNegativeButton("Tidak", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchProfile()
    }
}
