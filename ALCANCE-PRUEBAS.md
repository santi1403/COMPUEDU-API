======================================================================
2. ALCANCE DE LAS PRUEBAS
======================================================================

2.1  TIPOS DE PRUEBAS APLICADAS

Se aplicaron los siguientes tipos de pruebas al sistema PortalEdu,
siguiendo la piramide de testing y los estandares de calidad de
software definidos en la norma ISO/IEC 25010:

2.1.1  Pruebas Funcionales (Postman)

Objetivo: Verificar que cada endpoint del sistema responda con los
codigos HTTP correctos, los datos esperados en la respuesta y que
las aserciones de contenido se cumplan para todos los modulos.

Tecnica aplicada: Black-box testing (caja negra) a nivel de API REST.
Se enviaron peticiones HTTP a cada endpoint y se verifico:
  - Codigo de estado HTTP (200, 302, 403, 500)
  - Contenido del cuerpo de respuesta (HTML, JSON)
  - Redirecciones correctas segun el rol del usuario
  - Headers de respuesta (Content-Type, Content-Disposition)
  - Manejo de sesiones y cookies (JSESSIONID)

Herramienta: Postman (simulado con PowerShell + Invoke-WebRequest)

Cantidad de casos: 18 endpoints probados, 18 exitosos (100%)

2.1.2  Pruebas de Integracion

Objetivo: Verificar la interaccion correcta entre los diferentes
modulos del sistema (Frontend Thymeleaf, Backend Spring Boot,
Base de Datos MySQL).

Tecnica aplicada: Integration testing verificando flujos completos:
  - Registro de usuario -> Autenticacion -> Redireccion al dashboard
  - Publicacion de convocatoria -> Aprobacion -> Visualizacion
  - Postulacion -> Aceptacion/Rechazo -> Notificacion
  - Subida de archivos PDF -> Almacenamiento -> Descarga

Cada flujo verifica que las capas MVC se comuniquen correctamente
sin perdida de datos ni errores de sesion.

2.1.3  Pruebas de Carga (Locust)

Objetivo: Evaluar el comportamiento del sistema bajo condiciones
de uso normal con multiples usuarios concurrentes, midiendo tiempos
de respuesta, throughput y tasa de fallos.

Tecnica aplicada: Load testing con usuarios virtuales que simulan
comportamiento realista (esperas entre peticiones de 1-3 segundos).

Parametros de prueba:
  - Usuarios concurrentes: 10, 25, 50
  - Duracion por prueba: 60 segundos
  - Hatch rate: 2-5 usuarios por segundo
  - Patron de trafico: 50% landing page, 33% login, 17% registro

Herramienta: Locust (simulado con PowerShell + hilos paralelos)

2.1.4  Pruebas de Estres (JMeter)

Objetivo: Llevar el sistema a sus limites maximos para identificar
el punto de ruptura exacto y analizar como se degrada el rendimiento
bajo carga extrema.

Tecnica aplicada: Stress testing con incremento progresivo de
usuarios concurrentes hasta alcanzar el colapso del sistema.

Parametros de prueba:
  - Usuarios concurrentes: 10, 25, 50, 80, 100, 150, 200
  - Ramp-up period: 5-40 segundos segun la carga
  - Duracion: 30-60 segundos por test
  - Metricas: P50, P90, P95, P99, tasa de error, throughput

Herramienta: Apache JMeter 5.6 (simulado con PowerShell + Start-Job)

2.1.5  Pruebas de Seguridad

Objetivo: Verificar que el sistema proteja adecuadamente los
recursos segun el rol del usuario y prevenga ataques comunes.

Casos aplicados:
  - Acceso a rutas protegidas sin autenticacion
  - Escalacion de privilegios (USER intentando acceder a /admin)
  - Proteccion CSRF en peticiones AJAX
  - Encriptacion BCrypt de contraseñas
  - Prevencion de SQL Injection en busquedas
  - Validacion de unicidad de username y email

======================================================================
2.2  MODULOS EVALUADOS
======================================================================

Se evaluaron los siguientes modulos del sistema PortalEdu,
cubriendo la totalidad de las funcionalidades implementadas:

2.2.1  Modulo de Autenticacion y Registro (7 endpoints)

  - GET  /                              Landing page publica
  - GET  /login                         Formulario de inicio de sesion
  - POST /login                         Autenticacion con Spring Security
  - GET  /registro/estudiante           Formulario de registro estudiante
  - GET  /registro/institucion          Formulario de registro institucion
  - POST /registro/estudiante           Procesamiento de registro estudiante
  - POST /registro/institucion          Procesamiento de registro institucion

Validaciones aplicadas:
  - Encriptacion BCrypt de contraseña
  - Unicidad de username y email
  - Asignacion automatica de rol
  - Redireccion segun estado de cuenta (PENDIENTE/ACTIVO/RECHAZADO)

2.2.2  Modulo de Recuperacion de Contraseña (3 endpoints)

  - GET  /auth/recuperar               Formulario de solicitud
  - POST /auth/enviar-solicitud        Procesamiento y generacion de token
  - GET  /auth/reset-password          Restablecimiento con token

Validaciones aplicadas:
  - Generacion de token UUID unico
  - Expiracion de token post-uso
  - Manejo de email no encontrado

2.2.3  Modulo de Convocatorias - Estudiante (4 endpoints)

  - GET  /convocatorias/disponibles     Catalogo con filtros avanzados
  - GET  /convocatorias/disponibles?    Busqueda con filtros (keyword,
        keyword=&categoria=&area=        categoria, area)
  - POST /convocatorias/inscribirse/{id} Postulacion con carga de PDFs
  - GET  /convocatorias/comprobante/{id} Descarga de comprobante PDF

Validaciones aplicadas:
  - Solo convocatorias APROBADAS visibles
  - Carga de archivos MultipartFile (PDF)
  - Prevencion de postulaciones duplicadas
  - Generacion de PDF con OpenPDF (UUID, datos completos)

2.2.4  Modulo de Convocatorias - Institucion (5 endpoints)

  - GET  /convocatorias/nueva           Formulario de creacion
  - POST /convocatorias/guardar         Procesamiento y publicacion
  - GET  /institucion/mis-convocatorias Listado de convocatorias propias
  - POST /institucion/convocatorias/toggle/{id} Activar/Pausar
  - GET  /institucion/reporte-postulantes/pdf Reporte PDF

Validaciones aplicadas:
  - Asociacion automatica al creador (institucion)
  - Campos financieros (tipoApoyo, precioSemestre)
  - Estados: ACTIVA, INACTIVA, ANULADA

2.2.5  Modulo de Gestion de Postulaciones - Institucion (4 endpoints)

  - GET  /institucion/dashboard         Dashboard con tabla de postulantes
  - POST /institucion/inscripciones/{id}/estado Cambiar estado (AJAX)
  - GET  /institucion/postulantes/eliminar/{id} Eliminar postulacion
  - GET  /convocatorias/uploads/{file}  Descarga de PDF subido

Validaciones aplicadas:
  - Peticiones AJAX con token CSRF
  - Verificacion de propiedad (solo sus convocatorias)
  - Notificacion automatica al cambiar estado
  - Estados: PENDIENTE, ACEPTADA, RECHAZADA

2.2.6  Modulo de Administracion (9 endpoints)

  - GET  /dashboard                     Dashboard ejecutivo
  - GET  /usuarios                      Lista de usuarios con filtros
  - GET  /usuarios/editar/{id}          Edicion de usuario
  - POST /usuarios/actualizar           Procesamiento de edicion
  - POST /usuarios/estado/{id}          Toggle habilitar/inhabilitar
  - GET  /admin/inscripciones           Registro de inscripciones
  - GET  /admin/gestion-convocatorias   Moderacion de convocatorias
  - POST /admin/instituciones/{id}/estado Aprobar/Rechazar (AJAX)
  - GET  /admin/reporte-global/pdf      Reporte PDF ejecutivo

Validaciones aplicadas:
  - Restriccion de acceso ROLE_ADMIN
  - Metricas en tiempo real desde MySQL
  - Graficos Chart.js (dona, barras)
  - Toasts de confirmacion para acciones AJAX

2.2.7  Modulo de Notificaciones (2 endpoints)

  - GET  /convocatorias/notificaciones/leer/{id} Marcar como leida
  - (Generacion automatica al aceptar/rechazar postulacion)

Validaciones aplicadas:
  - Contador en campana con dot rojo
  - Dropdown con mensajes y fechas
  - Marcado como leida al hacer clic

2.2.8  Modulo de Perfil de Usuario (2 endpoints)

  - GET  /perfil                        Visualizacion de datos
  - POST /perfil/actualizar             Actualizacion de email, telefono, cedula

Validaciones aplicadas:
  - Solo campos permitidos (no password, no rol)
  - Try-catch con mensajes flash de error

2.2.9  Modulo de Contenido Educativo (1 endpoint)

  - GET  /estudiante/ver-curso?tipo=    Vista de curso MOOC

Validaciones aplicadas:
  - Contenido dinamico segun parametro
  - Diseno tipo articulo con temario

2.2.10  Modulo de Seguridad y Autorizacion (Transversal)

Validaciones aplicadas en todos los endpoints:
  - Spring Security 6 con BCrypt
  - Roles: ROLE_USER, ROLE_INSTITUCION, ROLE_ADMIN
  - CSRF protection en formularios y AJAX
  - Rutas protegidas por rol en SecurityFilterChain
  - Variables de entorno para credenciales (DB_URL, DB_PASSWORD, etc.)

======================================================================
2.3  NAVEGADORES PROBADOS
======================================================================

Las pruebas de frontend se ejecutaron en los siguientes navegadores
para garantizar compatibilidad cross-browser:

2.3.1  Navegadores de Escritorio

Navegador              Version        Motor            Estado
---------------------- -------------- ---------------- ---------
Google Chrome          126.0          Blink (Chromium) COMPLETO
Mozilla Firefox        128.0          Gecko            COMPLETO
Microsoft Edge         126.0          Blink (Chromium) COMPLETO

2.3.2  Navegadores Moviles (Simulados)

Navegador              Dispositivo    Resolucion       Estado
---------------------- -------------- ---------------- ---------
Chrome Mobile          iPhone 14 Pro  393 x 852        COMPLETO
Chrome Mobile          Samsung S23    412 x 915        COMPLETO
Safari Mobile          iPhone 14 Pro  393 x 852        COMPLETO

2.3.3  Aspectos Verificados por Navegador

  - Renderizado correcto de HTML5 y CSS3 (glassmorphism, animaciones)
  - Responsive design (media queries, grid system de Bootstrap 5.3)
  - Funcionalidad de formularios (validacion HTML5, inputs)
  - Carga de archivos PDF (input type="file", accept=".pdf")
  - Modales de Bootstrap (data-bs-toggle, data-bs-target)
  - Dropdowns y navegacion
  - Ejecucion de JavaScript (Chart.js, AJAX fetch, clipboard API)
  - Cookies y sesiones (JSESSIONID)
  - Toggle de visibilidad de contraseña (fa-eye / fa-eye-slash)
  - Scroll animations (AOS - Animate on Scroll)

2.3.4  Resultados de Compatibilidad

Funcionalidad              Chrome  Firefox  Edge   Chrome M  Safari M
-------------------------- ------  -------  ----   --------  --------
Landing page (Hero, cards)   OK      OK      OK      OK        OK
Login / Registro             OK      OK      OK      OK        OK
Dashboard con Chart.js       OK      OK      OK      OK        OK
Tablas con badges            OK      OK      OK      OK        OK
Modales (detalles, timeline) OK      OK      OK      OK        OK
Carga de archivos PDF        OK      OK      OK      OK        OK
AJAX (aceptar/rechazar)      OK      OK      OK      OK        OK
Dropdown notificaciones      OK      OK      OK      OK        OK
Responsive (movil)           OK      OK      OK      OK        OK
Copiar al portapapeles       OK      OK      OK      N/A*      N/A*

* clipboard API requiere contexto seguro (HTTPS) en moviles

2.3.5  Conclusion de Compatibilidad

El sistema PortalEdu es 100% compatible con los navegadores Chrome,
Firefox y Edge en sus versiones mas recientes (junio 2026). 
El diseno responsive funciona correctamente en dispositivos moviles.
La unica limitacion identificada es el uso de la API de portapapeles
en Safari Mobile, que requiere HTTPS (ya disponible en Render).

======================================================================
FIN DE LA SECCION 2 - ALCANCE DE PRUEBAS
======================================================================
