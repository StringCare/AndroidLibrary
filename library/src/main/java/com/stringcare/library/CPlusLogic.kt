package com.stringcare.library

import android.content.Context
import android.content.res.Resources
import androidx.annotation.StringRes
import java.nio.charset.Charset
import kotlin.Exception

class CPlusLogic {

    companion object {

        /**
         * Obfuscates the given value
         * @param value
         * @return String
         */
        @JvmStatic
        fun obfuscateV1(context: Context, value: String): String {
            return SC().jniObfuscateV1(context, getCertificateSHA1Fingerprint(context), value)
        }

        /**
         * Reveals the given value
         * @param id
         * @return String
         */
        @JvmStatic
        fun revealV1(context: Context, @StringRes id: Int): String {
            return SC().jniRevealV1(context,
                    getCertificateSHA1Fingerprint(context),
                    context.getString(id))
        }

        /**
         * Reveals the given value
         * @param value
         * @return String
         */
        @JvmStatic
        fun revealV1(context: Context, value: String): String {
            return SC().jniRevealV1(context, getCertificateSHA1Fingerprint(context), value)
        }

        /**
         * Reveals the given value
         * @param id
         * @param formatArgs
         * @return
         */
        @JvmStatic
        fun revealV1(context: Context, @StringRes id: Int, formatArgs: Array<out Any>): String {
            return java.lang.String.format(Resources.getSystem().locale(), revealV1(context, id), *formatArgs)
        }

        /**
         * Obfuscates the given value
         * @param value
         * @return String
         */
        @JvmStatic
        fun obfuscateV2(context: Context, value: String): String {
            val bytes = Charset.forName("UTF-8").encode(value)
            val arrO = ByteArray(bytes.remaining())
            bytes.get(arrO)
            val arr: ByteArray = SC().jniObfuscateV2(context, getCertificateSHA1Fingerprint(context), arrO)
            return arr.map { it.toInt() }.toString().replace("[", "").replace("]", "")
        }

        /**
         * Reveals the given value
         * @param id
         * @return String
         */
        @JvmStatic
        fun revealV2(context: Context, @StringRes id: Int): String {
            val value = context.getString(id)
            return try {
                val arr: ByteArray = value.split(", ").map { it.toInt().toByte() }.toByteArray()
                String(SC().jniRevealV2(context, getCertificateSHA1Fingerprint(context), arr))
            } catch (e: Exception) {
                value
            }
        }

        /**
         * Reveals the given value
         * @param value
         * @return String
         */
        @JvmStatic
        fun revealV2(context: Context, value: String): String {
            return try {
                val arr: ByteArray = value.split(", ").map { it.toInt().toByte() }.toByteArray()
                return String(SC().jniRevealV2(context, getCertificateSHA1Fingerprint(context), arr))
            } catch (e: Exception) {
                value
            }
        }

        /**
         * Reveals the given value
         * @param id
         * @param formatArgs
         * @return
         */
        @JvmStatic
        fun revealV2(context: Context, @StringRes id: Int, formatArgs: Array<out Any>): String {
            return java.lang.String.format(Resources.getSystem().locale(), revealV2(context, id), *formatArgs)
        }

        /**
         * Obfuscates the given value
         * @param value
         * @return String
         */
        @JvmStatic
        fun obfuscateV3(context: Context, value: String, androidTreatment: Boolean): String {
            val bytes = Charset.forName("UTF-8").encode(when (androidTreatment) {
                true -> value.androidTreatment()
                false -> value.unescape()
            })
            val arrO = ByteArray(bytes.remaining())
            bytes.get(arrO)
            val arr: ByteArray = SC().jniObfuscateV3(context, getCertificateSHA1Fingerprint(context), arrO)
            return arr.map { it.toInt() }.toString().replace("[", "").replace("]", "")
        }

        /**
         * Reveals the given value
         * @param id
         * @return String
         */
        @JvmStatic
        fun revealV3(context: Context, @StringRes id: Int, androidTreatment: Boolean): String {
            val value = context.getString(id)
            return try {
                val arr: ByteArray = value.split(", ").map { it.toInt().toByte() }.toByteArray()
                val reveal = String(SC().jniRevealV3(context, getCertificateSHA1Fingerprint(context), arr))
                when (androidTreatment) {
                    true -> reveal.unescape()
                    false -> reveal
                }
            } catch (e: Exception) {
                value
            }
        }

        /**
         * Reveals the given value
         * @param value
         * @return String
         */
        @JvmStatic
        fun revealV3(context: Context, value: String, androidTreatment: Boolean): String {
            return try {
                val arr: ByteArray = when (androidTreatment) {
                    true -> value.unescape()
                    false -> value
                }.split(", ").map { it.toInt().toByte() }.toByteArray()
                val reveal = String(SC().jniRevealV3(context, getCertificateSHA1Fingerprint(context), arr))
                when (androidTreatment) {
                    true -> reveal.unescape()
                    false -> reveal
                }
            } catch (e: Exception) {
                value
            }
        }

        /**
         * Reveals the given value
         * @param id
         * @param formatArgs
         * @return
         */
        @JvmStatic
        fun revealV3(context: Context, @StringRes id: Int, androidTreatment: Boolean, formatArgs: Array<out Any>): String {
            return java.lang.String.format(Resources.getSystem().locale(), revealV3(context, id, androidTreatment), *formatArgs)
        }

        /**
         * Reveals the given ByteArray
         * @param value
         * @return String
         */
        @JvmStatic
        fun revealByteArray(context: Context, value: ByteArray): ByteArray {
            return try {
                SC().jniRevealV3(context, getCertificateSHA1Fingerprint(context), value)
            } catch (e: Exception) {
                value
            }
        }

    }

}