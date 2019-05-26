package com.stringcare.library

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.support.annotation.StringRes
import java.nio.charset.Charset
import java.util.*

class CPlusLogic {

    companion object {

        /**
         * Obfuscates the given value
         * @param value
         * @return String
         */
        @JvmStatic
        fun obfuscateV1(context: Context, value: String): String? {
            return SC().jniObfuscateV1(context, getCertificateSHA1Fingerprint(context), value)
        }

        /**
         * Reveals the given value
         * @param id
         * @return String
         */
        @JvmStatic
        fun revealV1(context: Context, @StringRes id: Int): String? {
            return SC().jniRevealV1(context, getCertificateSHA1Fingerprint(context), context.getString(id))
        }

        /**
         * Reveals the given value
         * @param value
         * @return String
         */
        @JvmStatic
        fun revealV1(context: Context, value: String): String? {
            return SC().jniRevealV1(context, getCertificateSHA1Fingerprint(context), value)
        }

        /**
         * Reveals the given value
         * @param id
         * @param formatArgs
         * @return
         */
        @JvmStatic
        fun revealV1(context: Context, @StringRes id: Int, formatArgs: Array<out Any>): String ? {
            val value = revealV1(context, id)
            value?.let {
                val locale: Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Resources.getSystem().configuration.locales.get(0)
                } else {
                    Resources.getSystem().configuration.locale
                }
                return java.lang.String.format(locale, it, *formatArgs)
            }
            return null
        }

        /**
         * Obfuscates the given value
         * @param value
         * @return String
         */
        @JvmStatic
        fun obfuscateV2(context: Context, value: String): String? {
            val bytes = Charset.forName("UTF-8").encode(value)
            val arrO = ByteArray(bytes.remaining())
            bytes.get(arrO)
            val arr: ByteArray = SC().jniObfuscateV2(context, getCertificateSHA1Fingerprint(context), arrO)
            return arr.map { it.toInt() }.toString().replace("[","").replace("]","")
        }

        /**
         * Reveals the given value
         * @param id
         * @return String
         */
        @JvmStatic
        fun revealV2(context: Context, @StringRes id: Int): String? {
            val arr: ByteArray = context.getString(id).split(", ").map { it.toInt().toByte() }.toByteArray()
            return String(SC().jniRevealV2(context, getCertificateSHA1Fingerprint(context), arr))
        }

        /**
         * Reveals the given value
         * @param value
         * @return String
         */
        @JvmStatic
        fun revealV2(context: Context, value: String): String? {
            val arr: ByteArray = value.split(", ").map { it.toInt().toByte() }.toByteArray()
            return String(SC().jniRevealV2(context, getCertificateSHA1Fingerprint(context), arr))
        }

        /**
         * Reveals the given value
         * @param id
         * @param formatArgs
         * @return
         */
        @JvmStatic
        fun revealV2(context: Context, @StringRes id: Int, formatArgs: Array<out Any>): String? {
            val value = revealV2(context, id)
            value?.let {
                val locale: Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Resources.getSystem().configuration.locales.get(0)
                } else {
                    Resources.getSystem().configuration.locale
                }
                return java.lang.String.format(locale, it, *formatArgs)
            }
            return null
        }

    }

}