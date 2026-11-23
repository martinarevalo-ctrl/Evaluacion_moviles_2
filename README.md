# Gestión de Solicitudes de Servicios Digitales

App móvil Android: Jetpack Compose · MVVM · Retrofit · Room (según el diagrama del caso).

## Perfiles de prueba (ficticios, guardados en Room)
| Perfil | Usuario | Contraseña |
|---|---|---|
| Cliente | cliente / cliente2 | cliente123 |
| Administrador / Coordinador | admin | admin123 |
| Técnico | tecnico / tecnico2 | tecnico123 |

## Flujo por perfil
- **Cliente:** crea solicitud (título, descripción, categoría, servicio del catálogo, evidencia foto/documento) → recibe código único + estado *Recibida* → comparte referencia por WhatsApp (simulado) → "Mis solicitudes" (estado, historial, comentarios) → responde solicitudes de información → confirma la resolución o pide reapertura.
- **Administrador:** lista con filtros (cliente, estado, prioridad, responsable) → revisar y clasificar → solicitar información adicional → definir prioridad y asignar técnico → panel resumen → cerrar o cancelar.
- **Técnico:** mis solicitudes asignadas → ver antecedentes (historial y comentarios) → iniciar atención → registrar avances y evidencias → proponer resolución.

Estados: Recibida → En revisión → (Pendiente de información) → Asignada → En proceso → Resuelta → Cerrada · Cancelada.

## Persistencia (Room, versión 3)
Tablas: `usuarios`, `solicitudes`, `historial`, `comentarios`, `avisos`. Al subir de versión se recrea la base (se pierden datos de la versión anterior).

## Avisos locales
Nueva solicitud recibida · solicitud asignada al técnico · actualización de estado · solicitud resuelta (pendiente de confirmación del cliente). Se ven en la pantalla *Avisos* (con contador) y como notificación del sistema si se concede el permiso.

## Catálogo externo (Firebase Realtime Database)
`https://solicitud-2d314-default-rtdb.firebaseio.com/` — se lee con Retrofit (`GET /.json`) en `CatalogoApi` / `CatalogoRepository`.
Estructura esperada: un nodo (`servicios`, `catalogo`… o el primer nodo con datos) con filas `{ id, servicio, categoria, descripcion }`, como arreglo o como mapa de claves. Acepta también `nombre` en lugar de `servicio`.
Las reglas de la base deben permitir lectura (`".read": true`). Si Firebase no responde, el formulario usa un catálogo de respaldo de 3 servicios.
