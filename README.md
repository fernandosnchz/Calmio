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

### Opción 1: Instalar el APK

1. Descarga el archivo APK de la aplicación.
2. En el dispositivo, permite la instalación de aplicaciones de orígenes desconocidos.
3. Abre el archivo APK y pulsa instalar.
4. Una vez instalada, abre Calmio desde el menú de aplicaciones.

### Opción 2: Ejecutar desde Android Studio

1. Clona este repositorio:
   ```
   git clone https://github.com/fernandosnchz/Calmio.git
   ```
2. Abre el proyecto en **Android Studio**.
3. Espera a que se sincronicen las dependencias.
4. Conecta un dispositivo Android o inicia un emulador.
5. Pulsa el botón de ejecutar.

> **Nota:** para que la sincronización de datos funcione, el proyecto necesita estar conectado a un proyecto de Firebase con su archivo `google-services.json`.

---

## 📱 Capturas de pantalla

<!-- Sustituye estas líneas por tus propias capturas. Ejemplo:
![Pantalla de inicio](capturas/login.png)
![Pantalla de juegos](capturas/juegos.png)
-->

*(Próximamente)*

---

## 👤 Autor

Desarrollado por **Fernando Sánchez** como Proyecto Final del ciclo DAM.

- GitHub: [@fernandosnchz](https://github.com/fernandosnchz)
