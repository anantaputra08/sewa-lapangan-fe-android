package com.example.booking_lapangan.ui.admin.user

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.booking_lapangan.api.AdminUserApi
import com.example.booking_lapangan.api.User
import com.example.booking_lapangan.api.VolleyMultipartRequest

class AdminUserViewModel(application: Application) : AndroidViewModel(application) {
    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _actionSuccess = MutableLiveData<String?>()
    val actionSuccess: LiveData<String?> = _actionSuccess

    fun fetchUsers() {
        _isLoading.value = true
        AdminUserApi.getUsers(getApplication(),
            onSuccess = { userList ->
                _users.value = userList
                _isLoading.value = false
            },
            onError = { exception ->
                _error.value = "Gagal memuat pengguna: ${exception.message}"
                _isLoading.value = false
            }
        )
    }

    fun createUser(userData: Map<String, String>, photoData: VolleyMultipartRequest.DataPart?) {
        _isLoading.value = true
        AdminUserApi.createUser(getApplication(), userData, photoData,
            onSuccess = { message ->
                _actionSuccess.value = message
                _isLoading.value = false
            },
            onError = { exception ->
                _error.value = exception.message
                _isLoading.value = false
            }
        )
    }

    fun updateUser(user: User, userData: Map<String, String>, photoData: VolleyMultipartRequest.DataPart?) {
        _isLoading.value = true
        AdminUserApi.updateUser(getApplication(), user, userData, photoData,
            onSuccess = { message ->
                _actionSuccess.value = message
                _isLoading.value = false
            },
            onError = { exception ->
                _error.value = exception.message
                _isLoading.value = false
            }
        )
    }

    fun deleteUser(user: User) {
        _isLoading.value = true
        AdminUserApi.deleteUser(getApplication(), user,
            onSuccess = { message ->
                _actionSuccess.value = message
                _isLoading.value = false
            },
            onError = { exception ->
                _error.value = exception.message
                _isLoading.value = false
            }
        )
    }

    fun onActionDone() {
        _actionSuccess.value = null
    }
}
