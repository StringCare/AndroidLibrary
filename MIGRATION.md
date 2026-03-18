# Migration from StringCare 4.x to 5.0

## GroupId and artifact IDs

- **Old:** `io.github.stringcare` (or `com.stringcare`)  
- **New:** `dev.vyp.stringcare`

Update dependencies and plugin coordinates:

| 4.x | 5.0 |
|-----|-----|
| `io.github.stringcare:library` | `dev.vyp.stringcare:library` |
| `com.stringcare` plugin ID | `dev.vyp.stringcare.plugin` |

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

Resolve the plugin from Maven Central or a local `includeBuild`; see [README](README.md#installation-kotlin-dsl).

## Plugin configuration

- Extension and task names are unchanged (`stringcare { ... }`, `stringFiles`, `assetsFiles`, `srcFolders`, `debug`, `skip`).
- AGP 8.x and Gradle 8.x are required; the plugin uses the Variant API.

## Library package

- **Old:** `com.stringcare.library`  
- **New:** `dev.vyp.stringcare.library`

Update imports in your app:

```kotlin
// Before
import com.stringcare.library.SC
import com.stringcare.library.SCTextView

// After
import dev.vyp.stringcare.library.SC
import dev.vyp.stringcare.library.SCTextView
```

## API compatibility

Public API (e.g. `SC.reveal()`, `SC.obfuscate()`, `SCTextView`, resources usage) is unchanged. Only package and Maven coordinates differ.

## Build / CI

This repo uses the JNI native library as a Git submodule. Clone with `git clone --recurse-submodules` or `git submodule update --init --recursive`. In GitHub Actions use `checkout` with `submodules: true`.
