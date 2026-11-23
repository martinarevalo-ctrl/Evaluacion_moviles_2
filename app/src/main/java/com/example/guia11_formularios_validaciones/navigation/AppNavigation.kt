package com.example.guia11_formularios_validaciones.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.camara.ui.view.QrScannerScreen
import com.example.camara.ui.viewmodel.QrViewModel
import com.example.guia11_formularios_validaciones.ui.screen.LoginScreen
import com.example.guia11_formularios_validaciones.ui.screen.AvisosScreen
import com.example.guia11_formularios_validaciones.ui.screen.CatalogoApiScreen
import com.example.guia11_formularios_validaciones.ui.screen.MenuScreen
import com.example.guia11_formularios_validaciones.ui.screen.PanelResumenScreen
import com.example.guia11_formularios_validaciones.ui.solicitud.ListaSolicitudesScreen
import com.example.guia11_formularios_validaciones.ui.solicitud.SolicitudDetailScreen
import com.example.guia11_formularios_validaciones.ui.solicitud.SolicitudFormScreen
import com.example.guia11_formularios_validaciones.viewmodel.AuthViewModel
import com.example.guia11_formularios_validaciones.viewmodel.SolicitudViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val solicitudViewModel: SolicitudViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    val qrViewModel: QrViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "login",
        enterTransition = { fadeIn(animationSpec = tween(700)) + slideInHorizontally() },
        exitTransition = { fadeOut(animationSpec = tween(700)) + slideOutHorizontally() }
    ) {
        composable("login") {
            LoginScreen(navController, authViewModel, solicitudViewModel)
        }
        composable("menu") {
            MenuScreen(navController, solicitudViewModel)
        }
        composable("lista_solicitudes") {
            ListaSolicitudesScreen(navController, solicitudViewModel)
        }
        composable("solicitud_form") {
            SolicitudFormScreen(navController, solicitudViewModel)
        }
        composable("solicitud_detalle") {
            SolicitudDetailScreen(navController, solicitudViewModel)
        }
        composable("panel_resumen") {
            PanelResumenScreen(navController, solicitudViewModel)
        }
        composable("avisos") {
            AvisosScreen(navController, solicitudViewModel)
        }
        composable("qr_scanner") {
            QrScannerScreen(
                viewModel = qrViewModel,
                solicitudViewModel = solicitudViewModel,
                navController = navController
            )
        }
        composable("catalogo_api") {
            CatalogoApiScreen(navController, solicitudViewModel)
        }
    }
}
