package com.example.guia11_formularios_validaciones

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.guia11_formularios_validaciones.navigation.AppNavigation
import com.example.guia11_formularios_validaciones.ui.theme.Guia11Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Guia11Theme {
                AppNavigation()
            }
        }
    }
}
