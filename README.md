# Enchant Transfer Mod

## Configuración del IDE

- Descargar el plugin Minecraft Development

- Cambiar el modo de compilar desde el IntelliJ
    1. Abrir la configuración de Gradle
    2. Situarse en `Build, Execution, Deployment` -> `Build Tools` -> `Gradle`
    3. En el apartado `Build and run` cambiar:
        1. `Build and run using` -> **IntelliJ IDEA**
        2. `Run test using` -> **IntelliJ IDEA**
    4. Ir a `File` -> `Project Structure` -> `Project Settings` -> `Project`
        - Cambiar `Project compiler ouput` -> `$PROJECT_DIR$/out` 

- Revisar versiones de fabric para disponer de la última para nuestra versión de minecraft
  - https://fabricmc.net/versions.html
    > Sólo actualizar grade.properties, el build.gradle las coje de forma automática
- En caso de dar problemas con fabric-loom revisar de añadir la siguiente versión a la que tengamos y añadirlo en build.gradle:
   - https://github.com/FabricMC/fabric-loom
    > En caso de actualizar la versión de minecraft revisar y dejar configurado la versión de fabric-loom que venga en el mod de ejemplo (si actualizamos a la versión más actual de minecraft):
    > - https://github.com/FabricMC/fabric-example-mod/blob/1.17/build.gradle
        
## Probar mod

Es necesario tener instalado minecraft en el equipo y además instalar fabric a través del ejecutable que se puede descargar en:
https://fabricmc.net/use/

- build: Compila el proyecto y deja el jar en build/libs
- runClient: Lanza la ejecución de minecraft incluyendo el mod
- runServer: Lanza un servidor de minecraft con fabric que incluye el mod
