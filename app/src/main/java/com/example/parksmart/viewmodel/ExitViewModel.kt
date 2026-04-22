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
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class ExitUiState(
    val session: ParkingSession? = null,
    val isLoading: Boolean = false,
    val isProcessingPayment: Boolean = false,
    val isCompleted: Boolean = false,
    val error: String? = null,
    val currentCost: Double = 0.0
)

@HiltViewModel
class ExitViewModel @Inject constructor(
    private val repository: ParkingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExitUiState())
    val uiState: StateFlow<ExitUiState> = _uiState.asStateFlow()

    fun loadSessionById(id: String) {
        viewModelScope.launch {
            _uiState.value = ExitUiState(isLoading = true)

            // Charge la session depuis le repository
            repository.getAllSessions().collect { result ->
                result.fold(
                    onSuccess = { sessions ->
                        val session = sessions.find { it.id == id }
                        if (session != null) {
                            _uiState.value = ExitUiState(
                                session = session,
                                currentCost = session.currentCost
                            )
                        } else {
                            _uiState.value = ExitUiState(error = "Session non trouvée")
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = ExitUiState(error = error.message ?: "Erreur de chargement")
                    }
                )
            }
        }
    }

    // Mise à jour du coût en temps réel
    fun startCostUpdates() {
        viewModelScope.launch {
            while (uiState.value.session != null && !uiState.value.isCompleted) {
                delay(1000)
                uiState.value.session?.let { session ->
                    _uiState.value = _uiState.value.copy(currentCost = session.currentCost)
                }
            }
        }
    }

    fun processExit() {
        val currentState = _uiState.value
        val session = currentState.session ?: return

        viewModelScope.launch {
            _uiState.value = currentState.copy(isProcessingPayment = true, error = null)

            val exitTime = Instant.now()
            val finalAmount = currentState.currentCost

            repository.completeSession(session.id, exitTime, finalAmount).fold(
                onSuccess = {
                    _uiState.value = ExitUiState(isCompleted = true)
                },
                onFailure = { error ->
                    _uiState.value = currentState.copy(
                        isProcessingPayment = false,
                        error = error.message ?: "Erreur lors du paiement"
                    )
                }
            )
        }
    }

    fun resetState() {
        _uiState.value = ExitUiState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}