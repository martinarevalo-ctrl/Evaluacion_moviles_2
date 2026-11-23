package com.example.guia11_formularios_validaciones.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.guia11_formularios_validaciones.model.data.local.AppDatabase
import com.example.guia11_formularios_validaciones.model.data.local.UsuarioEntity
import com.example.guia11_formularios_validaciones.model.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val username: String = "cliente",
    val password: String = "cliente123",
    val usernameError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val isLoginSuccessful: Boolean = false
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UsuarioRepository(AppDatabase.getDatabase(application).usuarioDao())

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        // Carga los perfiles ficticios (cliente, administrador, técnico) la primera vez
        viewModelScope.launch { repository.asegurarPerfiles() }
    }

    fun onUsernameChanged(username: String) {
        _uiState.update { it.copy(username = username, usernameError = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null) }
    }

    // Selecciona un perfil de ejemplo y rellena sus credenciales
    fun seleccionarPerfil(usuario: String, contrasena: String) {
        _uiState.update {
            it.copy(username = usuario, password = contrasena, usernameError = null, passwordError = null)
        }
    }

    fun login(onSuccess: (UsuarioEntity) -> Unit) {
        val current = _uiState.value
        val usuarioErr = if (current.username.isBlank()) "El usuario es requerido" else null
        val passErr = if (current.password.isBlank()) "La contraseña es requerida" else null

        if (usuarioErr != null || passErr != null) {
            _uiState.update { it.copy(usernameError = usuarioErr, passwordError = passErr) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val usuario = repository.autenticar(current.username, current.password)
            if (usuario != null) {
                _uiState.update { it.copy(isLoading = false) }
                onSuccess(usuario)
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        usernameError = "Credenciales incorrectas",
                        passwordError = "Credenciales incorrectas"
                    )
                }
            }
        }
    }
}
