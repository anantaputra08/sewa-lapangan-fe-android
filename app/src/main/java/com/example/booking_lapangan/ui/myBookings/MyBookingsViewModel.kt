package com.example.booking_lapangan.ui.myBookings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.booking_lapangan.api.Booking
import com.example.booking_lapangan.api.BookingApi

class MyBookingsViewModel(application: Application) : AndroidViewModel(application) {

    private val _bookings = MutableLiveData<List<Booking>>()
    val bookings: LiveData<List<Booking>> = _bookings

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        fetchMyBookings()
    }

    fun fetchMyBookings() {
        _isLoading.value = true
        _error.value = null
        BookingApi.getMyBookings(getApplication(),
            onSuccess = { bookingList ->
                _bookings.value = bookingList.sortedByDescending { it.created_at }
                _isLoading.value = false
            },
            onError = { exception ->
                _error.value = exception.message
                _isLoading.value = false
            }
        )
    }
}
