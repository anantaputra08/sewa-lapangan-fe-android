package com.example.booking_lapangan.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.booking_lapangan.api.ProfileApi
import com.example.booking_lapangan.api.User
import com.example.booking_lapangan.api.VolleyMultipartRequest

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _logoutSuccess = MutableLiveData<Boolean>()
    val logoutSuccess: LiveData<Boolean> = _logoutSuccess

    private val _updateSuccess = MutableLiveData<Boolean>()
    val updateSuccess: LiveData<Boolean> = _updateSuccess

    init {
        fetchProfile()
    }

    fun onUpdateHandled() {
        _updateSuccess.value = false
    }

    fun fetchProfile() {
        _isLoading.value = true
        ProfileApi.getProfile(getApplication(),
            onSuccess = { userData ->
                _user.value = userData
                _isLoading.value = false
            },
            onError = { exception ->
                _error.value = exception.message
                _isLoading.value = false
            }
        )
    }

    fun logout() {
        _isLoading.value = true
        ProfileApi.logout(getApplication(),
            onSuccess = {
                _logoutSuccess.value = true
                _isLoading.value = false
            },
            onError = { exception ->
                _error.value = "Logout failed: ${exception.message}"
                _isLoading.value = false
            }
        )
    }

    fun updateProfile(
        profileData: Map<String, String>,
        photoData: VolleyMultipartRequest.DataPart?
    ) {
        _isLoading.value = true
        ProfileApi.updateProfile(getApplication(), profileData, photoData,
            onSuccess = { updatedUser ->
                _user.value = updatedUser
                _updateSuccess.value = true
                _isLoading.value = false
            },
            onError = { exception ->
                _error.value = "Update failed: ${exception.message}"
                _isLoading.value = false
            }
        )
    }
}
