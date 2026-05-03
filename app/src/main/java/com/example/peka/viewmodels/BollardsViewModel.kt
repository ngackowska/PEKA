package com.example.peka.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.peka.api.BollardItem
import com.example.peka.api.pekaApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BollardsViewModel : ViewModel() {
    private val _bollards = MutableStateFlow<List<BollardItem>>(emptyList())
    val bollards: StateFlow<List<BollardItem>> = _bollards

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun fetchBollards(stopName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val p0Json = "{\"name\":\"$stopName\"}"

                val response = pekaApiService.getBollardsByStopPoint(p0 = p0Json)
                _bollards.value = response.success.bollards
            } catch (e: Exception) {
                e.printStackTrace()
                _bollards.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}