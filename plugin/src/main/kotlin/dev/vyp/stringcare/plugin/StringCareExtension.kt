package dev.vyp.stringcare.plugin

/**
 * DSL configuration for the StringCare plugin.
 */
open class StringCareExtension {
    var assetsFiles = mutableListOf<String>()
    var stringFiles = mutableListOf<String>()
    var srcFolders = mutableListOf<String>()
    var debug = false
    var skip = false
    var mockedFingerprint = ""
}
