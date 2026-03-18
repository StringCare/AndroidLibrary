# Contributing to StringCare Android

## Release workflow secrets

To run the **Release** workflow and publish to Maven Central (Sonatype), configure these GitHub repository secrets:

| Secret | Description |
|--------|-------------|
| `NEXUS_USERNAME` | Sonatype/OSSRH username |
| `NEXUS_PASSWORD` | Sonatype/OSSRH password |
| `GPG_KEY_ID` | GPG key ID used for signing (e.g. short key id) |
| `GPG_PASSPHRASE` | Passphrase for the GPG key |
| `PAT` | Personal Access Token with repo scope (for checkout and release steps) |

When dispatching the release workflow, set **Publish Maven** to `true` to run the publish job. The workflow will:

1. Build and test all modules
2. Publish `dev.vyp.stringcare:library` and `dev.vyp.stringcare:plugin` to Sonatype (when publish is enabled)
3. Create the Git tag and GitHub Release

Local publish (for testing):

```bash
./gradlew :library:publishToMavenLocal
./gradlew :plugin:publishToMavenLocal
```
