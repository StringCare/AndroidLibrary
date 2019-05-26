package com.stringcare.library

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.support.annotation.StringRes
import java.nio.charset.Charset
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class JavaLogic {

    companion object  {

        @Throws(Exception::class)
        private fun encrypt(message: String, key: String?): String {
            val data = message.toByteArray(charset(codification))
            val cipher = Cipher.getInstance(transformation)
            cipher.init(Cipher.ENCRYPT_MODE, generateKey(key!!))
            val encryptData = cipher.doFinal(data)
            return encryptData.asHexUpper
        }

        @Throws(Exception::class)
        private fun decrypt(message: String, key: String?): String {
            val spec = SecretKeySpec(generateKey(key!!).encoded, "AES")
            val cipher = Cipher.getInstance(transformation)
            cipher.init(Cipher.DECRYPT_MODE, spec)
            return String(cipher.doFinal(message.hexAsByteArray), Charset.forName(codification))
        }
        /**
         * Encrypts the given value
         * @param value
         * @return String
         */
        internal fun encryptString(context: Context, value: String): String? {
            val hash = getCertificateSHA1Fingerprint(context)
            try {
                return encrypt(value, hash)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        /**
         * Decrypts the given value
         * @param value
         * @return String
         */
        internal fun decryptString(context: Context, value: String): String? {
            val hash = getCertificateSHA1Fingerprint(context)
            try {
                return decrypt(value, hash)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        /**
         * Decrypts the given ID
         * @param id
         * @return String
         */
        internal fun getString(context: Context, @StringRes id: Int): String? {
            val hash = getCertificateSHA1Fingerprint(context)
            try {
                return decrypt(context.getString(id), hash)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return context.getString(id) // returns original value, maybe not encrypted
        }

        /**
         * Decrypts the given ID
         * @param id
         * @param formatArgs
         * @return String
         */
        internal fun getString(context: Context, @StringRes id: Int, vararg formatArgs: Any): String {
            val value = getString(context, id)
            val locale: Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Resources.getSystem().configuration.locales.get(0)
            } else {
                Resources.getSystem().configuration.locale
            }
            return String.format(locale, value!!, *formatArgs)
        }

    }
}
