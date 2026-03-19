# Changelog

## [Unreleased] — refactor / 6.x groundwork

### Added

- **Docs**: [Refactor plan checklist](docs/refactor-plan-checklist.md) — seguimiento hecho/pendiente del roadmap del plugin.

### Fixed

- **Restore**: Backups under `temp/<module>/src/...` now copy back to `<module>/src/...` (no duplicated module segment). Restore uses binary `copyTo` for assets; restore-resource task no longer clears temp before restore-assets; `stringcareBeforeMergeAssets` runs after `stringcareBeforeMergeResources`. Restore tasks stay non–UP-TO-DATE so Gradle cannot skip them while sources remain obfuscated.

### Changed

- **Architecture**: Domain models under `domain.models` (with `models` typealiases for compatibility); `infrastructure` packages for parsers, Gradle wiring, crypto, filesystem; thin `ObfuscateStringsUseCase` + `ResourceRepository`.
- **XML**: SAX-first `strings.xml` parsing with DOM fallback for nested markup; `XmlAttributes` / `XmlParser` facade.
- **Tasks**: Gradle `Property` inputs on obfuscate/restore/preview tasks; `@DisableCachingByDefault` (sources are mutated in place — not suitable for `@CacheableTask`).
- **AGP**: `compileOnly` for the Android Gradle Plugin dependency (provided at runtime on Android projects).
- **JAR size**: With `compileOnly` AGP, plugin JAR is ~180KB vs previous multi‑MB fat jar (verify locally with `./gradlew :plugin:jar`).
- **Gradle**: `StringCareBuildService` (shared build service) replaces mutable static state on `StringCarePlugin` for paths, temp dir, and variant applicationIds.
- **Dependencies**: Removed Guava and Gson; added `kotlinx-serialization-json` for task JSON list inputs.
- **Execution**: Shell commands use `ProcessBuilder` with a 60s timeout and structured `ExecutionResult` (`Success` / `Failure` / `Timeout`).
- **Native host libs**: SHA-256 verification before `System.load`, retries, optional verbose logging tied to `debug` in tasks.
- **XML / scan**: Faster attribute iteration in `parseXML`; `walkTopDown` skips `build/`, `.gradle/`, `.git/`, `node_modules/`; `mapNotNull` for resource/asset discovery; idempotent `StringCareConfiguration.normalize()`.
- **Tooling**: Detekt + baseline, ktlint (non-failing), JaCoCo hook, Develocity build scan terms in root `settings.gradle.kts`.
- **Backups**: Resource/asset backups use temp-file copy + atomic move when the filesystem supports it.
- **Tests**: `ObfuscationServiceTest` (JNI roundtrip when loaded), `BackupServiceTest`, UTF-16 / malformed XML parser cases; CI runs `jacocoTestCoverageVerification` with a low interim line threshold.

### Breaking changes

- **Internal APIs**: Static mutable state on `StringCarePlugin` (paths, temp folder, variant map) was removed in favor of `StringCareBuildService`. Any build logic or tests reaching into those internals must use task inputs / the registered build service instead.
- **Dependencies on the plugin JAR**: Guava and Gson are no longer bundled; list-style DSL fields are serialized with Kotlin serialization in task properties. Pure-Java consumers of internal packages are unsupported.

### Performance (roadmap vs previous generations)

- XML handling targets **SAX-first** parsing with DOM only when nested `<string>` markup requires it, and filesystem walks **prune** heavy directories (`build/`, `.gradle/`, `.git/`, `node_modules/`). End-to-end timings depend on project size; run `./gradlew :plugin:test` and your app’s obfuscation tasks locally to validate.

### Migration notes

- Public plugin id and DSL block name are unchanged (`dev.vyp.stringcare.plugin`, `stringcare { }`).
- If you relied on internal APIs (`StringCarePlugin.absoluteProjectPath`, etc.), migrate to the build service or task inputs only.

See [MIGRATION.md](MIGRATION.md).
