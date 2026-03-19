package dev.vyp.stringcare.plugin.models

import java.io.File

data class AssetsFile(
    val file: File,
    val sourceFolder: String,
    val module: String,
)
