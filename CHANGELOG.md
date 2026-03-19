# Changelog

## [Unreleased] — refactor / 6.x groundwork

### Changed

- **Gradle**: `StringCareBuildService` (shared build service) replaces mutable static state on `StringCarePlugin` for paths, temp dir, and variant applicationIds.
- **Dependencies**: Removed Guava and Gson; added `kotlinx-serialization-json` for task JSON list inputs.
- **Execution**: Shell commands use `ProcessBuilder` with a 60s timeout and structured `ExecutionResult` (`Success` / `Failure`).
- **Native host libs**: SHA-256 verification before `System.load`, retries, optional verbose logging tied to `debug` in tasks.
- **XML / scan**: Faster attribute iteration in `parseXML`; `walkTopDown` skips `build/`, `.gradle/`, `.git/`, `node_modules/`; `mapNotNull` for resource/asset discovery; idempotent `StringCareConfiguration.normalize()`.
- **Tooling**: Detekt + baseline, ktlint (non-failing), JaCoCo hook, Develocity build scan terms in root `settings.gradle.kts`.

### Migration notes

- Public plugin id and DSL block name are unchanged (`dev.vyp.stringcare.plugin`, `stringcare { }`).
- If you relied on internal APIs (`StringCarePlugin.absoluteProjectPath`, etc.), migrate to the build service or task inputs only.

See [MIGRATION.md](MIGRATION.md).
