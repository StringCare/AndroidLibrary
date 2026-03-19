# Security

## Native binaries

Host-side JNI libraries shipped in the plugin JAR are verified with SHA-256 before load. If you replace prebuilts, update the expected hashes in `plugin/.../internal/Stark.kt`.

## Signing fingerprints

The plugin derives keys from `signingReport` output. Treat CI logs as sensitive if debug logging prints certificate details.

## Reporting

Report security issues via the repository’s issue tracker or maintainer contact in the published POM.
