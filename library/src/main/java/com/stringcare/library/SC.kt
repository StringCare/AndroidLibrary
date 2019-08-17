package com.stringcare.library

import android.content.Context
import android.support.annotation.StringRes
import android.util.Log
import org.jetbrains.anko.doAsync
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.Charset

/**
 * Created by efrainespada on 02/10/2016.
 */

class SC {

    companion object {

        init {
            System.loadLibrary("native-lib")
        }

        val context: Context
            get() = when (contextFun) {
                null -> throw StringcareException("Context not defined yet.")
                else -> contextFun!!()
            }

        private var contextFun: (() -> Context)? = null

        private val listeners = mutableListOf<ContextListener>()

        /**
         * Context getter. Common implementation
         */
        @JvmStatic
        fun init(c: Context) {
            contextFun = { c }
            processPendingContextListener()
        }

        /**
         * Context getter. Lambda implementation
         */
        @JvmStatic
        fun init(context: () -> Context) {
            contextFun = context
            processPendingContextListener()
        }

        /**
         * Process pending context listeners
         */
        private fun processPendingContextListener() {
            if (listeners.isNotEmpty())
                listeners.forEach { it.contextReady() }
        }

        /**
         * Holds all context listeners.
         */
        @JvmStatic
        fun onContextReady(listener: ContextListener) {
            if (contextFun != null) {
                listener.contextReady()
                return
            }
            listeners.add(listener)
        }

        /**
         * Obfuscates the string value
         * @param value
         * @return String
         */
        @JvmStatic
        fun obfuscate(value: String): String {
            return obfuscate(value, defaultAndroidTreatment, defaultVersion)
        }

        /**
         * Obfuscates the given value
         * @param value
         * @param androidTreatment
         * @param version
         * @return String
         */
        @JvmStatic
        fun obfuscate(
                value: String,
                androidTreatment: Boolean = defaultAndroidTreatment,
                version: Version = defaultVersion
        ): String {
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

        /**
         * Reveals the Int (@StringRes) value
         * @param id
         * @return String
         */
        @JvmStatic
        fun reveal(@StringRes id: Int): String {
            return reveal(id, defaultVersion)
        }

        /**
         * Reveals the Int (@StringRes) value
         * @param id
         * @param androidTreatment
         * @return String
         */
        @JvmStatic
        fun reveal(
                @StringRes id: Int,
                androidTreatment: Boolean = defaultAndroidTreatment
        ): String {
            return reveal(id, androidTreatment, defaultVersion)
        }

        /**
         * Reveals the Int (@StringRes) value
         * @param id
         * @param version
         * @return String
         */
        @JvmStatic
        fun reveal(@StringRes id: Int, version: Version = defaultVersion): String {
            return reveal(id, defaultAndroidTreatment, version)
        }

        /**
         * Reveals the Int (@StringRes) value
         * @param id
         * @param androidTreatment
         * @param version
         * @return String
         */
        @JvmStatic
        fun reveal(
                @StringRes id: Int,
                androidTreatment: Boolean = defaultAndroidTreatment,
                version: Version = defaultVersion
        ): String {
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

        /**
         * Reveals the String value
         * @param value
         * @return String
         */
        @JvmStatic
        fun reveal(value: String): String {
            return reveal(value, defaultAndroidTreatment, defaultVersion)
        }

        /**
         * Reveals the String value
         * @param value
         * @param version
         * @return String
         */
        @JvmStatic
        fun reveal(value: String, version: Version = defaultVersion): String {
            return reveal(value, defaultAndroidTreatment, version)
        }

        /**
         * Reveals the String value
         * @param value
         * @param androidTreatment
         * @return String
         */
        @JvmStatic
        fun reveal(value: String, androidTreatment: Boolean): String {
            return reveal(value, androidTreatment, defaultVersion)
        }

        /**
         * Reveals the String value
         * @param value
         * @param androidTreatment
         * @param version
         * @return String
         */
        @JvmStatic
        fun reveal(
                value: String,
                androidTreatment: Boolean = defaultAndroidTreatment,
                version: Version = defaultVersion
        ): String {
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

        /**
         * Reveals the Int (@StringRes) value with vararg
         * @param id
         * @param formatArgs
         * @return String
         */
        @JvmStatic
        fun reveal(@StringRes id: Int, vararg formatArgs: Any): String {
            return reveal(id, defaultAndroidTreatment, defaultVersion, formatArgs)
        }

        /**
         * Reveals the Int (@StringRes) value with vararg
         * @param id
         * @param version
         * @param formatArgs
         * @return String
         */
        @JvmStatic
        fun reveal(
                @StringRes id: Int,
                version: Version = defaultVersion,
                vararg formatArgs: Any
        ): String {
            return reveal(id, defaultAndroidTreatment, version, formatArgs)
        }

        /**
         * Reveals the Int (@StringRes) value with vararg
         * @param id
         * @param androidTreatment
         * @param formatArgs
         * @return String
         */
        @JvmStatic
        fun reveal(
                @StringRes id: Int,
                androidTreatment: Boolean = defaultAndroidTreatment,
                vararg formatArgs: Any
        ): String {
            return reveal(id, androidTreatment, defaultVersion, formatArgs)
        }

        /**
         * Reveals the Int (@StringRes) value with vararg
         * @param id
         * @param androidTreatment
         * @param version
         * @param formatArgs
         * @return String
         */
        @JvmStatic
        fun reveal(
                @StringRes id: Int,
                androidTreatment: Boolean = defaultAndroidTreatment,
                version: Version = defaultVersion,
                vararg formatArgs: Any
        ): String {
            return when (contextFun) {
                null -> {
                    Log.e(tag, initializationNeeded)
                    ""
                }
                else -> return when (version) {
                    Version.V0 -> JavaLogic.getString(context, id, formatArgs[0] as Array<out Any>)
                    Version.V1 -> CPlusLogic.revealV1(context, id, formatArgs[0] as Array<out Any>)
                    Version.V2 -> CPlusLogic.revealV2(context, id, formatArgs[0] as Array<out Any>)
                    Version.V3 -> CPlusLogic.revealV3(
                            context,
                            id,
                            androidTreatment,
                            formatArgs[0] as Array<out Any>
                    )
                }
            }
        }

        private fun assetByteArray(path: String, predicate: () -> Boolean = { true }): ByteArray {
            val inputStream = context.assets.openFd(path)
            var bytes = inputStream.createInputStream().readBytes()
            if (predicate()) {
                bytes = CPlusLogic.revealByteArray(context, bytes)
            }
            return bytes
        }

        @JvmStatic
        fun asset(): Assets {
            return Assets()
        }

    }

    class Assets {
        fun json(path: String, predicate: () -> Boolean): JSONObject {
            val bytes = assetByteArray(path, predicate)
            return JSONObject(String(bytes, Charset.forName("UTF-8")))
        }

        fun asyncJson(path: String,
                      predicate: () -> Boolean = { true },
                      json: (json: JSONObject) -> Unit) {
            doAsync {
                val j = json(path, predicate)
                json(j)
            }
        }

        fun jsonArray(path: String, predicate: () -> Boolean): JSONArray {
            val bytes = assetByteArray(path, predicate)
            return JSONArray(String(bytes, Charset.forName("UTF-8")))
        }

        fun asyncJsonArray(path: String,
                           predicate: () -> Boolean = { true },
                           json: (json: JSONArray) -> Unit) {
            doAsync {
                val j = jsonArray(path, predicate)
                json(j)
            }
        }

        fun json(path: String): JSONObject {
            return json(path) { true }
        }

        fun json(path: String, predicate: Boolean): JSONObject {
            return json(path) { predicate }
        }

        fun asyncJson(path: String, jsonObjectListener: JSONObjectListener) {
            asyncJson(path, jsonObjectListener, true)
        }

        fun asyncJson(path: String, jsonObjectListener: JSONObjectListener, predicate: Boolean) {
            asyncJson(path, { predicate }, jsonObjectListener::assetReady)
        }

        fun jsonArray(path: String): JSONArray {
            return jsonArray(path) { true }
        }

        fun jsonArray(path: String, predicate: Boolean): JSONArray {
            return jsonArray(path) { predicate }
        }

        fun asyncJsonArray(path: String, jsonArrayListener: JSONArrayListener) {
            asyncJsonArray(path, jsonArrayListener, true)
        }

        fun asyncJsonArray(path: String, jsonArrayListener: JSONArrayListener, predicate: Boolean) {
            asyncJsonArray(path, { predicate }, jsonArrayListener::assetReady)
        }

        fun bytes(path: String, predicate: () -> Boolean): ByteArray {
            return assetByteArray(path, predicate)
        }

        fun asyncBytes(path: String,
                       predicate: () -> Boolean = { true },
                       bytes: (bytes: ByteArray) -> Unit) {
            doAsync {
                bytes(assetByteArray(path, predicate))
            }
        }

        fun bytes(path: String): ByteArray {
            return bytes(path, true)
        }

        fun bytes(path: String, predicate: Boolean): ByteArray {
            return bytes(path) { predicate }
        }

        fun asyncBytes(path: String, byteArrayListener: AssetByteArrayListener) {
            asyncBytes(path, byteArrayListener, true)
        }

        fun asyncBytes(path: String, byteArrayListener: AssetByteArrayListener, predicate: Boolean) {
            doAsync {
                asyncBytes(path, { predicate }, byteArrayListener::assetReady)
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
