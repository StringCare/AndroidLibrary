package dev.vyp.stringcare.plugin.infrastructure.crypto

import dev.vyp.stringcare.plugin.internal.Stark

/**
 * JNI-backed obfuscation entry point (delegates to [Stark]).
 *
 * @throws UnsatisfiedLinkError if native code is not loaded on this host.
 */
class ObfuscationService {
    fun obfuscate(
        key: String,
        payload: ByteArray,
        applicationId: String,
    ): ByteArray = Stark.obfuscate(key, payload, applicationId)

    fun reveal(
        key: String,
        payload: ByteArray,
        applicationId: String,
    ): ByteArray = Stark.reveal(key, payload, applicationId)
}
