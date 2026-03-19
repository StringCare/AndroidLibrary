package dev.vyp.stringcare.plugin.domain.usecases

import dev.vyp.stringcare.plugin.StringCareConfiguration

/**
 * Parses Gradle `signingReport` console output and returns the SHA1 fingerprint line for
 * [module] and [variant], or an empty string when not found.
 */
object ExtractFingerprintUseCase {
    fun extract(
        reportText: String,
        module: String,
        variant: String,
        configuration: StringCareConfiguration,
        log: (module: String?, message: String, tab: Boolean) -> Unit = { _, _, _ -> },
    ): String {
        var key: String? = null
        var until: String? = null
        var variantLocated = false
        var moduleLocated = false
        val lines = reportText.split("\n")
        for (line in lines) {
            when {
                line.lowercase().contains("downloading") ->
                    if (configuration.debug) {
                        log(module, line, configuration.debug)
                    }
                line.lowercase().contains("unzipping") ->
                    if (configuration.debug) {
                        log(module, line, configuration.debug)
                    }
                line.lowercase().contains("permissions") ->
                    if (configuration.debug) {
                        log(module, line, configuration.debug)
                    }
                line.lowercase().contains("config:") && moduleLocated && variantLocated -> {
                    val k = (line.split(": ").getOrNull(1) ?: "").trim()
                    val valid = !k.equals("none", ignoreCase = true)
                    if (!valid) {
                        key = k
                        log(module, "\uD83E\uDD2F no config defined for variant $variant", true)
                        if (configuration.debug) {
                            until = key
                        }
                    } else if (configuration.debug) {
                        log(module, "Module: $module", true)
                        log(module, "Variant: $variant", true)
                    }
                }
                line.lowercase().contains("sha1") && moduleLocated && variantLocated -> {
                    key = line.split(" ").getOrNull(1) ?: ""
                    if (configuration.debug) {
                        log(module, line, configuration.debug)
                    }
                }
                line.lowercase().contains("error") -> {
                    /* reserved for diagnostics */
                }
                line.lowercase().contains("valid until") && moduleLocated && variantLocated -> {
                    until = line.split(": ").getOrNull(1)
                    if (configuration.debug) {
                        log(module, line, configuration.debug)
                    }
                }
                line.lowercase().contains("store") && moduleLocated && variantLocated ->
                    if (configuration.debug) {
                        log(module, line, configuration.debug)
                    }
                line.lowercase().contains("variant") && moduleLocated -> {
                    val locV = line.split(" ").getOrNull(1) ?: ""
                    if (locV == variant) {
                        variantLocated = true
                    }
                }
                line.lowercase().contains(":$module") -> moduleLocated = true
            }
            if (key != null && (!configuration.debug || configuration.debug && until != null)) {
                return key
            }
        }
        return ""
    }
}
