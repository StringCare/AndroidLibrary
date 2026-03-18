# Contributing

## Release workflow secrets

To run the **Release** workflow and publish to Maven Central (Sonatype), configure these GitHub **repository secrets**:

| Secret | Description |
|--------|-------------|
| `NEXUS_USERNAME` | Sonatype/OSSRH username |
| `NEXUS_PASSWORD` | Sonatype/OSSRH password |
| `GPG_KEY_ID` | GPG key ID used for signing (e.g. short key id) |
| `GPG_PASSPHRASE` | Passphrase for the GPG key |
| `PAT` | Personal Access Token with repo scope (for checkout and release steps) |

## Publishing a release

1. In GitHub, go to **Actions** and select the **Release** workflow.
2. Run it via **Run workflow** (`workflow_dispatch`).
3. Fill in the inputs: **version** (e.g. `5.0.0`), **title**, **changelog**, **issue**.
4. Set **Publish Maven** to **`true`** to publish the library and plugin to Sonatype. Leave it `false` to only run the prepare and tag steps.
5. The workflow will build, test, (optionally) publish both `dev.vyp.stringcare:library` and `dev.vyp.stringcare.plugin`, and create the Git tag and GitHub Release.

## Local publish

To test publishing locally without pushing to Sonatype:

```bash
./gradlew :library:publishToMavenLocal
./gradlew :plugin:publishToMavenLocal
```

Signing is **optional** when publishing to Maven local: if the property `signing.gnupg.keyName` is not set, the build skips signing and still publishes. For publishing to Sonatype (CI or manual), signing is required.

For more detail on the release workflow and secrets, see the root [CONTRIBUTING.md](../CONTRIBUTING.md).
