# Enchant Transfer Mod

Mod de Fabric para Minecraft que permite transferir encantamientos entre objetos mediante **Magic Cards** y una **Transfer Table**.

- Extrae encantamientos de cualquier objeto o libro encantado en forma de cartas
- Aplica esas cartas a otros objetos compatibles
- Combina dos cartas del mismo encantamiento y nivel para obtener una de nivel superior

![Version](https://img.shields.io/badge/version-1.0.0-blue)
![Minecraft](https://img.shields.io/badge/minecraft-1.21.1-62b47a?logo=minecraft&logoColor=white)
![Fabric](https://img.shields.io/badge/fabric_loader-≥0.15.0-dbd0b4)
![Java](https://img.shields.io/badge/java-21-ED8B00?logo=openjdk&logoColor=white)
![License](https://img.shields.io/badge/license-CC0--1.0-lightgrey)

---

## Requisitos

| Dependencia | Versión mínima |
|-------------|---------------|
| Java | 21 |
| Minecraft | 1.21.1 |
| Fabric Loader | ≥ 0.15.0 |
| Fabric API | 0.102.0+1.21.1 |

---

## Configuración del IDE (IntelliJ IDEA)

1. Instalar el plugin **Minecraft Development**
2. Cambiar el modo de compilación:
   - `Settings` → `Build, Execution, Deployment` → `Build Tools` → `Gradle`
   - En `Build and run using` seleccionar **IntelliJ IDEA**
   - En `Run test using` seleccionar **IntelliJ IDEA**
3. Configurar el output del compilador:
   - `File` → `Project Structure` → `Project Settings` → `Project`
   - `Project compiler output` → `$PROJECT_DIR$/out`

---

## Actualizar versiones de dependencias

Todas las versiones están centralizadas en `gradle.properties`:

```properties
minecraft_version = 1.21.1
yarn_mappings      = 1.21.1+build.3
loader_version     = 0.16.5
fabric_version     = 0.102.0+1.21.1
java_version       = 21
```

Para consultar las versiones disponibles: https://fabricmc.net/versions.html

Si se actualiza la versión de `fabric-loom` (definida en `build.gradle`), se puede tomar como referencia el mod de ejemplo oficial:
https://github.com/FabricMC/fabric-example-mod

---

## Tareas Gradle

| Tarea | Descripción |
|-------|-------------|
| `./gradlew build` | Compila el proyecto y genera el JAR en `build/libs/` |
| `./gradlew runClient` | Lanza Minecraft con el mod cargado (cliente) |
| `./gradlew runServer` | Lanza un servidor de Minecraft con el mod |

> Para `runClient` y `runServer` no es necesario tener Minecraft instalado por separado; Fabric Loom descarga los assets automáticamente.
