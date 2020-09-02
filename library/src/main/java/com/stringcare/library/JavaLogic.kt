package com.stringcare.library

import android.content.Context
import android.content.res.Resources
import androidx.annotation.StringRes
import java.nio.charset.Charset
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class JavaLogic {

    companion object  {

        @Throws(Exception::class)
        private fun encrypt(message: String, key: String): String {
            val data = message.toByteArray(charset(codification))
            val cipher = Cipher.getInstance(transformation)
            cipher.init(Cipher.ENCRYPT_MODE, generateKey(key))
            val encryptData = cipher.doFinal(data)
            return encryptData.asHexUpper
        }

        @Throws(Exception::class)
        private fun decrypt(message: String, key: String): String {
            val cipher = Cipher.getInstance(transformation)
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(generateKey(key).encoded, aes))
            return String(cipher.doFinal(message.hexAsByteArray), Charset.forName(codification))
        }
        /**
         * Encrypts the given value
         * @param value
         * @return String
         */
        internal fun encryptString(context: Context, value: String): String {
            return try {
                encrypt(value, getCertificateSHA1Fingerprint(context))
            } catch (e: Exception) {
                e.printStackTrace()
                value
            }
        }

        /**
         * Decrypts the given value
         * @param value
         * @return String
         */
        internal fun decryptString(context: Context, value: String): String {
            return try {
                decrypt(value, getCertificateSHA1Fingerprint(context))
            } catch (e: Exception) {
                e.printStackTrace()
                value
            }
        }

        /**
         * Decrypts the given ID
         * @param id
         * @return String
         */
        internal fun getString(context: Context, @StringRes id: Int): String {
            return try {
                decrypt(context.getString(id), getCertificateSHA1Fingerprint(context))
            } catch (e: Exception) {
                e.printStackTrace()
                context.getString(id) // returns original value, maybe not encrypted
            }
        }

        /**
         * Decrypts the given ID
         * @param id
         * @param formatArgs
         * @return String
         */
        internal fun getString(context: Context,
                               @StringRes id: Int,
                               formatArgs: Array<out Any>): String {
            return java.lang.String.format(
                    Resources.getSystem().locale(),
                    getString(context, id),
                    *formatArgs)
        }

    }
}
