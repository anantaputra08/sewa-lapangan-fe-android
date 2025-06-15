package com.example.booking_lapangan.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.booking_lapangan.api.BookingApi // Import BookingApi
import com.example.booking_lapangan.api.Session

class SessionViewModel(application: Application) : AndroidViewModel(application) {

    private val _sessions = MutableLiveData<List<Session>>()
    val sessions: LiveData<List<Session>> = _sessions

    private val _bookingResult = MutableLiveData<Result<String>>()
    val bookingResult: LiveData<Result<String>> = _bookingResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error



    fun fetchSessions(lapanganId: Int, date: String) {
        _isLoading.value = true
        _error.value = null

        // Gunakan BookingApi yang baru
        BookingApi.checkAvailability(getApplication(), lapanganId, date,
            onSuccess = { sessionList ->
                _sessions.value = sessionList
                _isLoading.value = false
            },
            onError = { exception ->
                _error.value = "Gagal memuat sesi: ${exception.message}"
                _isLoading.value = false
            }
        )
    }
    fun createBooking(lapanganId: Int, date: String, sessionIds: List<Int>) {
        BookingApi.createBooking(getApplication(), lapanganId, date, sessionIds,
            onSuccess = { message ->
                // Kirim hasil sukses ke UI
                _bookingResult.value = Result.success(message)
            },
            onError = { exception ->
                // Kirim hasil error ke UI
                _bookingResult.value = Result.failure(exception)
            }
        )
    }
}
