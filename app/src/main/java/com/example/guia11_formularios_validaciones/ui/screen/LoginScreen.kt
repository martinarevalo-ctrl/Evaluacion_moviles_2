package com.example.guia11_formularios_validaciones.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.guia11_formularios_validaciones.viewmodel.AuthViewModel
import com.example.guia11_formularios_validaciones.viewmodel.SolicitudViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, viewModel: AuthViewModel, solicitudViewModel: SolicitudViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Servicios Digitales",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Gestión de solicitudes",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Perfiles de ejemplo: rellenan usuario y contraseña ficticios
        Text("Perfiles", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = uiState.username == "cliente",
                onClick = { viewModel.seleccionarPerfil("cliente", "cliente123") },
                label = { Text("Cliente") }
            )
            FilterChip(
                selected = uiState.username == "admin",
                onClick = { viewModel.seleccionarPerfil("admin", "admin123") },
                label = { Text("Administrador") }
            )
            FilterChip(
                selected = uiState.username == "tecnico",
                onClick = { viewModel.seleccionarPerfil("tecnico", "tecnico123") },
                label = { Text("Técnico") }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = uiState.username,
            onValueChange = { viewModel.onUsernameChanged(it) },
            label = { Text("Usuario (ficticio)") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.usernameError != null,
            supportingText = {
                AnimatedVisibility(
                    visible = uiState.usernameError != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    uiState.usernameError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.password,
            onValueChange = { viewModel.onPasswordChanged(it) },
            label = { Text("Contraseña (ficticia)") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.passwordError != null,
            supportingText = {
                AnimatedVisibility(
                    visible = uiState.passwordError != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    uiState.passwordError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                viewModel.login { usuario ->
                    solicitudViewModel.iniciarSesion(usuario)
                    navController.navigate("menu") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("ENTRAR")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("💡 Credenciales de prueba:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text("cliente / cliente123  (también cliente2)", style = MaterialTheme.typography.bodySmall)
                Text("admin / admin123", style = MaterialTheme.typography.bodySmall)
                Text("tecnico / tecnico123  (también tecnico2)", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
