package models

import java.io.File

data class ResourceFile(val file: File, val sourceFolder: String, val module: String)