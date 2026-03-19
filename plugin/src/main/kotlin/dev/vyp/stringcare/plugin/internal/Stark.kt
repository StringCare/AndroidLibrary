package dev.vyp.stringcare.plugin.internal

import java.io.File
import java.io.InputStream
import java.net.JarURLConnection
import java.net.URL
import java.nio.file.Files
import java.security.MessageDigest
import java.util.zip.ZipFile

/** Result of attempting to load the host-side JNI library. */
sealed class NativeLoadResult {
    data object Loaded : NativeLoadResult()

    data class Failed(
        val message: String,
    ) : NativeLoadResult()
}

open class Stark {
    companion object {
        /**
         * SHA-256 (hex) of bundled prebuilts under `internal/jni/` / `dist/`.
         * Update when replacing native binaries (run `shasum -a 256` on each file).
         */
        private val EXPECTED_SHA256: Map<String, String> =
            mapOf(
                winLib to "cd4f6226b7d073f67f1810c3dbdfdd722baba79f43214396e117650d88aa7c7e",
                winLibArm64 to "319ab0f5d055be6b4090f5307fb351ff6680438eb5e734d140f61ac887d37e63",
                osxLib to "a352ee421e017966f6234030980291326015ca07fcdd37e9ed0fbb7361d1ea7d",
                linuxLib to "42010b2512ed512aaba720ee398ec20e2b3d509de5c3142a293f6cb97d4db8cf",
                linuxLibArm64 to "17de04a716395f69020642c88363c0caa54863d323f1702478c6443541b7eba5",
            )

        @Volatile
        private var nativeLibLoaded = false

        @Volatile
        private var nativeLoadAttempted = false

        @Volatile
        private var verboseNativeLogging = true

        @JvmStatic
        fun setVerboseNativeLogging(enabled: Boolean) {
            verboseNativeLogging = enabled
        }

        private fun logNative(message: String) {
            if (verboseNativeLogging) {
                println("[StringCare native] $message")
            }
        }

        private fun sha256Hex(bytes: ByteArray): String {
            val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
            return digest.joinToString("") { b -> "%02x".format(b) }
        }

        @JvmStatic
        fun nativeLoadResult(): NativeLoadResult {
            synchronized(Stark::class.java) {
                if (!nativeLoadAttempted) {
                    logNative(
                        "Loading native library (os.name=${System.getProperty("os.name")}, " +
                            "os.arch=${System.getProperty("os.arch")}, os.detected=${getOs()})",
                    )
                    nativeLibLoaded = loadNativeForHostWithRetries()
                    nativeLoadAttempted = true
                    if (nativeLibLoaded) {
                        logNative("Native library loaded successfully.")
                    } else {
                        logNative("Native library NOT loaded — obfuscation tasks will skip. See messages above.")
                    }
                } else if (!nativeLibLoaded) {
                    logNative("isNativeLibLoaded: returning cached false (load attempted earlier in this Gradle JVM)")
                }
                return if (nativeLibLoaded) NativeLoadResult.Loaded else NativeLoadResult.Failed("Native library not loaded")
            }
        }

        @JvmStatic
        fun isNativeLibLoaded(): Boolean = nativeLoadResult() is NativeLoadResult.Loaded

        private fun nativeResourceDir(): String =
            when (getOs()) {
                Os.WINDOWS -> "windows"
                Os.LINUX -> "linux"
                Os.OSX -> "macos"
            }

        private fun loadNativeForHostWithRetries(): Boolean {
            val arch = (System.getProperty("os.arch") ?: "").lowercase()
            val order =
                when (getOs()) {
                    Os.WINDOWS -> {
                        if (arch == "aarch64") listOf(winLibArm64, winLib) else listOf(winLib, winLibArm64)
                    }
                    Os.OSX -> listOf(osxLib)
                    Os.LINUX -> {
                        if (arch == "aarch64") listOf(linuxLibArm64, linuxLib) else listOf(linuxLib, linuxLibArm64)
                    }
                }
            logNative("Try order for this host: ${order.joinToString()}")
            for (name in order) {
                repeat(3) { attempt ->
                    if (loadLib(name)) return true
                    if (attempt < 2) {
                        try {
                            Thread.sleep(100L * (attempt + 1))
                        } catch (_: InterruptedException) {
                            Thread.currentThread().interrupt()
                        }
                    }
                }
            }
            return false
        }

        private fun loadLib(name: String): Boolean {
            var tempFile: File? = null
            return try {
                val stream = openNativeStream(name)
                if (stream == null) {
                    logNative("loadLib($name): no bytes found on classpath (see openNativeStream traces)")
                    return false
                }
                val bytes = stream.use { it.readBytes() }
                val expected = EXPECTED_SHA256[name]
                if (expected != null) {
                    val actual = sha256Hex(bytes)
                    if (!actual.equals(expected, ignoreCase = true)) {
                        logNative("loadLib($name): SHA-256 mismatch (expected $expected, was $actual)")
                        throw SecurityException("StringCare native library checksum mismatch for $name")
                    }
                    logNative("loadLib($name): SHA-256 OK")
                }
                tempFile = createNativeTempFile(name)
                tempFile.writeBytes(bytes)
                val size = tempFile.length()
                logNative("loadLib($name): extracted $size bytes → ${tempFile.absolutePath}")
                System.load(tempFile.absolutePath)
                tempFile.deleteOnExit()
                logNative("loadLib($name): System.load OK")
                true
            } catch (e: UnsatisfiedLinkError) {
                logNative("loadLib($name): UnsatisfiedLinkError — ${e.message} (wrong CPU arch vs binary slice?)")
                tempFile?.let { runCatching { it.delete() } }
                false
            } catch (e: SecurityException) {
                logNative("loadLib($name): SecurityException — ${e.message}")
                tempFile?.let { runCatching { it.delete() } }
                false
            } catch (e: Throwable) {
                logNative("loadLib($name): ${e.javaClass.name} — ${e.message}")
                tempFile?.let { runCatching { it.delete() } }
                false
            }
        }

        private fun createNativeTempFile(libraryFileName: String): File {
            val suffix =
                when {
                    libraryFileName.endsWith(".dylib") -> ".dylib"
                    libraryFileName.endsWith(".so") -> ".so"
                    libraryFileName.endsWith(".dll") -> ".dll"
                    else -> ".bin"
                }
            return Files.createTempFile("stringcare-signkey-", suffix).toFile()
        }

        private fun openNativeStream(fileName: String): InputStream? {
            val platformDir = nativeResourceDir()
            val paths = listOf("/$platformDir/$fileName", "/$fileName")
            for (path in paths) {
                Stark::class.java.getResourceAsStream(path)?.let {
                    logNative("openNativeStream($fileName): found via Stark.class $path")
                    return it
                }
            }
            logNative("openNativeStream($fileName): not on Stark.class resources, trying classloaders…")
            val loaders =
                buildList {
                    add(Stark::class.java.classLoader)
                    add(Thread.currentThread().contextClassLoader)
                    add(ClassLoader.getSystemClassLoader())
                }.distinct()
            loaders.forEachIndexed { i, loader ->
                if (loader == null) {
                    logNative("openNativeStream: loader[$i]=null")
                    return@forEachIndexed
                }
                val label = loader.javaClass.name
                for (path in listOf("$platformDir/$fileName", "/$platformDir/$fileName", fileName, "/$fileName")) {
                    loader.getResourceAsStream(path)?.let {
                        logNative("openNativeStream($fileName): found via loader[$i] $label as $path")
                        return it
                    }
                }
            }
            logNative("openNativeStream($fileName): trying codeSource / filesystem / nested JAR fallback…")
            return openNativeStreamFromCodeSource(fileName)
        }

        private fun openNativeStreamFromCodeSource(fileName: String): InputStream? {
            val url: URL =
                try {
                    Stark::class.java.protectionDomain
                        ?.codeSource
                        ?.location
                } catch (_: Throwable) {
                    null
                } ?: run {
                    logNative("openNativeStreamFromCodeSource: codeSource.location is null")
                    return null
                }
            logNative("openNativeStreamFromCodeSource: codeSource=$url")
            return when (url.protocol) {
                "file" -> {
                    val file =
                        try {
                            File(url.toURI())
                        } catch (_: Throwable) {
                            logNative("openNativeStreamFromCodeSource: toURI failed for $url")
                            return null
                        }
                    when {
                        file.isFile && file.name.endsWith(".jar") -> {
                            val s = openStreamFromPlainJar(file, fileName)
                            if (s != null) {
                                logNative("openNativeStreamFromCodeSource: found inside JAR ${file.absolutePath}")
                            } else {
                                logNative("openNativeStreamFromCodeSource: no entry $fileName in ${file.absolutePath}")
                            }
                            s
                        }
                        file.isDirectory -> {
                            val resMain = File(file, "../../../resources/main")
                            val a = findNativeFile(file, fileName)?.inputStream()
                            val b = findNativeFile(resMain, fileName)?.inputStream()
                            when {
                                a != null -> {
                                    logNative("openNativeStreamFromCodeSource: file under classes dir ${file.absolutePath}")
                                    a
                                }
                                b != null -> {
                                    logNative("openNativeStreamFromCodeSource: file under ${resMain.absolutePath}")
                                    b
                                }
                                else -> {
                                    logNative(
                                        "openNativeStreamFromCodeSource: not under ${file.absolutePath} nor $resMain",
                                    )
                                    null
                                }
                            }
                        }
                        else -> {
                            logNative("openNativeStreamFromCodeSource: unexpected file location $file")
                            null
                        }
                    }
                }
                "jar" -> {
                    val s = openStreamFromJarUrl(url, fileName)
                    if (s == null) logNative("openNativeStreamFromCodeSource: jar: URL had no entry $fileName")
                    s
                }
                else -> {
                    logNative("openNativeStreamFromCodeSource: unsupported protocol ${url.protocol}")
                    null
                }
            }
        }

        private fun findNativeFile(
            dir: File,
            fileName: String,
        ): File? {
            if (!dir.isDirectory) return null
            return dir.walkTopDown().find { it.isFile && it.name == fileName }
        }

        private fun openStreamFromPlainJar(
            jar: File,
            fileName: String,
        ): InputStream? {
            if (!jar.isFile) return null
            return ZipFile(jar).use { zip ->
                val entry =
                    zip.entries().asSequence().firstOrNull { e ->
                        !e.isDirectory && (e.name == fileName || e.name.endsWith("/$fileName"))
                    } ?: return null
                zip.getInputStream(entry).readBytes().inputStream()
            }
        }

        private fun openStreamFromJarUrl(
            url: URL,
            fileName: String,
        ): InputStream? {
            return try {
                val conn = url.openConnection() as JarURLConnection
                val jar = conn.jarFile
                val entry =
                    jar.entries().asSequence().firstOrNull { e ->
                        !e.isDirectory && (e.name == fileName || e.name.endsWith("/$fileName"))
                    } ?: return null
                logNative("openStreamFromJarUrl: entry ${entry.name}")
                jar.getInputStream(entry).readBytes().inputStream()
            } catch (e: Throwable) {
                logNative("openStreamFromJarUrl: ${e.javaClass.simpleName} — ${e.message}")
                null
            }
        }

        @JvmStatic
        external fun obfuscate(
            key: String,
            value: ByteArray,
            mockId: String,
        ): ByteArray

        @JvmStatic
        external fun reveal(
            key: String,
            value: ByteArray,
            mockId: String,
        ): ByteArray
    }
}
