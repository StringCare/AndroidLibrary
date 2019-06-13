package com.stringcare.library

import android.content.Context
import android.content.pm.PackageManager
import java.io.ByteArrayInputStream
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateEncodingException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

internal fun getCertificateSHA1Fingerprint(context: Context): String {
    return try {
        val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNATURES)
        val signatures = packageInfo!!.signatures
        val cert = signatures[0].toByteArray()
        val input = ByteArrayInputStream(cert)

        val cf = CertificateFactory.getInstance(certificate)
        val c = cf.generateCertificate(input) as X509Certificate
        val publicKey = MessageDigest.getInstance(sha1).digest(c.encoded)
        publicKey.asHexUpper
    } catch (e1: NoSuchAlgorithmException) {
        e1.printStackTrace()
        ""
    } catch (e1: CertificateEncodingException) {
        e1.printStackTrace()
        ""
    }
}

@Throws(NoSuchAlgorithmException::class)
internal fun generateKey(key: String): SecretKey {
    return try {
        val digest = MessageDigest.getInstance(sha1)
        var passphrase = digest.digest(key.toByteArray(charset(codification)))
        passphrase = passphrase.copyOf(16)
        SecretKeySpec(passphrase, aes)
    } catch (e: UnsupportedEncodingException) {
        e.printStackTrace()
        SecretKeySpec(emptyArray<Byte>().toByteArray(), aes)
    }
}
