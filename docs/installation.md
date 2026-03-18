# Installation

## Kotlin DSL

1. In your **project** `settings.gradle.kts`, configure plugin repositories:

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}
```

2. In your **app** (or module) `build.gradle.kts`, apply the plugin and add the library:

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("dev.vyp.stringcare.plugin")
}

dependencies {
    implementation("dev.vyp.stringcare:library:5.0.0")
}
```

Use the same version for both the plugin and the library (e.g. `5.0.0`). The plugin is resolved from Maven Central (or Gradle Plugin Portal) when you use the `id("dev.vyp.stringcare.plugin")` form; you can append a version in the root project with `id("dev.vyp.stringcare.plugin") version "5.0.0" apply false` and then in the app only `id("dev.vyp.stringcare.plugin")` if you use a version catalog or extra settings.

## Groovy

If you use Groovy build scripts:

1. In the project `build.gradle`, add the plugin to the buildscript classpath and apply it in the app module:

```groovy
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:8.7.3'
        classpath 'org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.21'
        classpath 'dev.vyp.stringcare:plugin:5.0.0'
    }
}
```

2. In the app `build.gradle`:

```groovy
apply plugin: 'com.android.application'
apply plugin: 'org.jetbrains.kotlin.android'
apply plugin: 'dev.vyp.stringcare.plugin'

dependencies {
    implementation 'dev.vyp.stringcare:library:5.0.0'
}
```

For migration from 4.x Groovy setup, see [Migration](migration.md).

## Using a local build

To test the plugin and library from a local build of the stringcare-android repo:

1. **Plugin:** In your app project `settings.gradle.kts`, include the plugin as a composite build:

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}
// Path to the stringcare-android repo; adjust as needed
includeBuild("../path/to/stringcare-android/plugin")
```

Then in the app `build.gradle.kts` use `id("dev.vyp.stringcare.plugin")` without a version (the composite build supplies it).

2. **Library:** Either:
   - Publish the library to Maven local from stringcare-android: `./gradlew :library:publishToMavenLocal`, then in your app add `mavenLocal()` to repositories and `implementation("dev.vyp.stringcare:library:5.0.0")`, or  
   - If your app is inside the same stringcare-android repo, use `implementation(project(":library"))`.

## Requirements

- **Gradle** 8.11 or later  
- **Android Gradle Plugin** 8.7 or later  
- **Kotlin** 2.0 or later (if using Kotlin)  
- **minSdk** 21 (Android 5.0) or higher  

See [Getting started](getting-started.md) for a minimal walkthrough.
