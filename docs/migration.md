# Migration from 4.x to 5.0

## Overview

StringCare 5.0 introduces **breaking changes**: new groupId and plugin ID, new package name for the library, and higher requirements for Gradle, AGP, and Kotlin. The public API (e.g. `SC.reveal()`, `SC.obfuscate()`, `SCTextView`) is unchanged; only coordinates, package, and build setup differ.

**Summary of breaking changes:**

- GroupId and artifact coordinates changed from `io.github.stringcare` / `com.stringcare` to **`dev.vyp.stringcare`**.
- Plugin ID changed from the legacy class-based form to **`dev.vyp.stringcare.plugin`**.
- Library package changed from **`com.stringcare.library`** to **`dev.vyp.stringcare.library`** (all imports must be updated).
- **Gradle** 8.11+ and **AGP** 8.7+ are required.
- **Kotlin** 2.0+ is required when using Kotlin.
- **minSdk** is now 21 (Android 5.0).

## Maven coordinates

| 4.x | 5.0 |
|-----|-----|
| `io.github.stringcare:library` | `dev.vyp.stringcare:library` |
| `com.stringcare` (plugin) | `dev.vyp.stringcare.plugin` (plugin ID) |
| `io.github.stringcare:plugin` or similar | `dev.vyp.stringcare:plugin` |

## Gradle (Kotlin DSL)

**Before (4.x, Groovy):**

```groovy
buildscript {
    dependencies {
        classpath 'com.stringcare:gradle-plugin:4.x'
    }
}
apply plugin: 'com.stringcare'
dependencies {
    implementation 'io.github.stringcare:library:4.x'
}
```

**After (5.0, Kotlin DSL):**

```kotlin
plugins {
    id("dev.vyp.stringcare.plugin")
}
dependencies {
    implementation("dev.vyp.stringcare:library:5.0.0")
}
```

Resolve the plugin from Maven Central (or Gradle Plugin Portal) and ensure `pluginManagement.repositories` include `mavenCentral()` and `google()`. See [Installation](installation.md).

## Library package and imports

**Old package:** `com.stringcare.library`  
**New package:** `dev.vyp.stringcare.library`

Update every import in your app (or other modules) that use StringCare:

```kotlin
// Before
import com.stringcare.library.SC
import com.stringcare.library.SCTextView

// After
import dev.vyp.stringcare.library.SC
import dev.vyp.stringcare.library.SCTextView
```

API method names and behavior are unchanged; only the package and Maven coordinates differ.

## Plugin configuration

The extension name and configuration options are **unchanged**: you still use the `stringcare { }` block with `debug`, `skip`, `assetsFiles`, `stringFiles`, `srcFolders`, `mockedFingerprint`. The plugin now uses the AGP 8.x **Variant API** (no `BuildListener`); no changes are required in your build scripts for the block itself.

## Build and CI

This repository uses the **stringcare-jni** native library as a Git submodule. When cloning or in CI, use:

- `git clone --recurse-submodules <url>`
- or `git submodule update --init --recursive`
- In GitHub Actions: `checkout` with `submodules: true`

Otherwise the library’s CMake build will fail. See [Build and CI](build-and-ci.md).

A short form of this migration guide is also available at the root: [MIGRATION.md](../MIGRATION.md).
