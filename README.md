# PROYECTO_FPFC

Proyecto en Scala 3. Puedes correrlo directamente desde IntelliJ IDEA.

## Requisitos
- IntelliJ IDEA (Community o Ultimate)
- Plugin de Scala instalado en IntelliJ
- JDK (preferiblemente 17 o superior)

## Cómo correr en IntelliJ
1. Abre IntelliJ IDEA.
2. Ve a “Open” y selecciona la carpeta del repositorio (`PROYECTO_FPFC`).
3. Asegúrate de que IntelliJ detecte el JDK (Project SDK) y la versión de Scala. Si te lo pide, configúralo en:
   - File > Project Structure > Project
   - Selecciona un SDK válido y comprueba el lenguaje Scala.
4. Espera a que el proyecto indexe. IntelliJ debería reconocer el código fuente dentro de `src/`.
5. Localiza el objeto/clase que tenga `main` (por ejemplo, un `object` con `def main(args: Array[String])`).
6. Haz clic derecho sobre ese `main` y selecciona “Run '<MainName>'”.
   - Si hay varias entradas `main`, elige la que corresponda.
7. Si lo prefieres, crea una Run Configuration:
   - Run > Edit Configurations > + > Scala Application
   - Indica el `Main class` y el `Module` del proyecto
   - Guarda y ejecuta.

## Pruebas (opcional)
- Si hay tests con `munit`, puedes ejecutarlos desde el panel de tests o haciendo clic derecho sobre el paquete/clase de pruebas y “Run Tests”.

## Notas
- Si IntelliJ pide agregar dependencias o configurar Scala, acepta las sugerencias y sigue los asistentes.
- Si algo falla, verifica que el plugin de Scala esté activo y que el JDK esté correctamente configurado.
