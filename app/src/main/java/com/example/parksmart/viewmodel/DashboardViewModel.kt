package com.example.parksmart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parksmart.data.model.ParkingSession
import com.example.parksmart.data.repository.ParkingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val sessions: List<ParkingSession> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val totalRevenue: Double = 0.0,
    val occupiedSpots: Int = 0,
    val tick: Int = 0  // ✅ Forcer le rafraîchissement de l'UI
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: ParkingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState(isLoading = true))
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadActiveSessions()
        startCostTicker()  // ✅ Démarre le timer pour les prix
    }

    private fun startCostTicker() {
        viewModelScope.launch {
            while (true) {
                delay(1000) // 1 seconde
                _uiState.update { it.copy(tick = it.tick + 1) } // Force recomposition
            }
        }
    }

    fun refreshSessions() {
        loadActiveSessions()
    }

    fun loadActiveSessions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getActiveSessions().collect { result ->
                result.fold(
                    onSuccess = { sessions ->
                        _uiState.update { 
                            it.copy(
                                sessions = sessions,
                                occupiedSpots = sessions.size,
                                isLoading = false,
                                error = null
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = error.message ?: "Erreur de chargement"
                            )
                        }
                    }
                )
            }
        }
    }

    fun deleteSession(id: String) {
        viewModelScope.launch {
            repository.deleteSession(id).fold(
                onSuccess = {
                    loadActiveSessions()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}