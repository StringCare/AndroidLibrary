# Getting started

## What is StringCare

StringCare Android obfuscates string resources and asset files at **build time** (via a Gradle plugin) and reveals them at **runtime** in your app (via a small library). Sensitive strings never appear in plain text in your APK; the library uses the app signing certificate to derive a key and decrypt them. You add the plugin and library to your project, mark which strings and assets to obfuscate, and call `SC.reveal()` (or use `SCTextView`) where you need the plain value.

## Prerequisites

- **Gradle** 8.11 or later  
- **Android Gradle Plugin (AGP)** 8.7 or later  
- **Kotlin** 2.0 or later (or compatible Java)  
- **minSdk** 21 (Android 5.0) or higher  
- **targetSdk** 35 recommended  

## Add the library

In your app module `build.gradle.kts`, add the runtime dependency:

```kotlin
dependencies {
    implementation("dev.vyp.stringcare:library:5.0.0")
}
```

Use the same version as the plugin (see below). For other build systems see [Installation](installation.md).

## Apply the plugin

In your **project** `settings.gradle.kts`, ensure plugin repositories are available:

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}
```

In your **app** module `build.gradle.kts`, apply the plugin and add a minimal `stringcare` block:

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("dev.vyp.stringcare.plugin")
}

stringcare {
    debug = false
    skip = false
    stringFiles = mutableListOf("strings.xml")
    srcFolders = mutableListOf("src/main")
    assetsFiles = mutableListOf()  // add e.g. "*.json" if you obfuscate assets
}
```

## Obfuscate a string

1. In `res/values/strings.xml` (or a file matched by `stringFiles`), add a string and mark it for obfuscation with `hidden="true"`:

```xml
<resources>
    <string name="api_key" hidden="true">my-secret-api-key</string>
</resources>
```

2. In your code, initialize StringCare with the application context (e.g. in `Application.onCreate()` or before first use):

```kotlin
import dev.vyp.stringcare.library.SC

// In Application or Activity:
SC.init(applicationContext)
```

3. Reveal the string by resource ID or by value:

```kotlin
// By resource ID (typical for strings.xml)
val apiKey = SC.reveal(R.string.api_key)

// Or if you have the obfuscated value (e.g. from another source)
val plain = SC.reveal(obfuscatedValue)
```

The plugin will replace the plain text in `strings.xml` during the build with an obfuscated form; the library decrypts it at runtime using the app signing certificate.

## Run the app

Build and run your app as usual (`./gradlew assembleDebug` or Run from Android Studio). The first time you call `SC.reveal()`, the library uses the signing key (or the debug key when debugging) to reveal the string. Ensure you have called `SC.init(context)` before any `SC.reveal()` or `SCTextView` usage.

For more options (assets, `SCTextView`, versions, and full configuration), see [Configuration](configuration.md) and [Library API](library-api.md). For detailed installation variants (Groovy, local build), see [Installation](installation.md).
