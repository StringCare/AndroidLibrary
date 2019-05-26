package com.stringcare.library

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.support.annotation.StringRes
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
            try {
                return SC().jniObfuscateV1(context, getCertificateSHA1Fingerprint(context), value)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return value
        }

        /**
         * Deobfuscates the given value
         * @param id
         * @return String
         */
        @JvmStatic
        fun revealV1(context: Context, @StringRes id: Int): String? {
            try {
                return SC().jniRevealV1(context, getCertificateSHA1Fingerprint(context), context.getString(id))
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return context.getString(id)
        }

        /**
         * Deobfuscates the given value
         * @param value
         * @return String
         */
        @JvmStatic
        fun revealV1(context: Context, value: String): String? {
            try {
                return SC().jniRevealV1(context, getCertificateSHA1Fingerprint(context), value)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return value
        }

        /**
         * Deobfuscates the given value
         * @param id
         * @param formatArgs
         * @return
         */
        @JvmStatic
        fun revealV1(context: Context, @StringRes id: Int, vararg formatArgs: Any): String {
            val value = revealV1(context, id)
            val locale: Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Resources.getSystem().configuration.locales.get(0)
            } else {
                Resources.getSystem().configuration.locale
            }
            return String.format(locale, value!!, *formatArgs)
        }

    }

}