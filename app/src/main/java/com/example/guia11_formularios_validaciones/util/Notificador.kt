package com.example.guia11_formularios_validaciones.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

// Avisos locales: notificación del sistema (si el usuario dio permiso) además de la lista de avisos dentro de la app
object Notificador {
    private const val CANAL = "avisos_solicitudes"

    fun mostrar(context: Context, titulo: String, texto: String) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        if (Build.VERSION.SDK_INT >= 26) {
            val canal = NotificationChannel(CANAL, "Avisos de solicitudes", NotificationManager.IMPORTANCE_DEFAULT)
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
        }

        val notificacion = NotificationCompat.Builder(context, CANAL)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(1001, notificacion)
    }
}
