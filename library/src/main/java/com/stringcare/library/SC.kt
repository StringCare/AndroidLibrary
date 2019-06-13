package com.stringcare.library

import android.content.Context

import android.support.annotation.StringRes
import android.util.Log
import java.lang.Exception

import java.util.ArrayList

/**
 * Created by efrainespada on 02/10/2016.
 */

class SC {

    companion object {

        init {
            System.loadLibrary("native-lib")
        }

        private val context: Context
            get() = when (contextFun) {
                null -> throw Exception("Context not defined yet.")
                else -> contextFun!!.invoke()
            }

        private var contextFun: (() -> Context)? = null

        private val listeners = ArrayList<ContextListener>()

        @JvmStatic
        fun init(c: Context) {
            contextFun = { c }
            if (listeners.isNotEmpty()) {
                for (listener in listeners) {
                    listener.contextReady()
                }
            }
        }

        @JvmStatic
        fun init(lambda: () -> Context) {
            contextFun = lambda
            if (listeners.isNotEmpty()) {
                for (listener in listeners) {
                    listener.contextReady()
                }
            }
        }

        @JvmStatic
        fun onContextReady(listener: ContextListener) {
            if (contextFun != null) {
                listener.contextReady()
                return
            }
            listeners.add(listener)
        }

        @JvmStatic
        fun obfuscate(value: String): String {
            return obfuscate(value, true, defaultVersion)
        }

        /**
         * Obfuscates the given value
         * @param value
         * @return String
         */
        @JvmStatic
        fun obfuscate(value: String, androidTreatment: Boolean = true, version: Version = defaultVersion): String {
            return if (contextFun == null) {
                Log.e(tag, initializationNeeded)
                value
            } else when (version) {
                Version.V0 -> JavaLogic.encryptString(context, value)
                Version.V1 -> CPlusLogic.obfuscateV1(context, value)
                Version.V2 -> CPlusLogic.obfuscateV2(context, value)
                Version.V3 -> CPlusLogic.obfuscateV3(context, value, androidTreatment)
            }
        }

        @JvmStatic
        fun reveal(@StringRes id: Int): String {
            return reveal(id, defaultVersion)
        }

        /**
         * Deobfuscates the given value
         * @param id
         * @return String
         */
        @JvmStatic
        fun reveal(@StringRes id: Int, androidTreatment: Boolean = true, version: Version = defaultVersion): String {
            return if (contextFun == null) {
                Log.e(tag, initializationNeeded)
                ""
            } else when (version) {
                Version.V0 -> JavaLogic.getString(context, id)
                Version.V1 -> CPlusLogic.revealV1(context, id)
                Version.V2 -> CPlusLogic.revealV2(context, id)
                Version.V3 -> CPlusLogic.revealV3(context, id, androidTreatment)
            }
        }

        @JvmStatic
        fun reveal(value: String): String {
            return reveal(value, defaultAndroidTreatment, defaultVersion)
        }

        @JvmStatic
        fun reveal(value: String, version: Version = defaultVersion): String {
            return reveal(value, defaultAndroidTreatment, version)
        }

        @JvmStatic
        fun reveal(value: String, androidTreatment: Boolean): String {
            return reveal(value, androidTreatment, defaultVersion)
        }

        /**
         * Deobfuscates the given value
         * @param value
         * @return String
         */
        @JvmStatic
        fun reveal(value: String, androidTreatment: Boolean = defaultAndroidTreatment, version: Version = defaultVersion): String {
            return if (contextFun == null) {
                Log.e(tag, initializationNeeded)
                value
            } else when (version) {
                Version.V0 -> JavaLogic.decryptString(context, value)
                Version.V1 -> CPlusLogic.revealV1(context, value)
                Version.V2 -> CPlusLogic.revealV2(context, value)
                Version.V3 -> CPlusLogic.revealV3(context, value, androidTreatment)
            }
        }

        @JvmStatic
        fun reveal(@StringRes id: Int, vararg formatArgs: Any): String {
            return reveal(id, defaultAndroidTreatment, defaultVersion, formatArgs)
        }

        @JvmStatic
        fun reveal(@StringRes id: Int, version: Version = defaultVersion, vararg formatArgs: Any): String {
            return reveal(id, defaultAndroidTreatment, version, formatArgs)
        }

        @JvmStatic
        fun reveal(@StringRes id: Int, androidTreatment: Boolean, vararg formatArgs: Any): String {
            return reveal(id, androidTreatment, defaultVersion, formatArgs)
        }

        /**
         * Deobfuscates the given value
         * @param id
         * @param formatArgs
         * @return
         */
        @JvmStatic
        fun reveal(@StringRes id: Int, androidTreatment: Boolean, version: Version, vararg formatArgs: Any): String {
            return if (contextFun == null) {
                Log.e(tag, initializationNeeded)
                ""
            } else return when (version) {
                Version.V0 -> JavaLogic.getString(context, id, formatArgs[0] as Array<out Any>)
                Version.V1 -> CPlusLogic.revealV1(context, id, formatArgs[0] as Array<out Any>)
                Version.V2 -> CPlusLogic.revealV2(context, id, formatArgs[0] as Array<out Any>)
                Version.V3 -> CPlusLogic.revealV3(context, id, androidTreatment, formatArgs[0] as Array<out Any>)
            }
        }


    }

    external fun jniObfuscateV1(context: Context, key: String, value: String): String

    external fun jniRevealV1(context: Context, key: String, value: String): String

    external fun jniObfuscateV2(context: Context, key: String, value: ByteArray): ByteArray

    external fun jniRevealV2(context: Context, key: String, value: ByteArray): ByteArray

    external fun jniObfuscateV3(context: Context, key: String, value: ByteArray): ByteArray

    external fun jniRevealV3(context: Context, key: String, value: ByteArray): ByteArray

}
