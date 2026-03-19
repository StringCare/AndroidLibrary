# StringCare Android Documentation

StringCare Android provides **compile-time obfuscation** for strings and assets in your app, and **runtime reveal** via a small library. The Gradle plugin (groupId `dev.vyp.stringcare`) obfuscates resources during the build; the runtime library reveals them using the app signing certificate.

## Documentation index

- [Getting started](getting-started.md) — Quick path to your first obfuscated string (dependency, plugin, `hidden="true"`, `SC.reveal()`).
- [Installation](installation.md) — Kotlin DSL, Groovy, Maven Central vs local build, and version alignment.
- [Configuration](configuration.md) — The `stringcare { }` block, strings.xml attributes, and asset patterns.
- [Library API](library-api.md) — Public API: `SC.init`, `SC.reveal`, `SC.obfuscate`, `Version`, `SC.Assets`, `SCTextView`, extension functions.
- [Plugin tasks](plugin-tasks.md) — User-facing tasks (`stringcarePreview`, `stringcareTestObfuscate`) and variant tasks.
- [Build and CI](build-and-ci.md) — Building the repo, submodule clone, and CI (checkout with submodules).
- [Publishing](publishing.md) — Release workflow, secrets, and local publish (`publishToMavenLocal`).
- [Migration](migration.md) — Upgrading from 4.x to 5.0 (groupId, plugin ID, package, Gradle/AGP).
- [Architecture](architecture.md) — Mono-repo layout, library vs plugin, JNI, and Variant API.
- [Contributing](contributing.md) — Release workflow secrets and local publish steps.
- [Troubleshooting](troubleshooting.md) — Submodule, signing, plugin not found, publish job, JNI/NDK.
- [Verify obfuscation](verify-obfuscation.md) — Comandos `gradlew` para comprobar que strings/assets se ofuscan (nativas del host, `syncPluginNativeLibraries`).

For the main project README and badges, see the [root README](../README.md).
