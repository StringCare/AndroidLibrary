package dev.vyp.stringcare.plugin.internal

import dev.vyp.stringcare.plugin.StringCarePlugin
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipFile

open class Stark {

    companion object {
        @Volatile
        private var nativeLibLoaded = false

        init {
            val arch = System.getProperty("os.arch", "").lowercase()
            when (getOs()) {
                Os.WINDOWS -> {
                    nativeLibLoaded = if (arch == "aarch64") {
                        loadLib(winLibArm64) || loadLib(winLib)
                    } else {
                        loadLib(winLib) || loadLib(winLibArm64)
                    }
                }
                Os.OSX -> nativeLibLoaded = loadLib(osxLib)
                Os.LINUX -> {
                    nativeLibLoaded = if (arch == "aarch64") {
                        loadLib(linuxLibArm64) || loadLib(linuxLib)
                    } else {
                        loadLib(linuxLib) || loadLib(linuxLibArm64)
                    }
                }
            }
        }

        @JvmStatic
        fun isNativeLibLoaded(): Boolean = nativeLibLoaded

        private fun loadLib(name: String): Boolean {
            val file = getLibFromFolder(name) ?: return false
            return try {
                val inputStream = file.inputStream()
                val buffer = ByteArray(1024)
                val temp = File.createTempFile(name, "")
                FileOutputStream(temp).use { fos ->
                    var read = inputStream.read(buffer)
                    while (read != -1) {
                        fos.write(buffer, 0, read)
                        read = inputStream.read(buffer)
                    }
                }
                inputStream.close()
                System.load(temp.absolutePath)
                true
            } catch (_: UnsatisfiedLinkError) {
                false
            }
        }

        private fun getLibFromFolder(fileName: String): File? {
            var lib: File? = null
            val dir = Stark::class.java.protectionDomain.codeSource.location.toURI()
            when {
                dir.toString().endsWith(".jar") -> {
                    val jar = File(dir)
                    val zipFile = File(jar.absolutePath.replace(".jar", ".zip"))

                    jar.copyTo(zipFile, true)

                    ZipFile(zipFile.absolutePath).use { zip ->
                        zip.entries().asSequence().forEach { entry ->
                            zip.getInputStream(entry).use { input ->
                                if (entry.name == fileName) {

                                    lib = File("${StringCarePlugin.tempFolder}${File.separator}${entry.name}").apply {
                                        this.outputStream().use { output ->
                                            input.copyTo(output)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {
                    lib = File(dir).walkTopDown().find { file ->
                        file.name == fileName
                    }
                }
            }
            return lib
        }

        @JvmStatic
        external fun obfuscate(key: String, value: ByteArray, mockId: String): ByteArray

        @JvmStatic
        external fun reveal(key: String, value: ByteArray, mockId: String): ByteArray
    }
}
