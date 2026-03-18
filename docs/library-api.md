# Library API

All public types live in the package **`dev.vyp.stringcare.library`**.

## Package and initialization

- **Package:** `dev.vyp.stringcare.library`
- **Initialization:** Call once before any `reveal` or `obfuscate` use.

**`SC.init(context: Context)`**  
Sets the application context (e.g. from `Application.onCreate()` or your main Activity). Use this when you have a `Context` available at startup.

**`SC.init(context: () -> Context)`**  
Sets a lambda that supplies the context. Useful when the context is not available at plugin apply time (e.g. you need the context from a specific Activity later).

You should call `init` in your `Application` subclass or before the first use of `SC.reveal()`, `SC.obfuscate()`, or `SCTextView`.

## Revealing strings

**By resource ID:**

- **`SC.reveal(@StringRes id: Int): String`**  
  Reveals the string resource with the default version and default android treatment.
- **`SC.reveal(@StringRes id: Int, version: Version): String`**  
  Reveals with the given algorithm version (V0–V3).
- **`SC.reveal(@StringRes id: Int, androidTreatment: Boolean, version: Version): String`**  
  Full control; use the same `androidTreatment` and `version` as used when the string was obfuscated by the plugin.

**By value (obfuscated string):**

- **`SC.reveal(value: String): String`**  
  Reveals an obfuscated string (e.g. returned from a previous `obfuscate` call or stored value).
- **`SC.reveal(value: String, androidTreatment: Boolean, version: Version): String`**  
  Same with explicit options.

**With format args (resource ID):**

- **`SC.reveal(@StringRes id: Int, vararg formatArgs: Any, ...): String`**  
  Reveals the string resource and formats it with `String.format` using the given format args.

If the context has not been set via `init`, these methods may log an error and return the original value (or empty string for ID overloads).

## Obfuscating strings

Use these for **runtime-generated** strings that you want to obfuscate in memory (e.g. before sending to a backend or storing).

- **`SC.obfuscate(value: String): String`**  
  Obfuscates with default android treatment and default version.
- **`SC.obfuscate(value: String, androidTreatment: Boolean, version: Version): String`**  
  Obfuscates with the given options. Use the same `version` (and optionally `androidTreatment`) when calling `reveal` later.

If the context has not been set, obfuscate may return the original value unchanged.

## Version enum

**`Version`** (in `dev.vyp.stringcare.library`) has four values: **V0**, **V1**, **V2**, **V3**. They correspond to different obfuscation algorithms (V0 is Java-based; V1–V3 use native code). The plugin can produce strings for any of these; the library must use the same version when revealing. Defaults are typically V3 for new usage. Use the same version in both plugin configuration and library calls when you need a specific algorithm.

## Assets

**`SC.asset()`** returns an **`SC.Assets`** instance with:

- **`json(path: String): JSONObject`** — Loads an obfuscated JSON asset and returns a `JSONObject`.
- **`jsonArray(path: String): JSONArray`** — Loads an obfuscated JSON array asset.
- **`bytes(path: String, predicate: () -> Boolean): ByteArray`** — Loads obfuscated asset bytes; the predicate can gate loading (e.g. feature flag).
- **`bytes(path: String, predicate: Boolean): ByteArray`** — Overload with a boolean.
- **`bytes(path: String): ByteArray`** — Loads obfuscated bytes (always).

Paths are relative to the assets folder (e.g. `"config.json"`).

## ContextListener and onContextReady

If you need to run logic **when** the context becomes available (e.g. you call `SC.init()` later):

- **`SC.onContextReady(listener: ContextListener)`**  
  Registers a callback. If the context is already set, the listener is invoked immediately; otherwise it is invoked when `init` is next called.

**`ContextListener`** has a single method: **`contextReady()`**.

## SCTextView

**`SCTextView`** is an `AppCompatTextView` that can reveal obfuscated string resources or text in XML layouts.

**XML attributes** (use the library’s namespace or `res-auto`):

- **`reveal`** — If true, the view treats its `android:text` (or linked resource) as obfuscated and reveals it.
- **`htmlSupport`** — If true, the revealed text is interpreted as HTML (e.g. for formatting).
- **`androidTreatment`** — Boolean; applies the same whitespace normalization as the library’s `androidTreatment` parameter.

Use `SCTextView` in layouts when you want to bind a string resource or obfuscated value to a TextView without calling `SC.reveal()` in code. The view will call the library internally after the context is available.

## Extension functions

Convenience extensions (from `StringExt.kt` and `ResExt.kt`) in the same package:

- **`Int.string(): String`** — Equivalent to `context.getString(this)`.
- **`Int.reveal(...): String`** — Reveals the string resource (e.g. `R.string.foo.reveal()`). Overloads for `androidTreatment`, `version`, and format args.
- **`String.obfuscate(...): String`** — Obfuscates the string (e.g. `"secret".obfuscate()`).
- **`String.reveal(...): String`** — Reveals an obfuscated string.
- **`String.json(): JSONObject`** — Loads an obfuscated JSON asset (path is the receiver).
- **`String.jsonArray(): JSONArray`** — Loads an obfuscated JSON array asset.
- **`String.bytes(predicate: () -> Boolean = { true }): ByteArray`** — Loads obfuscated asset bytes for the path.

All of these require `SC.init(context)` to have been called first (they use `SC` internally). `Resources.locale()` in `ResExt` is used for formatting and is an extension on `Resources`.
