# 🌿 Calmio

**Calmio** es una aplicación móvil para Android orientada a la gestión del estrés y el bienestar emocional, dirigida especialmente a un público joven. Combina minijuegos de relajación, un diario emocional y un sistema de seguimiento del nivel de estrés antes y después de cada sesión, todo en una interfaz sencilla y pensada para transmitir calma.

Este proyecto ha sido desarrollado como Proyecto Final del ciclo de Desarrollo de Aplicaciones Multiplataforma (DAM).

---

## ✨ Funcionalidades principales

- **Registro e inicio de sesión** mediante correo electrónico y contraseña, con recuperación de contraseña.
- **Minijuegos de relajación** orientados a reducir el estrés mediante la distracción y la concentración.
- **Seguimiento del estrés**: el usuario registra su nivel de estrés antes y después de cada sesión.
- **Diario emocional** con preguntas guiadas y la posibilidad de escribir un pensamiento libre.
- **Historial visual** con la evolución del estrés, gráficos y la racha de días de uso.
- **Personalización**: distintos avatares y modo claro / oscuro.
- **Recordatorios diarios** configurables.
- **Sincronización en la nube**: los datos del usuario se guardan de forma segura.

---

## 🛠️ Tecnologías utilizadas

- **Lenguaje:** Kotlin
- **Interfaz:** Jetpack Compose
- **Arquitectura:** MVVM (Model-View-ViewModel)
- **Backend / Base de datos:** Firebase (Authentication + Firestore)
- **Preferencias locales:** DataStore
- **Notificaciones:** WorkManager
- **Navegación:** Navigation Compose

---

## 📋 Requisitos

- Dispositivo Android con versión **8.0 (API 26)** o superior.
- Conexión a internet (necesaria para la autenticación y la sincronización de datos).

---

## 🚀 Instalación

### Opción 1: Descargar el APK (recomendada)

1. Entra en la última versión: **[Descargar Calmio (APK)](https://github.com/fernandosnchz/Calmio/releases/latest)**
2. Descarga el archivo `Calmio.apk` desde la sección **Assets**.
3. En tu móvil Android, permite la instalación de apps de orígenes desconocidos.
4. Abre el APK y pulsa instalar.

### Opción 2: Ejecutar desde Android Studio

1. Clona este repositorio:
   ```
   git clone https://github.com/fernandosnchz/Calmio.git
   ```
2. Abre el proyecto en **Android Studio** y espera a que se sincronicen las dependencias.
3. Conecta un dispositivo Android o inicia un emulador.
4. Pulsa el botón de ejecutar.

> **Nota:** para que la sincronización funcione, el proyecto necesita estar conectado a un proyecto de Firebase con su archivo `google-services.json`.

---

## 📱 Capturas de pantalla

<p align="center"><b>Acceso</b></p>
<p align="center">
  <img src="https://github.com/user-attachments/assets/557ab07b-cc3f-4ad3-9051-0de9a18200bc" width="480" alt="Inicio de sesión y registro">
</p>
<p align="center"><i>Inicio de sesión y creación de cuenta</i></p>

<br>

<p align="center"><b>Minijuegos y medición del estrés</b></p>
<p align="center">
  <img src="https://github.com/user-attachments/assets/b15e5c08-f8f0-4fce-9286-166ba47e679e" width="720" alt="Medir el estrés, jugar y volver a medirlo">
</p>
<p align="center"><i>El usuario indica su nivel de estrés, juega un minijuego y vuelve a medirlo al terminar</i></p>

<br>

<p align="center"><b>Diario, juegos y ajustes</b></p>
<p align="center">
  <img src="https://github.com/user-attachments/assets/f3c6e457-66ea-49db-ba62-b99821f52585" width="230" alt="Lista de minijuegos">
  <img src="https://github.com/user-attachments/assets/aa67d2e7-91c1-4be8-ab6a-321465c49c09" width="230" alt="Diario emocional">
  <img src="https://github.com/user-attachments/assets/c0a57e43-2894-4e4a-8cea-1d201c2b3022" width="230" alt="Ajustes y recordatorios">
</p>
<p align="center"><i>Lista de minijuegos · Diario emocional · Ajustes y recordatorios</i></p>

<br>

<p align="center"><b>Historial y progreso</b></p>
<p align="center">
  <img src="https://github.com/user-attachments/assets/092a4bb2-23ca-48bc-8092-4e90ee118e53" width="480" alt="Historial con gráficas y sesiones recientes">
</p>
<p align="center"><i>Evolución del estrés, racha de días seguidos y sesiones recientes</i></p>

---

## 👤 Autor

Desarrollado por **Fernando Sánchez** como Proyecto Final del ciclo DAM.

- GitHub: [@fernandosnchz](https://github.com/fernandosnchz)
