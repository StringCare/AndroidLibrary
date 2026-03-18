package dev.vyp.stringcare.plugin.internal

import dev.vyp.stringcare.plugin.StringCareConfiguration

private class Fingerprint {

    companion object {
        private var key: String? = null
        private var until: String? = null
        private var error: String? = null
        private var variantLocated = false
        private var moduleLocated = false
    }

    fun extract(module: String, variant: String, configuration: StringCareConfiguration, trace: String): String {
        val lines = trace.split("\n")
        lines.forEach { line ->
            when {
                line.lowercase().contains("downloading") -> if (configuration.debug) {
                    PrintUtils.print(module, line, configuration.debug)
                }
                line.lowercase().contains("unzipping") -> if (configuration.debug) {
                    PrintUtils.print(module, line, configuration.debug)
                }
                line.lowercase().contains("permissions") -> if (configuration.debug) {
                    PrintUtils.print(module, line, configuration.debug)
                }
                line.lowercase().contains("config:") && moduleLocated && variantLocated -> {
                    val k = (line.split(": ").getOrNull(1) ?: "").trim()
                    val valid = !k.equals("none", ignoreCase = true)
                    if (!valid) {
                        key = k
                        PrintUtils.print(module, "\uD83E\uDD2F no config defined for variant $variant", true)
                        if (configuration.debug) {
                            until = key
                        }
                    } else if (configuration.debug) {
                        PrintUtils.print(module, "Module: $module", true)
                        PrintUtils.print(module, "Variant: $variant", true)
                    }

                }
                line.lowercase().contains("sha1") && moduleLocated && variantLocated -> {
                    key = line.split(" ").getOrNull(1) ?: ""
                    if (configuration.debug) {
                        PrintUtils.print(module, line, configuration.debug)
                    }
                }
                line.lowercase().contains("error") -> {
                    error = line.split(": ").getOrNull(1)
                }
                line.lowercase().contains("valid until") && moduleLocated && variantLocated -> {
                    until = line.split(": ").getOrNull(1)
                    if (configuration.debug) {
                        PrintUtils.print(module, line, configuration.debug)
                    }
                }
                line.lowercase().contains("store") && moduleLocated && variantLocated -> if (configuration.debug) {
                    PrintUtils.print(module, line, configuration.debug)
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
                return key!!
            }
        }
        return ""
    }
}

fun fingerPrint(
    module: String,
    variant: String,
    configuration: StringCareConfiguration,
    keyFound: (key: String) -> Unit
) {
    if (configuration.mockedFingerprint.isNotEmpty()) {
        keyFound(configuration.mockedFingerprint)
        return
    }
    signingReportTask().runCommand { _, report ->
        keyFound(report.extractFingerprint(module, variant, configuration))
    }
}

fun String.extractFingerprint(module: String = "app", variant: String = "debug", configuration: StringCareConfiguration): String {
    return Fingerprint().extract(module, variant, configuration, this)
}
