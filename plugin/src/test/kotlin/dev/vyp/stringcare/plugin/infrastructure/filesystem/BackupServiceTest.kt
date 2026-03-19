package dev.vyp.stringcare.plugin.infrastructure.filesystem

import dev.vyp.stringcare.plugin.internal.defaultConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files

class BackupServiceTest {
    @Test
    fun `backupStringResources copies values xml under temp root`() {
        val root = Files.createTempDirectory("sc-backup-svc").toFile()
        val backupRoot = Files.createTempDirectory("sc-backup-dest").toFile()
        try {
            val valuesDir = File(root, "app/src/main/res/values").apply { mkdirs() }
            val xml =
                """
                <?xml version="1.0" encoding="utf-8"?>
                <resources>
                    <string name="visible">ok</string>
                </resources>
                """.trimIndent()
            File(valuesDir, "strings.xml").writeText(xml)
            val config = defaultConfig()
            val svc = BackupService()
            val backed = svc.backupStringResources(root.absolutePath, config, backupRoot.absolutePath)
            assertEquals(1, backed.size)
            val expected =
                File(
                    backupRoot,
                    "app${File.separator}src${File.separator}main${File.separator}res${File.separator}values${File.separator}strings.xml",
                )
            assertTrue(expected.exists(), "expected $expected")
            assertEquals(xml, expected.readText())
        } finally {
            root.deleteRecursively()
            backupRoot.deleteRecursively()
        }
    }
}
