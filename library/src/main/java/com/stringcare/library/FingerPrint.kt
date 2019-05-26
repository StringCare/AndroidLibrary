package com.stringcare.library

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import java.io.ByteArrayInputStream
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateEncodingException
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

internal fun getCertificateSHA1Fingerprint(context: Context): String? {
    val pm = context.packageManager
    val flags = PackageManager.GET_SIGNATURES
    var packageInfo: PackageInfo? = null
    try {
        packageInfo = pm.getPackageInfo(context.packageName, flags)
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }

    val signatures = packageInfo!!.signatures
    val cert = signatures[0].toByteArray()
    val input = ByteArrayInputStream(cert)
    var cf: CertificateFactory? = null
    try {
        cf = CertificateFactory.getInstance("X509")
    } catch (e: CertificateException) {
        e.printStackTrace()
    }

    var c: X509Certificate? = null
    try {
        c = cf!!.generateCertificate(input) as X509Certificate
    } catch (e: CertificateException) {
        e.printStackTrace()
    }

    var hexString: String? = null
    try {
        val md = MessageDigest.getInstance("SHA1")
        val publicKey = md.digest(c!!.encoded)
        hexString = publicKey.asHexUpper
    } catch (e1: NoSuchAlgorithmException) {
        e1.printStackTrace()
    } catch (e1: CertificateEncodingException) {
        e1.printStackTrace()
    }

    return hexString
}

@Throws(NoSuchAlgorithmException::class)
internal fun generateKey(key: String): SecretKey {
    val digest = MessageDigest.getInstance("SHA-1")
    var passphrase: ByteArray? = null
    try {
        passphrase = digest.digest(key.toByteArray(charset(codification)))
    } catch (e: UnsupportedEncodingException) {
        e.printStackTrace()
    }

    passphrase = Arrays.copyOf(passphrase!!, 16)
    return SecretKeySpec(passphrase, "AES")
}
