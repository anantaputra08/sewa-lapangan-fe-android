package com.example.booking_lapangan.ui.admin.bookings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.booking_lapangan.api.AdminBookingApi
import com.example.booking_lapangan.api.Booking
import com.example.booking_lapangan.api.BookingCreationData
import org.json.JSONObject

class AdminBookingViewModel(application: Application) : AndroidViewModel(application) {
    private val _bookings = MutableLiveData<List<Booking>>()
    val bookings: LiveData<List<Booking>> = _bookings

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _actionSuccess = MutableLiveData<String?>()
    val actionSuccess: LiveData<String?> = _actionSuccess

    private val _creationData = MutableLiveData<BookingCreationData>()
    val creationData: LiveData<BookingCreationData> = _creationData

    fun fetchBookings() {
        _isLoading.value = true
        AdminBookingApi.getBookings(getApplication(),
            onSuccess = { bookingList ->
                _bookings.value = bookingList
                _isLoading.value = false
            },
            onError = { exception ->
                _error.value = "Gagal memuat booking: ${exception.message}"
                _isLoading.value = false
            }
        )
    }

    fun fetchCreationData() {
        _isLoading.value = true
        _error.value = null

        AdminBookingApi.getCreationData(getApplication(),
            onSuccess = { data ->
                try {
                    _creationData.value = data ?: BookingCreationData()
                    _isLoading.value = false
                } catch (e: Exception) {
                    _error.value = "Gagal memproses data creation: ${e.message}"
                    _isLoading.value = false
                }
            },
            onError = { exception ->
                _error.value = "Gagal memuat data creation: ${exception.message}"
                _isLoading.value = false
            }
        )
    }

    fun createBooking(params: JSONObject) {
        if (params.length() == 0) {
            _error.value = "Parameter booking tidak valid"
            return
        }

        _isLoading.value = true
        _error.value = null

        AdminBookingApi.createBooking(getApplication(), params,
            onSuccess = { message ->
                try {
                    _actionSuccess.value = message ?: "Booking berhasil dibuat"
                    _isLoading.value = false
                } catch (e: Exception) {
                    _error.value = "Gagal memproses response: ${e.message}"
                    _isLoading.value = false
                }
            },
            onError = { exception ->
                _error.value = "Gagal membuat booking: ${exception.message}"
                _isLoading.value = false
            }
        )
    }

    fun updateBooking(booking: Booking, status: String, paymentStatus: String) {
        _isLoading.value = true
        AdminBookingApi.updateBooking(getApplication(), booking, status, paymentStatus,
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

    fun deleteBooking(booking: Booking) {
        _isLoading.value = true
        AdminBookingApi.deleteBooking(getApplication(), booking,
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

    fun clearError() {
        _error.value = null
    }
}
