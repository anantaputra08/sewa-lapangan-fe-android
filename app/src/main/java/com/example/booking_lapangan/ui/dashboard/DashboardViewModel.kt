package com.example.booking_lapangan.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.booking_lapangan.api.Lapangan
import com.example.booking_lapangan.api.LapanganApi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val _lapangans = MutableLiveData<List<Lapangan>>()
    val lapangans: LiveData<List<Lapangan>> = _lapangans

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _selectedDate = MutableLiveData<String>()
    val selectedDate: LiveData<String> = _selectedDate

    init {
        // Set today's date as the default
        val defaultDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        setSelectedDate(defaultDate)
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
        fetchAvailableLapangans()
    }

    fun fetchAvailableLapangans() {
        _isLoading.value = true
        _error.value = null

        val dateToFetch = _selectedDate.value ?: return

        LapanganApi.getAvailableLapangans(getApplication(), dateToFetch,
            onSuccess = { result ->
                _lapangans.value = result
                _isLoading.value = false
            },
            onError = { exception ->
                _error.value = exception.message
                _isLoading.value = false
            }
        )
    }
}
