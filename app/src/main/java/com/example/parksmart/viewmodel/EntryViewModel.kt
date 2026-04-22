package com.example.parksmart.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parksmart.data.model.ParkingSession
import com.example.parksmart.data.model.VehicleType
import com.example.parksmart.data.repository.ParkingRepository
import com.example.parksmart.data.storage.SupabaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

data class EntryUiState(
    val licensePlate: String = "",
    val selectedVehicleType: VehicleType = VehicleType.VOITURE,
    val photoUri: Uri? = null,
    val isUploading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class EntryViewModel @Inject constructor(
    private val repository: ParkingRepository,
    private val storage: SupabaseStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(EntryUiState())
    val uiState: StateFlow<EntryUiState> = _uiState.asStateFlow()

    fun onLicensePlateChange(plate: String) {
        _uiState.update { it.copy(licensePlate = plate.uppercase()) }
    }

    fun onVehicleTypeChange(type: VehicleType) {
        _uiState.update { it.copy(selectedVehicleType = type) }
    }

    fun onPhotoSelected(uri: Uri?) {
        _uiState.update { it.copy(photoUri = uri) }
    }

    fun saveEntry() {
        val currentState = _uiState.value
        if (currentState.licensePlate.isBlank()) {
            _uiState.update { it.copy(error = "La plaque d'immatriculation est requise") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, error = null) }

            try {
                // ✅ Encode l'image en Base64 (au lieu d'uploader vers Storage)
                var photoBase64: String? = null
                currentState.photoUri?.let { uri ->
                    val encodeResult = storage.encodeImageToBase64(uri)
                    if (encodeResult.isSuccess) {
                        photoBase64 = encodeResult.getOrNull()
                    } else {
                        println(">>> Encodage photo échoué: ${encodeResult.exceptionOrNull()?.message}")
                    }
                }

                // Création de la session
                val session = ParkingSession(
                    id = UUID.randomUUID().toString(),
                    licensePlate = currentState.licensePlate,
                    vehicleType = currentState.selectedVehicleType,
                    entryTime = Instant.now(),
                    photoUrl = photoBase64,
                    status = com.example.parksmart.data.model.SessionStatus.ACTIVE
                )

                val result = repository.createSession(session)
                if (result.isSuccess) {
                    _uiState.update { it.copy(isUploading = false, isSuccess = true) }
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "Erreur lors de l'enregistrement"
                    _uiState.update { it.copy(isUploading = false, error = errorMsg) }
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(isUploading = false, error = e.message ?: "Une erreur est survenue") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun resetState() {
        _uiState.value = EntryUiState()
    }
}