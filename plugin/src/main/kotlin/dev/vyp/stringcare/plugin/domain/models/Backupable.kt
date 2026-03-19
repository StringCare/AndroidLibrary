package dev.vyp.stringcare.plugin.domain.models

import java.io.File

/** Filesystem types that support backup into a StringCare temp root. */
fun interface Backupable {
    fun backup(tempRoot: String): File
}
