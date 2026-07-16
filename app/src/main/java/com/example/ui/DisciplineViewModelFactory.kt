package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.DisciplineRepository
import com.example.data.NotificationHelper
import com.example.data.SettingsRepository

import com.example.data.QuoteApiService
import com.example.data.WorldTimeApiService

class DisciplineViewModelFactory(
    private val repository: DisciplineRepository,
    private val settingsRepository: SettingsRepository,
    private val notificationHelper: NotificationHelper,
    private val apiService: QuoteApiService,
    private val timeApiService: WorldTimeApiService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DisciplineViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DisciplineViewModel(repository, settingsRepository, notificationHelper, apiService, timeApiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
