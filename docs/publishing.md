# Publishing

## Release workflow

Releases are driven by the **Release** GitHub Actions workflow (`workflow_dispatch`).

**Inputs:**

- **version** — Semver release version (e.g. `5.0.0`). Required.
- **title** — Release title. Required.
- **changelog** — Release notes / changelog. Required.
- **issue** — Launcher issue number (or `-1`). Required.
- **publish_maven** — Set to `true` to run the publish job (publish library and plugin to Sonatype). Default `false`.

**Jobs:**

1. **prepare-version-files**  
   Checkout (with submodules), set up JDK 17, validate inputs, update `VERSION_NAME` in `gradle.properties`, run `./gradlew clean build` and `./gradlew test`, then commit the version bump.

2. **publish**  
   Runs only when `publish_maven` is `true`. Checkout (with submodules), set up JDK 17, then:
   - Publish the **library** to Sonatype: `./gradlew :library:publishReleasePublicationToSonatypeRepository`
   - Publish the **plugin** to Sonatype: `./gradlew :plugin:publishPluginPublicationToSonatypeRepository`  
   Requires repository secrets for Nexus and signing (see Secrets below).

3. **tag**  
   Create Git tag and GitHub Release (e.g. via custom actions or scripts) using the version and changelog.

## Secrets

Configure these **repository secrets** for the release workflow:

| Secret | Description |
|--------|-------------|
| `NEXUS_USERNAME` | Sonatype/OSSRH username |
| `NEXUS_PASSWORD` | Sonatype/OSSRH password |
| `GPG_KEY_ID` | GPG key ID used for signing artifacts |
| `GPG_PASSPHRASE` | Passphrase for the GPG key |
| `PAT` | Personal Access Token (repo scope) for checkout and release steps (e.g. when submodule or release action needs it) |

The workflow passes them to Gradle via environment variables (e.g. `ORG_GRADLE_PROJECT_nexusUsername`, `ORG_GRADLE_PROJECT_signing_gnupg_keyName`, etc.) so that publishing and signing work in CI.

## Local publish

To publish the library and plugin to the **local Maven repository** (~/.m2) for testing:

```bash
./gradlew :library:publishToMavenLocal
./gradlew :plugin:publishToMavenLocal
```

Signing is **optional** for local publish: if the property `signing.gnupg.keyName` is not set, the build skips signing and still publishes. For Sonatype (CI), signing is required and the secrets must be set.

To try a full publish without uploading (dry-run), run the publish tasks with `--dry-run`; note that without credentials the Sonatype publish task may still fail configuration validation. See [Contributing](contributing.md) for more on local workflow.
