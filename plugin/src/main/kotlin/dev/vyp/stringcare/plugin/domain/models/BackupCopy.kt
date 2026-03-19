package dev.vyp.stringcare.plugin.domain.models

import java.io.File
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * Copies [source] to [dest] via a temp file in the same directory, then replaces [dest].
 * Uses [StandardCopyOption.ATOMIC_MOVE] when supported so readers rarely see a half-written file.
 */
internal fun copyToBackupAtomically(
    source: File,
    dest: File,
) {
    dest.parentFile?.mkdirs()
    val parent = dest.parentFile ?: error("Backup destination has no parent: ${dest.absolutePath}")
    val tmp = File.createTempFile("sc-backup-", ".tmp", parent)
    try {
        Files.copy(source.toPath(), tmp.toPath(), StandardCopyOption.REPLACE_EXISTING)
        try {
            Files.move(
                tmp.toPath(),
                dest.toPath(),
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING,
            )
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(tmp.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
    } finally {
        if (tmp.exists()) {
            tmp.delete()
        }
    }
}
