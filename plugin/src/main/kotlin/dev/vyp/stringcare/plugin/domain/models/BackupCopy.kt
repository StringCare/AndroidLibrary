package dev.vyp.stringcare.plugin.domain.models

import java.io.File
import java.io.RandomAccessFile
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest

/**
 * Copies [source] to [dest] via a temp file in the same directory, then replaces [dest].
 * Uses [StandardCopyOption.ATOMIC_MOVE] when supported so readers rarely see a half-written file.
 */
internal fun copyToBackupAtomically(
    source: File,
    dest: File,
) {
    withFileLock(dest) {
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
            val sourceChecksum = source.sha256Hex()
            val backupChecksum = dest.sha256Hex()
            check(sourceChecksum == backupChecksum) {
                "Backup checksum mismatch for ${source.absolutePath} -> ${dest.absolutePath}"
            }
        } finally {
            if (tmp.exists()) {
                tmp.delete()
            }
        }
    }
}

internal inline fun <T> withFileLock(
    file: File,
    action: () -> T,
): T {
    file.parentFile?.mkdirs()
    if (!file.exists()) {
        file.createNewFile()
    }
    RandomAccessFile(file, "rw").use { raf ->
        raf.channel.use { channel ->
            channel.lock().use {
                return action()
            }
        }
    }
}

internal inline fun <T> withBackup(
    backupFile: File,
    targetFile: File,
    action: () -> T,
): T {
    return try {
        action()
    } catch (error: Throwable) {
        withFileLock(targetFile) {
            Files.copy(
                backupFile.toPath(),
                targetFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
            )
        }
        throw error
    }
}

/**
 * Shared backup/rollback workflow for any [Backupable] item.
 *
 * Backups are created into [tempRoot], then [action] is executed for each item.
 * If [action] fails for an item, that item is restored from its backup and the error is rethrown.
 */
internal inline fun <T : Backupable> forEachWithBackup(
    items: Iterable<T>,
    tempRoot: String,
    action: (T) -> Unit,
) {
    items.forEach { item ->
        val backup = item.backup(tempRoot)
        val target =
            when (item) {
                is ResourceFile -> item.file
                is AssetsFile -> item.file
                else -> error("Unsupported Backupable type: ${item::class.qualifiedName}")
            }
        withBackup(backup, target) {
            action(item)
        }
    }
}

private fun File.sha256Hex(): String {
    val digest = MessageDigest.getInstance("SHA-256")
    inputStream().use { stream ->
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (true) {
            val read = stream.read(buffer)
            if (read <= 0) break
            digest.update(buffer, 0, read)
        }
    }
    return digest.digest().joinToString(separator = "") { b -> "%02x".format(b) }
}
