# Troubleshooting

## Submodule not loaded

**Symptoms:** CMake or JNI build fails; errors about missing native source or `stringcare-jni`.

**Fix:** The native code lives in the **stringcare-jni** Git submodule. Load it with:

```bash
git submodule update --init --recursive
```

Or clone from the start with:

```bash
git clone --recurse-submodules <repo-url>
```

**CI:** In GitHub Actions (or other CI), use checkout with submodules:

```yaml
- uses: actions/checkout@v4
  with:
    submodules: true
    token: ${{ secrets.PAT }}   # if the submodule repo is private
```

See [Build and CI](build-and-ci.md).

## Signing failures

**Symptoms:** `signReleasePublication` or similar fails; “gpg” not found or passphrase error.

**Fix:**

- For **local** publish: Signing can be skipped. Do not set `signing.gnupg.keyName` (and related properties); the build will publish to Maven local without signing.
- For **CI** or publishing to Sonatype: Set the repository secrets `GPG_KEY_ID` and `GPG_PASSPHRASE`. The workflow passes them to Gradle so that signing works in the CI environment. Ensure `gpg` is available in the CI image if using `useGpgCmd()`.

## Plugin not found

**Symptoms:** “Plugin with id 'dev.vyp.stringcare.plugin' not found” or resolution errors.

**Fix:**

- Ensure **pluginManagement** in `settings.gradle.kts` includes **gradlePluginPortal()**, **mavenCentral()**, and **google()** so the plugin can be resolved.
- If you are using a **local** build of the plugin, use **includeBuild** in `settings.gradle.kts`, e.g. `includeBuild("../path/to/stringcare-android/plugin")`, and apply the plugin with `id("dev.vyp.stringcare.plugin")` (no version when using composite build).
- Confirm the plugin ID is exactly **`dev.vyp.stringcare.plugin`** (no typos).

## Publish job not running

**Symptoms:** The “Publish to Maven” job is skipped when running the Release workflow.

**Fix:** The publish job runs only when the workflow input **Publish Maven** is set to **`true`**. In “Run workflow”, set that input to `true` when you intend to publish. Also ensure the repository secrets **NEXUS_USERNAME**, **NEXUS_PASSWORD**, **GPG_KEY_ID**, and **GPG_PASSPHRASE** are configured so that the publish and signing steps can succeed.

## JNI / NDK

**Symptoms:** Native build failures, or “skip” needed on certain platforms.

**Fix:**

- The **library** requires **minSdk 21** and a valid NDK/CMake setup. The **stringcare-jni** submodule must be present (see “Submodule not loaded” above).
- **ABI filters:** The library builds for the default ABIs (e.g. armeabi-v7a, arm64-v8a, x86, x86_64). If you restrict ABIs in your app, ensure the library is compatible.
- The **plugin** ships JNI for the **host** (Gradle runs on macOS, Windows, or Linux). Prebuilts live in **stringcare-jni** (`dist/macos`, `dist/linux`, `dist/windows`). The plugin build **automatically** packs `dist/` into the JAR via **`preparePluginNativeLibraries`** (as long as `stringcare-jni/dist` exists after submodule init + native build). Optional: **`./gradlew :plugin:syncPluginNativeLibraries`** copies `dist/` into `plugin/.../jni/` for Git. If your OS/arch binary is still missing from `dist/`, set **`skip = true`** or complete the native build. See [Build and CI](build-and-ci.md) and [Configuration](configuration.md).

- **`Skipping … (native library not available for this architecture)`** even though the JAR lists `libsignKey.*`: the native loader runs on first **`isNativeLibLoaded()`** (task **execution**), not during Gradle configuration — earlier versions loaded in a static initializer when the task class was loaded, which was **too early** for classpath resources. If you still see this on **Linux**, ensure **`libsignKey.so`** / **`libsignKey-arm64.so`** are inside the plugin JAR (`jar tf … | grep signKey`). On **Apple Silicon**, the macOS **`.dylib`** must include **arm64** (universal or arm64-only); an x86_64-only dylib will fail `System.load`.
