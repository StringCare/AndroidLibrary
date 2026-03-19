# Build and CI

## Building the project

The stringcare-android repository uses the **stringcare-jni** native library as a **Git submodule**. You must load it before building.

**Clone with submodules:**

```bash
git clone --recurse-submodules <repo-url> stringcare-android
cd stringcare-android
```

**If you already cloned without submodules:**

```bash
git submodule update --init --recursive
```

Then build and test:

```bash
./gradlew clean build
./gradlew test
```

To run instrumented tests (requires an emulator or device):

```bash
./gradlew connectedCheck
```

## Project structure

- **Root** — Contains `settings.gradle.kts` and `build.gradle.kts`. Includes the **app** and **library** modules.
- **app** — Demo Android application; applies the StringCare plugin and depends on the library.
- **library** — Android library (AAR) that provides the runtime API (`SC`, `SCTextView`, etc.) and loads the native library from **stringcare-jni** via CMake.
- **plugin** — Gradle plugin (JVM); included as a **composite build** via `includeBuild("plugin")` in the root `settings.gradle.kts`, not as `include(":plugin")`. So the plugin is built from the `plugin/` directory and applied to the app by ID `dev.vyp.stringcare.plugin`.
- **stringcare-jni** — Git submodule containing the native C++ code used by the library and the **plugin host** prebuilts. The library’s `CMakeLists.txt` points to `../stringcare-jni/lib` for the Android JNI source. Plugin host binaries are produced under **`stringcare-jni/dist/`** (`macos/`, `linux/`, `windows/`).

### Plugin host natives in the JAR

After building natives in the submodule (`stringcare-jni/dist/{macos,linux,windows}/`), **every** `:plugin:jar` / `:plugin:processResources` runs **`preparePluginNativeLibraries`**, which copies `dist/` into `plugin/build/generated/stringcare-plugin-natives/` and packages those files into the plugin JAR. No manual step is required for local builds as long as the submodule is present and `dist/` is populated.

Optional — copy prebuilts into Git-tracked `jni/` (for publishing without submodule on CI):

```bash
./gradlew :plugin:syncPluginNativeLibraries
```

## CI

For **GitHub Actions** (or any CI), always checkout the repo **with submodules** so that the JNI code is available:

```yaml
- uses: actions/checkout@v4
  with:
    submodules: true
    token: ${{ secrets.PAT }}   # if the submodule is private
```

Without `submodules: true`, the library’s CMake build will fail because the native source tree will be missing. For the release workflow and publish steps, see [Publishing](publishing.md).
