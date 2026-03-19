# Migration guide (5.x → current refactor)

## For consumers of the published plugin

No changes are required in `build.gradle.kts` if you only use the public DSL:

```kotlin
stringcare {
    debug = true
    // ...
}
```

## For forks or code that used internal statics

| Old | New |
|-----|-----|
| `StringCarePlugin.absoluteProjectPath` | Resolved per build via `StringCareBuildService.absoluteProjectPath()` (internal) |
| `StringCarePlugin.variantMap` | `StringCareBuildService` variant map (internal) |
| `StringCarePlugin.tempFolder` / `resetFolder()` | Service `getOrCreateTempFolder()` / `resetTempFolder()`; tests use `StringCareSession` + `StringCarePlugin.resetFolder()` |

## Native libraries

When updating bundled `.dylib` / `.so` / `.dll`, refresh SHA-256 entries in `Stark.kt` (`EXPECTED_SHA256`).
