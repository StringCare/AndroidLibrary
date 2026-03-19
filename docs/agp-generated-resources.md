# AGP: generated `res` / `assets` directories

The Android Gradle Plugin does **not** expose a stable public artifact for “merged resources still as XML” before AAPT2 compiles to `.flat` binaries. Transforming post-merge XML is therefore not a supported first-class workflow for third-party plugins.

StringCare obfuscates **before** merge by writing outputs under `build/intermediates/stringcare/generated-*` and registering them with:

- `variant.sources.res.addGeneratedSourceDirectory(task, Task::outputDirectory)`
- `variant.sources.assets.addGeneratedSourceDirectory(task, Task::outputDirectory)`

This is the same extension point used for other generated resource trees and keeps `src/` untouched while remaining compatible with incremental builds and Gradle caching when task inputs/outputs are declared correctly.
