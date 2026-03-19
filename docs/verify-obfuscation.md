# Verificar ofuscación con Gradle

La ofuscación de strings y assets en el **módulo `app`** depende de que el plugin cargue la librería nativa del **host** (el mismo OS y arquitectura que ejecuta Gradle). Si no carga, verás en consola:

```text
Skipping <variant> (native library not available for this architecture)
```

## 1. Requisitos

1. Submódulo **`stringcare-jni`** inicializado y precompilados generados en `stringcare-jni/dist/` (tras el build en stringcare-android-c).
2. **Automático:** al compilar el plugin (`:plugin:jar` o cualquier build que lo use), la tarea **`preparePluginNativeLibraries`** copia `stringcare-jni/dist/{macos,linux,windows}/` a `build/generated/stringcare-plugin-natives/` y **`processResources`** las empaqueta en el JAR. No hace falta `syncPluginNativeLibraries` salvo que quieras **commitear** las binarias en `src/.../jni/`.
3. Si **no** existe `stringcare-jni/dist/`, se usan los ficheros ya presentes en `plugin/.../internal/jni/` (fallback).
4. En el JAR del plugin deben estar, según tu máquina:
   - **macOS:** `libsignKey.dylib` (universal o la variante correcta).
   - **Linux:** `libsignKey.so` y/o `libsignKey-arm64.so`.
   - **Windows:** `libsignKey.dll` y/o `libsignKey-arm64.dll`.

Tras actualizar `dist/` en el submódulo, basta con **`./gradlew :plugin:jar`** o **`./gradlew :app:assemble...`** para volver a empaquetar las nativas.

## 2. Depurar carga de librería nativa (plugin)

Si ves *Skipping … native library not available*, activa trazas en **stderr** con cualquiera de:

1. **`stringcare { debug = true }`** en el módulo `app` (ya propagado a las tareas de ofuscación).
2. **Propiedad JVM** al invocar Gradle:
   ```bash
   ./gradlew :app:assembleDevDebug -Dstringcare.debug.native=true --info
   ```
3. **Variable de entorno:** `STRINGCARE_DEBUG_NATIVE=1` o `STRINGCARE_DEBUG_NATIVE=true`.

Verás líneas `[StringCare native] …`: OS, arquitectura, orden de ficheros probados, si se encontró el recurso en classpath / JAR / `resources/main`, tamaño del fichero temporal y errores de `System.load` (p. ej. `UnsatisfiedLinkError` si el binario no coincide con la CPU).

## 3. Tareas útiles

Listar tareas del plugin en el app:

```bash
./gradlew :app:tasks --all | grep -i stringcare
```

Tareas típicas (por variante; ejemplo **prodDebug**):

| Tarea | Rol |
|-------|-----|
| `stringcareObfuscateStringResourcesProdDebug` | Escribe strings ofuscados en `build/intermediates/stringcare/generated-res/prodDebug/` |
| `stringcareObfuscateAssetsProdDebug` | Escribe assets ofuscados en `build/intermediates/stringcare/generated-assets/prodDebug/` |
| `stringcarePreview` | Vista previa / diagnóstico |
| `stringcareTestObfuscate` | Prueba de ofuscación en directorio temporal (no modifica fuentes) |

## 4. Comprobar que no se salta la ofuscación

Con salida detallada:

```bash
./gradlew :app:stringcareObfuscateStringResourcesProdDebug --rerun-tasks --info 2>&1 | tee stringcare-verify.log
```

**Esperado si la nativa carga y hay huella de firma:** mensajes del plugin con variante y clave SHA1, listado de recursos, etc. **No** debe aparecer la línea de *Skipping … native library*.

Comprobar también assets:

```bash
./gradlew :app:stringcareObfuscateAssetsProdDebug --rerun-tasks --info 2>&1 | tee -a stringcare-verify.log
```

## 5. Build completa

```bash
./gradlew :app:clean :app:assembleProdDebug --rerun-tasks --info 2>&1 | tee stringcare-assemble.log
```

Los **fuentes en `src/`** permanecen en texto claro; la ofuscación va a directorios generados bajo `build/intermediates/stringcare/`. La comprobación fiable es el **log** de las tareas `stringcareObfuscate*` (y que exista huella vía `signingReport` o `mockedFingerprint` en el bloque `stringcare { }`).

## 6. Huella de firma (SHA1)

Si no hay `mockedFingerprint` en el bloque `stringcare { }`, el plugin usa `./gradlew signingReport` para la variante. Sin SHA1 válido, puede no ofuscar aunque la nativa cargue. Para depurar:

```bash
./gradlew :app:signingReport
```

---

**Resumen:** Si ves *Skipping … native library*, comprueba que exista `stringcare-jni/dist/` con las tres plataformas (o al menos la del host), ejecuta **`./gradlew :plugin:clean :plugin:jar`** y repite. Si el submódulo no está inicializado, haz `git submodule update --init --recursive` o usa `syncPluginNativeLibraries` + commit en `jni/` como respaldo.
