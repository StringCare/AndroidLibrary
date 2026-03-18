# Configuration

## Plugin block

Configure the StringCare plugin in your app (or module) `build.gradle.kts` under the `stringcare` extension:

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `debug` | Boolean | `false` | Enable extra logging (paths, located files). |
| `skip` | Boolean | `false` | Skip obfuscation (e.g. for CI or when JNI is unavailable). |
| `assetsFiles` | List<String> | `[]` | Glob patterns for asset files to obfuscate (e.g. `"*.json"`). |
| `stringFiles` | List<String> | e.g. `["strings.xml"]` | Names of string resource XML files to scan (e.g. `"strings.xml"`, `"strings_extra.xml"`). |
| `srcFolders` | List<String> | e.g. `["src/main"]` | Source set paths relative to the module where the plugin looks for resources and assets. |
| `mockedFingerprint` | String | `""` | If set, use this value instead of the real signing certificate fingerprint (e.g. for tests or when signing is not available). |

Example:

```kotlin
stringcare {
    debug = false
    skip = false
    assetsFiles = mutableListOf("*.json")
    stringFiles = mutableListOf("strings.xml", "strings_extra.xml")
    srcFolders = mutableListOf("src/main")
    mockedFingerprint = ""
}
```

## Strings (strings.xml)

Only strings marked with `hidden="true"` are obfuscated. Other attributes are optional:

- **`hidden="true"`** — Include this string in obfuscation. Omit or set to `"false"` to leave it plain.
- **`androidTreatment`** — `"true"` (default) or `"false"`. Affects whitespace normalization when revealing; should match how you call `SC.reveal(..., androidTreatment = ...)`.
- **`containsHtml`** — Set to `"true"` if the string contains HTML that should be parsed when revealing.

Example:

```xml
<resources>
    <string name="public_label">Hello</string>
    <string name="api_secret" hidden="true">my-secret</string>
    <string name="html_content" hidden="true" containsHtml="true">&lt;b&gt;Bold&lt;/b&gt;</string>
</resources>
```

The plugin scans files matching `stringFiles` under directories matching `srcFolders` (e.g. `src/main/res/values/strings.xml`).

## Assets

Asset files are matched by the patterns in `assetsFiles` (e.g. `*.json`). The plugin looks under the paths in `srcFolders` (e.g. `src/main/assets`). Obfuscated assets are decrypted at runtime via the library. To read an obfuscated asset in code, use [Library API](library-api.md): `SC.asset().json(path)`, `SC.asset().jsonArray(path)`, or `SC.asset().bytes(path)`.
