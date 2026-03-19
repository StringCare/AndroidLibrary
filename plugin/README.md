[![Maven Central](https://img.shields.io/maven-central/v/io.github.stringcare/plugin.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.stringcare%22%20AND%20a:%22plugin%22)

#### [Wiki](https://github.com/StringCare/AndroidLibrary/wiki)

## Requirements (this repo)

- **Gradle** 8.11+ (configuration cache validated on sample `:app:assembleDebug`)
- **JDK** 17
- **Android Gradle Plugin** 8.7+ (as used by the sample app)
- **Troubleshooting**: If host native libs fail to load, set `stringcare` `debug = true` for verbose JNI logs, or set env `STRINGCARE_NATIVE_DEBUG` (see `Stark`).

See repo root [CHANGELOG.md](../CHANGELOG.md), [MIGRATION.md](../MIGRATION.md), [SECURITY.md](../SECURITY.md).

### Tests

- Default `./gradlew :plugin:test` excludes `@Tag("integration")` (network + `git clone` KotlinSample + nested Gradle).
- Full suite: `./gradlew :plugin:integrationTest` (may fail offline or if the sample repo changes).

### Advanced configuration

Apply in the **app** module `build.gradle.kts`:

```kotlin
plugins {
    id("com.android.application")
    id("dev.vyp.stringcare.plugin")
}

stringcare {
    debug = false
    skip = false
    mockedFingerprint = "" // optional: offline / CI
    srcFolders.add("src/main")
    stringFiles.add("strings.xml")
    assetsFiles.add("*.json")
}
```

### Troubleshooting

| Symptom | What to try |
|--------|-------------|
| `native library not available` | Run on a supported host/OS; ensure JNI prebuilts are packaged (see `preparePluginNativeLibraries`). |
| Wrong or empty signing SHA1 | Run `./gradlew signingReport` for the same variant; check `mockedFingerprint` if offline. |
| Tasks not ordered | Ensure `com.android.application` is applied before StringCare; use AGP 8.7+. |

### Performance tuning

- **Large projects**: SAX parsing is used for flat `<string>` entries; nested HTML strings fall back to DOM for that file.
- **CI**: Use `./gradlew :plugin:test` (fast); defer `:plugin:integrationTest` to nightly or manual runs.
- **Configuration cache**: Enabled at the root build; run `./gradlew :app:assembleDebug --configuration-cache` to validate.

License
-------
    Copyright 2019 StringCare [🐒 SpaceMonkeys]

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
    
[link]: https://github.com/StringCare/KotlinGradlePlugin
[badge]: https://img.shields.io/bintray/v/efff/maven/StringCareAndroidPlugin.svg

