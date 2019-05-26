package com.stringcare.library

import android.content.Context

import android.support.annotation.StringRes
import android.util.Log

import java.util.ArrayList

/**
 * Created by efrainespada on 02/10/2016.
 */

class SC {

    companion object {

        init {
            System.loadLibrary("native-lib")
        }

        private var context: Context? = null

        private val listeners = ArrayList<ContextListener>()

        @JvmStatic
        fun init(c: Context) {
            context = c
            if (listeners.isNotEmpty()) {
                for (listener in listeners) {
                    listener.contextReady()
                }
            }
        }

        @JvmStatic
        fun onContextReady(listener: ContextListener) {
            if (context != null) {
                listener.contextReady()
                return
            }
            listeners.add(listener)
        }

        @JvmStatic
        fun obfuscate(value: String): String? {
            return obfuscate(value, defaultVersion)
        }

        /**
         * Obfuscates the given value
         * @param value
         * @return String
         */
        @JvmStatic
        fun obfuscate(value: String, version: Version = defaultVersion): String? {
            return if (context == null) {
                Log.e(tag, "Library not initialized: SC.init(Context)")
                null
            } else when (version) {
                Version.V0 -> JavaLogic.encryptString(context!!, value)
                Version.V1 -> CPlusLogic.obfuscateV1(context!!, value)
                Version.V2 -> CPlusLogic.obfuscateV2(context!!, value)
            }
        }

        @JvmStatic
        fun reveal(@StringRes id: Int): String? {
            return reveal(id, defaultVersion)
        }

        /**
         * Deobfuscates the given value
         * @param id
         * @return String
         */
        @JvmStatic
        fun reveal(@StringRes id: Int, version: Version = defaultVersion): String? {
            return if (context == null) {
                Log.e(tag, "Library not initialized: SC.init(Context)")
                null
            } else when (version) {
                Version.V0 -> JavaLogic.getString(context!!, id)
                Version.V1 -> CPlusLogic.revealV1(context!!, id)
                Version.V2 -> CPlusLogic.revealV2(context!!, id)
            }
        }

        @JvmStatic
        fun reveal(value: String): String? {
            return reveal(value, defaultVersion)
        }

        /**
         * Deobfuscates the given value
         * @param value
         * @return String
         */
        @JvmStatic
        fun reveal(value: String, version: Version = defaultVersion): String? {
            return if (context == null) {
                Log.e(tag, "Library not initialized: SC.init(Context)")
                null
            } else when (version) {
                Version.V0 -> JavaLogic.decryptString(context!!, value)
                Version.V1 -> CPlusLogic.revealV1(context!!, value)
                Version.V2 -> CPlusLogic.revealV2(context!!, value)
            }
        }

        @JvmStatic
        fun reveal(@StringRes id: Int, vararg formatArgs: Any): String? {
            return reveal(id, defaultVersion, formatArgs)
        }

        /**
         * Deobfuscates the given value
         * @param id
         * @param formatArgs
         * @return
         */
        @JvmStatic
        fun reveal(@StringRes id: Int, version: Version, vararg formatArgs: Any): String? {
            return if (context == null) {
                Log.e(tag, "Library not initialized: SC.init(Context)")
                null
            } else return when (version) {
                Version.V0 -> JavaLogic.getString(context!!, id, formatArgs[0] as Array<out Any>)
                Version.V1 -> CPlusLogic.revealV1(context!!, id, formatArgs[0] as Array<out Any>)
                Version.V2 -> CPlusLogic.revealV2(context!!, id, formatArgs[0] as Array<out Any>)
            }
        }


    }

    external fun jniObfuscateV1(context: Context, key: String?, value: String): String

    external fun jniRevealV1(context: Context, key: String?, value: String): String

    external fun jniObfuscateV2(context: Context, key: String?, value: ByteArray): ByteArray

    external fun jniRevealV2(context: Context, key: String?, value: ByteArray): ByteArray

}
