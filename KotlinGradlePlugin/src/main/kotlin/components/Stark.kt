package components

import StringCare
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipFile

open class Stark {

    companion object {
        init {
            when (getOs()) {
                Os.WINDOWS -> loadLib(winLib)
                Os.OSX -> loadLib(osxLib)
            }
        }

        private fun loadLib(name: String) {
            val inputStream = getLibFromFolder(name)?.inputStream()?: return
            val buffer = ByteArray(1024)
            val temp = File.createTempFile(name, "")
            val fos = FileOutputStream(temp)

            var read = inputStream.read(buffer)
            while (read != -1) {
                fos.write(buffer, 0, read)
                read = inputStream.read(buffer)
            }
            fos.close()
            inputStream.close()

            System.load(temp.absolutePath)
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

                                    lib = File("${StringCare.tempFolder}${File.separator}${entry.name}").apply {
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