package dev.vyp.stringcare.plugin.internal

import dev.vyp.stringcare.plugin.StringCarePlugin
import java.io.File
import java.io.InputStream
import java.net.JarURLConnection
import java.net.URL
import java.util.zip.ZipFile

open class Stark {

    companion object {
        /**
         * Native libraries must NOT be loaded in a static initializer: Gradle loads plugin task classes during
         * **configuration** phase, while classpath resources for the plugin JAR are reliably visible during
         * **task execution**. Loading here used to run too early → stream null / load failure → permanent "Skipping".
         */
        @Volatile
        private var nativeLibLoaded = false

        @Volatile
        private var nativeLoadAttempted = false

        private fun logNative(message: String) {
            // if (StringCarePlugin.configuration.debug) {
                println("[StringCare native] $message")
            // }
        }

        @JvmStatic
        fun isNativeLibLoaded(): Boolean {
            if (!nativeLoadAttempted) {
                logNative(
                    "Loading native library (os.name=${System.getProperty("os.name")}, " +
                        "os.arch=${System.getProperty("os.arch")}, os.detected=${getOs()})"
                )
                nativeLibLoaded = loadNativeForHost()
                nativeLoadAttempted = true
                if (nativeLibLoaded) {
                    logNative("Native library loaded successfully.")
                } else {
                    logNative("Native library NOT loaded — obfuscation tasks will skip. See messages above.")
                }
            } else if (!nativeLibLoaded) {
                logNative("isNativeLibLoaded: returning cached false (load attempted earlier in this Gradle JVM)")
            }
            return nativeLibLoaded
        }

        /** Classpath folder where host natives live in the JAR (`dist/` layout). */
        private fun nativeResourceDir(): String = when (getOs()) {
            Os.WINDOWS -> "windows"
            Os.LINUX -> "linux"
            Os.OSX -> "macos"
        }

        private fun loadNativeForHost(): Boolean {
            val arch = (System.getProperty("os.arch") ?: "").lowercase()
            val order = when (getOs()) {
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
                if (loadLib(name)) return true
            }
            return false
        }

        /**
         * Loads a native library packaged as a classpath resource (JAR or exploded `build/resources/main`).
         * Composite builds (`includeBuild("plugin")`) load classes from `classes/kotlin/main` while natives live
         * under `resources/main` — [openNativeStream] uses classloaders so both layouts work.
         */
        private fun loadLib(name: String): Boolean {
            return try {
                val stream = openNativeStream(name)
                if (stream == null) {
                    logNative("loadLib($name): no bytes found on classpath (see openNativeStream traces)")
                    return false
                }
                val temp = createNativeTempFile(name)
                stream.use { input ->
                    temp.outputStream().use { output -> input.copyTo(output) }
                }
                val size = temp.length()
                logNative("loadLib($name): extracted $size bytes → ${temp.absolutePath}")
                System.load(temp.absolutePath)
                logNative("loadLib($name): System.load OK")
                true
            } catch (e: UnsatisfiedLinkError) {
                logNative("loadLib($name): UnsatisfiedLinkError — ${e.message} (wrong CPU arch vs binary slice?)")
                false
            } catch (e: Throwable) {
                logNative("loadLib($name): ${e.javaClass.name} — ${e.message}")
                false
            }
        }

        private fun createNativeTempFile(libraryFileName: String): File {
            val suffix = when {
                libraryFileName.endsWith(".dylib") -> ".dylib"
                libraryFileName.endsWith(".so") -> ".so"
                libraryFileName.endsWith(".dll") -> ".dll"
                else -> ".bin"
            }
            return File.createTempFile("stringcare-signkey-", suffix).apply {
                deleteOnExit()
            }
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
            val loaders = buildList {
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

        /** Fallback when resources are not visible to the usual classloaders (edge classloaders). */
        private fun openNativeStreamFromCodeSource(fileName: String): InputStream? {
            val url: URL = try {
                Stark::class.java.protectionDomain?.codeSource?.location
            } catch (_: Throwable) {
                null
            } ?: run {
                logNative("openNativeStreamFromCodeSource: codeSource.location is null")
                return null
            }
            logNative("openNativeStreamFromCodeSource: codeSource=$url")
            return when (url.protocol) {
                "file" -> {
                    val file = try {
                        File(url.toURI())
                    } catch (_: Throwable) {
                        logNative("openNativeStreamFromCodeSource: toURI failed for $url")
                        return null
                    }
                    when {
                        file.isFile && file.name.endsWith(".jar") -> {
                            val s = openStreamFromPlainJar(file, fileName)
                            if (s != null) logNative("openNativeStreamFromCodeSource: found inside JAR ${file.absolutePath}")
                            else logNative("openNativeStreamFromCodeSource: no entry $fileName in ${file.absolutePath}")
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
                                        "openNativeStreamFromCodeSource: not under ${file.absolutePath} nor $resMain"
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

        private fun findNativeFile(dir: File, fileName: String): File? {
            if (!dir.isDirectory) return null
            return dir.walkTopDown().find { it.isFile && it.name == fileName }
        }

        private fun openStreamFromPlainJar(jar: File, fileName: String): InputStream? {
            if (!jar.isFile) return null
            return ZipFile(jar).use { zip ->
                val entry = zip.entries().asSequence().firstOrNull { e ->
                    !e.isDirectory && (e.name == fileName || e.name.endsWith("/$fileName"))
                } ?: return null
                zip.getInputStream(entry).readBytes().inputStream()
            }
        }

        private fun openStreamFromJarUrl(url: URL, fileName: String): InputStream? {
            return try {
                val conn = url.openConnection() as JarURLConnection
                val jar = conn.jarFile
                val entry = jar.entries().asSequence().firstOrNull { e ->
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
        external fun obfuscate(key: String, value: ByteArray, mockId: String): ByteArray

        @JvmStatic
        external fun reveal(key: String, value: ByteArray, mockId: String): ByteArray
    }
}
