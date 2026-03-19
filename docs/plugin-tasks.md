# Plugin tasks

## User-facing tasks

The plugin registers two tasks you can run from the command line or IDE:

- **`stringcarePreview`**  
  Prints a report of what would be obfuscated: which string and asset files were found and which entries match the configuration. Use it to verify that your `stringFiles`, `srcFolders`, and `assetsFiles` are correct before a full build.  
  Example: `./gradlew :app:stringcarePreview`

- **`stringcareTestObfuscate`**  
  Performs a dry run of the obfuscation logic (backup, obfuscate, then restore) without changing the final build output. Useful to confirm that obfuscation and key derivation work in your environment.  
  Example: `./gradlew :app:stringcareTestObfuscate`

Both tasks are registered once per project (not per variant). They use the first variant’s configuration (e.g. debug) for fingerprint and paths when the project is an Android application.

## Variant tasks (internal)

The plugin also registers **per-variant** tasks that hook into the Android build. You do not need to run these yourself; they are wired before and after the merge steps:

- **`stringcareBeforeMergeResources<Variant>`** — Runs before the variant’s merge of resources; obfuscates the string XML files.
- **`stringcareAfterMergeResources<Variant>`** — Runs after merge; restores the original string files from backup.
- **`stringcareBeforeMergeAssets<Variant>`** — Runs before the variant’s asset merge; obfuscates asset files.
- **`stringcareAfterMergeAssets<Variant>`** — Runs after asset merge; restores the original assets.

`<Variant>` is the variant name with the first letter capitalized (e.g. `Debug`, `Release`, or `ProdDebug` if you use product flavors). The plugin connects these so that the merge tasks see the obfuscated resources/assets, and the originals are restored afterward so the source tree stays unchanged. Dependencies are set so that the “before” task runs before the corresponding merge task, and the “after” task runs after it.

### Ordering and temp backups

- **`stringcareBeforeMergeResources<Variant>`** is ordered to run **before** **`stringcareBeforeMergeAssets<Variant>`** (`mustRunAfter`), so string obfuscation and backups are written first; both phases share the same temp root under `StringCareBuildService`.
- **`stringcareAfterMergeResources<Variant>`** restores from backup using paths relative to the module directory (the Gradle project folder): backups live at `temp/<module>/src/...` and are copied back to `<module>/src/...`, not `<module>/<module>/src/...`.
- **`stringcareAfterMergeResources<Variant>`** does **not** delete the temp tree; **`stringcareAfterMergeAssets<Variant>`** runs last and restores assets (re-copy from the same backups is harmless), then clears the temp directory so backups are not left behind.
- Restore tasks use **`outputs.upToDateWhen { false }`** so Gradle does not skip them while sources are still obfuscated from a previous run.
