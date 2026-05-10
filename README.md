# Enchant Transfer Mod

![Version](https://img.shields.io/badge/version-1.0.0-blue)
![Minecraft](https://img.shields.io/badge/minecraft-1.21.1-62b47a?logo=minecraft&logoColor=white)
![Fabric](https://img.shields.io/badge/fabric_loader-≥0.15.0-dbd0b4)
![Java](https://img.shields.io/badge/java-21-ED8B00?logo=openjdk&logoColor=white)
![License](https://img.shields.io/badge/license-CC0--1.0-lightgrey)

Mod de Fabric para Minecraft 1.21.1 que permite extraer, transferir y combinar encantamientos entre objetos mediante un sistema de **Magic Cards** y una **Transfer Table**.

---

## Características

- **Extracción** — coloca cualquier objeto o libro encantado en la Transfer Table para obtener sus encantamientos en forma de cartas individuales
- **Transferencia** — aplica Magic Cards a otros objetos compatibles, respetando las restricciones de incompatibilidad de encantamientos de Minecraft
- **Combinación** — combina dos cartas del mismo encantamiento y nivel para obtener una de nivel superior (hasta el máximo vanilla)
- **Stacks** — funciona con stacks de cartas: combinar 5 + 6 cartas produce 5 cartas de nivel superior y deja 1 carta sobrante

---

## Instalación

1. Instala [Fabric Loader](https://fabricmc.net/use/) **≥ 0.15.0** para Minecraft 1.21.1
2. Descarga [Fabric API](https://modrinth.com/mod/fabric-api) y colócalo en la carpeta `mods/`
3. Descarga el JAR del mod y colócalo también en `mods/`
4. Lanza Minecraft con el perfil de Fabric

> **Fabric API es obligatorio.** El mod no arrancará sin ella.

---

## Uso

### Transfer Table

La Transfer Table es el bloque central del mod. Se craftea con... *(añadir receta)*.

Al interactuar con ella se abre una interfaz con dos secciones:

**Sección de transferencia**
- Coloca un objeto encantado (o libro encantado) en el slot central para que sus encantamientos aparezcan como Magic Cards en los slots adyacentes
- Retira una carta para extraer ese encantamiento del objeto
- Añade una carta desde tu inventario para aplicar ese encantamiento al objeto (si es compatible)

**Sección de combinación**
- Coloca dos Magic Cards del mismo encantamiento y nivel en los slots de entrada
- El slot de resultado mostrará la carta combinada de nivel superior
- Recoge el resultado para consumir las cartas de entrada

### Consejos
- Los libros sin encantar también son válidos como destino: puedes construir un libro encantado desde cero añadiendo cartas
- Usa `Shift + Click` en una Magic Card del inventario para enviarla directamente al slot adecuado según el contexto
- Los encantamientos incompatibles entre sí no se pueden combinar en el mismo objeto

---

## Requisitos

| Dependencia | Versión |
|-------------|---------|
| Java | 21 |
| Minecraft | 1.21.1 |
| Fabric Loader | ≥ 0.15.0 |
| Fabric API | 0.102.0+1.21.1 |

---

## Para desarrolladores

### Configuración del IDE (IntelliJ IDEA)

1. Instala el plugin **Minecraft Development**
2. Cambia el modo de compilación:
   - `Settings` → `Build, Execution, Deployment` → `Build Tools` → `Gradle`
   - `Build and run using` → **IntelliJ IDEA**
   - `Run test using` → **IntelliJ IDEA**
3. Configura el output del compilador:
   - `File` → `Project Structure` → `Project Settings` → `Project`
   - `Project compiler output` → `$PROJECT_DIR$/out`

### Tareas Gradle

| Tarea | Descripción |
|-------|-------------|
| `./gradlew build` | Compila el proyecto y genera el JAR en `build/libs/` |
| `./gradlew runClient` | Lanza Minecraft con el mod cargado (cliente) |
| `./gradlew runServer` | Lanza un servidor de Minecraft con el mod |

> `runClient` y `runServer` no requieren tener Minecraft instalado por separado; Fabric Loom descarga los assets automáticamente.

### Actualizar dependencias

Todas las versiones están centralizadas en `gradle.properties`:

```properties
minecraft_version = 1.21.1
yarn_mappings     = 1.21.1+build.3
loader_version    = 0.16.5
fabric_version    = 0.102.0+1.21.1
java_version      = 21
```

Referencia de versiones disponibles: https://fabricmc.net/versions.html  
Referencia de `fabric-loom`: https://github.com/FabricMC/fabric-example-mod

---

## Licencia

Este proyecto se distribuye bajo la licencia **CC0-1.0**. Puedes usar, modificar y distribuir el código sin restricciones.
