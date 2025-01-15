package com.happypet.app.activities.ui.onboarding.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class OnboardingViewModel :
    ViewModel() {

    // Mutable state flow to hold the initial value from datastore
    private val _isConnectedState = MutableStateFlow(false)
    val isConnectedState: StateFlow<Boolean> = _isConnectedState.asStateFlow()

    private val _isDayNightModeState = MutableStateFlow(false)
    val isDayNightModeState: StateFlow<Boolean> = _isDayNightModeState.asStateFlow()

    private val _getEmailState = MutableStateFlow("")
    val getEmailState: StateFlow<String> = _getEmailState.asStateFlow()

    private val _isOnboardingCompletedState = MutableStateFlow(false)
    val isOnboardingCompletedState: StateFlow<Boolean> = _isOnboardingCompletedState.asStateFlow()

    fun setOnboardingCompleted(completed: Boolean?) {
        viewModelScope.launch {
            if (completed != null) {
//                datastorePreferenceDatabase.setOnboardingCompleted(completed)
            }
        }
    }
}