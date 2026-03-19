package dev.vyp.stringcare.plugin.infrastructure.crypto

import dev.vyp.stringcare.plugin.internal.Stark

/**
 * JNI-backed obfuscation entry point (delegates to [Stark]).
 *
 * @throws UnsatisfiedLinkError if native code is not loaded on this host.
 */
class ObfuscationService {
    /**
     * Obfuscates a payload using the JNI runtime.
     *
     * @param key SHA-1 signing fingerprint used as obfuscation key.
     * @param payload Raw bytes to obfuscate.
     * @param applicationId Android application id mixed in native transformation.
     * @return Obfuscated bytes to persist in resources/assets.
     * @throws UnsatisfiedLinkError if native code is unavailable for this host.
     */
    fun obfuscate(
        key: String,
        payload: ByteArray,
        applicationId: String,
    ): ByteArray = Stark.obfuscate(key, payload, applicationId)

    /**
     * Reverts bytes previously obfuscated by [obfuscate].
     *
     * @param key SHA-1 signing fingerprint used during obfuscation.
     * @param payload Obfuscated bytes.
     * @param applicationId Android application id used by native transformation.
     * @return Plain decoded bytes.
     * @throws UnsatisfiedLinkError if native code is unavailable for this host.
     */
    fun reveal(
        key: String,
        payload: ByteArray,
        applicationId: String,
    ): ByteArray = Stark.reveal(key, payload, applicationId)
}
