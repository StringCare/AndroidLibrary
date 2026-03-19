# Plugin tasks

## User-facing tasks

The plugin registers two tasks you can run from the command line or IDE:

- **`stringcarePreview`**  
  Prints a report of what would be obfuscated: which string and asset files were found and which entries match the configuration. Use it to verify that your `stringFiles`, `srcFolders`, and `assetsFiles` are correct before a full build.  
  Example: `./gradlew :app:stringcarePreview`

- **`stringcareTestObfuscate`**  
  Runs obfuscation into a temporary directory and prints before/after content. **Source files in the module are never modified.**  
  Example: `./gradlew :app:stringcareTestObfuscate`

Both tasks are registered once per project (not per variant). They use the first variant’s configuration (e.g. debug) for fingerprint and paths when the project is an Android application.

## Variant tasks (internal)

The plugin registers **per-variant** tasks that feed **generated** resource and asset directories into the Android merge pipeline via AGP’s **`addGeneratedSourceDirectory`**. You do not need to run these yourself.

- **`stringcareObfuscateStringResources<Variant>`** — Writes obfuscated string XML under `build/intermediates/stringcare/generated-res/<variant>/`. Registered as an extra `res` source layer (highest priority overlay).
- **`stringcareObfuscateAssets<Variant>`** — Writes obfuscated assets under `build/intermediates/stringcare/generated-assets/<variant>/`. Registered as an extra `assets` source layer.

`<Variant>` is the variant name with the first letter capitalized (e.g. `Debug`, `Release`, or `ProdDebug` if you use product flavors).

### Behaviour

- **Sources are never modified** — Plaintext files stay in `src/main/res` and `src/main/assets` for version control and IDE indexing.
- **Gradle caching** — Obfuscate tasks declare proper inputs (`xmlInputFiles` / `assetInputFiles`) and outputs (`outputDirectory`), so incremental builds and build cache can apply.
- AGP wires `merge*Resources` / asset merge tasks to depend on the obfuscate tasks automatically when generated directories are registered.
