# Architecture

## Repository layout

The project is a **mono-repo** with:

- **app** — Demo Android application. Applies the StringCare plugin and depends on the library. Included in the root `settings.gradle.kts` via `include(":app", ":library")`.
- **library** — Android library (AAR) that provides the runtime API (`SC`, `SCTextView`, Version, Assets, etc.) and loads the native library. Built with CMake; native source lives in the **stringcare-jni** submodule.
- **plugin** — Gradle plugin (JVM). Built as a **composite build**: root `settings.gradle.kts` uses `includeBuild("plugin")`, so the plugin is not a subproject but a separate build. It is applied to the app by ID `dev.vyp.stringcare.plugin`.
- **stringcare-jni** — Git submodule containing the C++ implementation used by the library (and optionally by the plugin for fingerprint/signing). The library’s `CMakeLists.txt` references `../stringcare-jni/lib` for the native source.

## Library

The **library** module:

- Exposes the public API in package **`dev.vyp.stringcare.library`**: `SC`, `SCTextView`, `Version`, `ContextListener`, extension functions, etc.
- Loads the native library **sc-native-lib** (built from **stringcare-jni**) via `System.loadLibrary("sc-native-lib")`. CMake is configured with `cmake_minimum_required` 3.22.1, C++17, and the JNI source dir set to `../stringcare-jni/lib`.
- Responsibilities: **reveal** (decrypt strings and assets at runtime using the app signing certificate), **obfuscate** (encrypt strings at runtime for the same key), and **asset** access (JSON, bytes) for obfuscated assets.

## Plugin

The **plugin** module:

- Implements the Gradle plugin entry point **`StringCarePlugin`** (implements `Plugin<Project>`), creates the **`stringcare`** extension (`StringCareExtension`) and registers tasks.
- Uses the **AGP 8.7 Variant API**: it does **not** use `BuildListener`. It uses `project.plugins.withId("com.android.application")` and then `project.extensions.getByType(AndroidComponentsExtension::class.java)` and **`onVariants`** to register per-variant tasks (e.g. `stringcareBeforeMergeResourcesDebug`, `stringcareAfterMergeResourcesDebug`, and the same for Assets). Each variant gets before/after merge tasks for resources and for assets; the “before” tasks obfuscate, the “after” tasks restore originals from backup.
- Contains **JNI** (`.dylib`, `.dll`, `.so`) for the **host** (Gradle on macOS, Windows, or Linux). Prebuilts come from **stringcare-jni** `dist/{macos,linux,windows}/`; **`preparePluginNativeLibraries`** (before `processResources`) copies them into `build/generated/stringcare-plugin-natives/` and they are packaged in the JAR. Optional checked-in copies live under `internal/jni/`. macOS: universal `libsignKey.dylib`; Linux/Windows: x64 + arm64. Separate from the app’s **sc-native-lib** in the library module.

## Flow

- **Compile time:** When you build an Android application that applies the plugin, the plugin runs before the merge of resources and assets. It backs up the configured string XML files and asset files, obfuscates them using the signing certificate fingerprint (or mocked fingerprint), and the merge steps see the obfuscated content. After the merge, the plugin restores the originals so the source tree is unchanged. The APK therefore contains obfuscated strings and assets instead of plain text.
- **Runtime:** The app loads the **library** and calls `SC.init(context)`. When you call `SC.reveal(id)` or `SC.reveal(value)`, or use `SCTextView`, the library uses the same certificate (from the running app) to derive the key and decrypt the content. Obfuscation and revelation are symmetric with respect to the signing key.
