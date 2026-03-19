[![Maven Central](https://img.shields.io/maven-central/v/dev.vyp.stringcare/library.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22dev.vyp.stringcare%22%20AND%20a:%22library%22)

**StringCare Android** v5.0.0 — Compile-time obfuscation for strings and assets; runtime reveal in your app. GroupId: `dev.vyp.stringcare`.

Full documentation is in the [docs/](docs/) directory (getting started, configuration, API, publishing, troubleshooting).

<p align="center"><img width="10%" vspace="10" src="https://github.com/StringCare/AndroidLibrary/raw/develop/app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png"></p>

<h3 align="center" style="margin-bottom:30px" vspace="20">StringCare Android Library</h3>

<p align="center"><img width="10%" vspace="20" src="https://github.com/StringCare/AndroidLibrary/raw/develop/white.png"></p>

### Installation (Kotlin DSL)

**1. Plugin** — In the project root `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}
// If publishing locally: includeBuild("../path/to/stringcare-android/plugin")
```

In the app (or module) `build.gradle.kts`:

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("dev.vyp.stringcare.plugin")
}

stringcare {
    debug = false
    skip = false
    assetsFiles = mutableListOf("*.json")
    stringFiles = mutableListOf("strings.xml")
    srcFolders = mutableListOf("src/main")
}
```

**2. Library** — In the same module:

```kotlin
dependencies {
    implementation("dev.vyp.stringcare:library:5.0.0")
}
```

For **Groovy** use `buildscript` / `apply plugin: 'dev.vyp.stringcare.plugin'` and `implementation 'dev.vyp.stringcare:library:5.0.0'`. See [Migration from 4.x](MIGRATION.md) if upgrading.

#### [Flutter Support](https://github.com/StringCare/stringcare)

#### [What Is StringCare](https://github.com/StringCare/AndroidLibrary/wiki/What-is-StringCare)

#### [Migration from 4.x to 5.0](MIGRATION.md)

#### [Implementation](https://github.com/StringCare/AndroidLibrary/wiki/Implementation)

#### [Strings Usage](https://github.com/StringCare/AndroidLibrary/wiki/Strings-Usage)

#### [Assets Usage](https://github.com/StringCare/AndroidLibrary/wiki/Assets-Usage)

#### [Configuration](https://github.com/StringCare/AndroidLibrary/wiki/Configuration)

#### [Publish APK](https://github.com/StringCare/AndroidLibrary/wiki/Publish-APK)

#### [Limitations](https://github.com/StringCare/AndroidLibrary/wiki/Limitations)

#### [Compatibility](https://github.com/StringCare/AndroidLibrary/wiki/Compatibility)

#### [Tasks](https://github.com/StringCare/AndroidLibrary/wiki/Tasks)

#### [Resource Tips](https://github.com/StringCare/AndroidLibrary/wiki/Resource-Tips)

**Build / CI:** This project uses the JNI native library as a Git submodule (`stringcare-jni`, e.g. stringcare-android-c). Clone with submodules: `git clone --recurse-submodules ...` or run `git submodule update --init --recursive` after clone. CI workflows must use `checkout` with `submodules: true`.

**Plugin host natives:** With submodule `stringcare-jni` and `dist/` built, the plugin JAR picks up **`dist/{macos,linux,windows}/`** automatically on each **`preparePluginNativeLibraries`** (runs before `processResources`). Use **`./gradlew :plugin:syncPluginNativeLibraries`** only if you want to commit those binaries under `plugin/.../jni/`. macOS: universal `libsignKey.dylib`; Linux/Windows: x64 + arm64 `*.so` / `*.dll`.

**Publishing (release workflow):** See [CONTRIBUTING.md](CONTRIBUTING.md) for required secrets: `NEXUS_USERNAME`, `NEXUS_PASSWORD`, `GPG_KEY_ID`, `GPG_PASSPHRASE`, `PAT`. When dispatching the release workflow, set **Publish Maven** to `true` to run the publish job (publishes both `dev.vyp.stringcare:library` and `dev.vyp.stringcare:plugin`).

<p align="center"><img width="10%" vspace="20" src="https://github.com/StringCare/AndroidLibrary/raw/develop/white.png"></p>

<p align="center"><img width="40%" vspace="20" src="https://raw.githubusercontent.com/efraespada/AndroidStringObfuscator/develop/sample.png"> <img width="40%" vspace="20" src="https://raw.githubusercontent.com/efraespada/AndroidStringObfuscator/develop/sample2.png"></p>


<p align="center"><img width="10%" vspace="20" src="https://github.com/StringCare/AndroidLibrary/raw/develop/white.png"></p>

License
-------

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

