# Cancheito
### Aplicación Móvil para la Búsqueda y Gestión de Empleo

<div align="center">

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Firebase](https://img.shields.io/badge/firebase-%23039BE5.svg?style=for-the-badge&logo=firebase)
![License](https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge)

[Descargar APK](https://github.com/SamStormDEV/App_Movil_Cancheito/releases/download/v0.1.0-alpha/app-debug.apk) • 
[Reportar Bug](https://github.com/SamStormDEV/App_Movil_Cancheito/issues) • 
[Solicitar Feature](https://github.com/SamStormDEV/App_Movil_Cancheito/issues)

</div>

---

## 📝 Descripción

**Cancheito** es una aplicación móvil nativa de Android desarrollada en **Kotlin** que revoluciona la forma en que **postulantes y empleadores** se conectan en el mercado laboral boliviano. 

La plataforma permite a los usuarios registrarse, crear perfiles profesionales completos, publicar ofertas laborales y postularse a empleos de manera **simple, rápida y segura**.

> 🚀 **Versión actual:** v0.1.0-alpha (MVP Funcional)

---

## Funcionalidades Principales

### Para Postulantes
- **Registro y autenticación** con email y contraseña
- **Perfil profesional completo** con datos personales y experiencia laboral
- **Carga de CV** en formato PDF
- **Foto de perfil personalizable**
- **Búsqueda avanzada** con filtros por categoría, ciudad y modalidad
- **Postulación directa** a ofertas de empleo
- **Seguimiento de postulaciones** en tiempo real

### Para Empleadores  
- **Registro empresarial** con datos comerciales
- **Perfil corporativo** con logo, rubro y ubicación
- **Publicación de ofertas laborales** detalladas
- **Gestión de postulaciones** recibidas
- **Visualización de perfiles** de candidatos
- **Descarga de CVs** de postulantes

### Características Técnicas
- **Arquitectura MVVM** para código mantenible
- **Integración Firebase** para autenticación y almacenamiento
- **Interfaz intuitiva** siguiendo Material Design
- **Almacenamiento seguro** de datos y archivos

---

## Tecnologías Utilizadas

| Categoría | Tecnología | Versión |
|-----------|------------|---------|
| **Lenguaje** | Kotlin | Latest |
| **Plataforma** | Android Nativo | API 24+ |
| **IDE** | Android Studio | Latest |
| **Arquitectura** | MVVM | - |
| **Backend** | Firebase | Latest |
| **Autenticación** | Firebase Auth | Latest |
| **Base de Datos** | Firestore | Latest |
| **Almacenamiento** | Firebase Storage | Latest |
| **Testing** | JUnit + Espresso | Latest |
| **Control de Versiones** | Git/GitHub | - |

---

## Instalación y Configuración

### Opción 1: Descargar APK (Recomendado para usuarios)
1. Descarga la última versión desde [Releases](https://github.com/SamStormDEV/App_Movil_Cancheito/releases)
2. Habilita "Fuentes desconocidas" en tu dispositivo Android
3. Instala el archivo APK descargado
4. ¡Listo para usar!

### Opción 2: Compilar desde código fuente (Para desarrolladores)

#### Prerrequisitos
- **Android Studio** (versión más reciente)
- **JDK 11** o superior
- **Git** instalado
- **Dispositivo Android** (API 24+) o emulador

#### Pasos de instalación
```bash
# 1. Clonar el repositorio
git clone https://github.com/SamStormDEV/App_Movil_Cancheito.git

# 2. Navegar al directorio del proyecto
cd App_Movil_Cancheito

# 3. Abrir en Android Studio
# File -> Open -> Seleccionar carpeta del proyecto

# 4. Configurar Firebase (opcional para desarrollo)
# Agregar google-services.json en /app/
# Configurar Firebase Auth, Firestore y Storage

# 5. Sincronizar dependencias
# Build -> Make Project

# 6. Ejecutar en dispositivo/emulador
# Run -> Run 'app'
```

#### Configuración de Firebase (Opcional)
```bash
# Para funcionalidad completa, configura:
1. Firebase Authentication
2. Firestore Database  
3. Firebase Storage
4. Agrega google-services.json al proyecto
```

---

## 🚀 Uso de la Aplicación

### Primera vez
1. **Descarga e instala** la aplicación
2. **Regístrate** como Postulante o Empleador
3. **Completa tu perfil** con información relevante
4. **Explora las funcionalidades** disponibles

### Para Postulantes
1. **Busca empleos** usando los filtros disponibles
2. **Revisa las ofertas** que te interesen
3. **Postúlate** con un clic
4. **Sigue el estado** de tus postulaciones

### Para Empleadores
1. **Publica ofertas laborales** detalladas
2. **Recibe postulaciones** de candidatos
3. **Revisa perfiles** de postulantes
4. **Gestiona** el proceso de selección

---

## Testing

### Ejecutar pruebas
```bash
# Pruebas unitarias
./gradlew test

# Pruebas de instrumentación
./gradlew connectedAndroidTest
```

### Cobertura de pruebas
- **Autenticación:** Login, registro, logout
- **Navegación:** Transiciones entre pantallas
- **CRUD:** Creación, lectura, actualización de datos
- **Integración:** Firebase connectivity

---

## Estructura del Proyecto

```
App_Movil_Cancheito/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/cancheito/
│   │   │   │   ├── activities/     # Actividades principales
│   │   │   │   ├── fragments/      # Fragmentos de UI
│   │   │   │   ├── adapters/       # Adaptadores RecyclerView
│   │   │   │   ├── models/         # Modelos de datos
│   │   │   │   ├── repositories/   # Acceso a datos
│   │   │   │   └── utils/          # Utilidades y helpers
│   │   │   └── res/                # Recursos (layouts, strings, etc.)
│   │   └── test/                   # Pruebas unitarias
│   └── build.gradle                # Dependencias del módulo
├── gradle/                         # Configuración Gradle
├── README.md                       # Este archivo
└── build.gradle                    # Configuración del proyecto
```

---

## Contribuir al Proyecto

¡Las contribuciones son bienvenidas! Sigue estos pasos:

1. **Fork** el proyecto
2. **Crea una rama** para tu feature (`git checkout -b feature/AmazingFeature`)
3. **Commit** tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. **Push** a la rama (`git push origin feature/AmazingFeature`)
5. **Abre un Pull Request**

### Convenciones de código
- Seguir las convenciones de **Kotlin**
- Usar **nombres descriptivos** para variables y funciones
- **Comentar código complejo**
- **Escribir pruebas** para nuevas funcionalidades

---

## Issues y Soporte

- **Bugs:** [Reportar aquí](https://github.com/SamStormDEV/App_Movil_Cancheito/issues)
- **Features:** [Solicitar aquí](https://github.com/SamStormDEV/App_Movil_Cancheito/issues)
- **Preguntas:** Abre un issue con la etiqueta `question`

---

## Roadmap

### Versión 0.1.0-alpha (Actual)
- [x] Sistema de autenticación
- [x] Perfiles de usuario
- [x] Publicación de ofertas
- [x] Búsqueda y postulación
- [x] Gestión básica

### Próximas versiones
- [ ] **v0.2.0** - Notificaciones push
- [ ] **v0.3.0** - Chat integrado
- [ ] **v0.4.0** - Sistema de valoraciones
- [ ] **v1.0.0** - Versión estable para Play Store

---

## Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.

---
