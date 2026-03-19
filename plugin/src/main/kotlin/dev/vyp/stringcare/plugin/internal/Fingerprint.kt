package dev.vyp.stringcare.plugin.internal

import dev.vyp.stringcare.plugin.StringCareConfiguration
import dev.vyp.stringcare.plugin.domain.usecases.ExtractFingerprintUseCase

fun fingerPrint(
    module: String,
    variant: String,
    configuration: StringCareConfiguration,
    keyFound: (key: String) -> Unit,
) {
    if (configuration.mockedFingerprint.isNotEmpty()) {
        keyFound(configuration.mockedFingerprint)
        return
    }
    signingReportTask().runCommand { _, report ->
        keyFound(report.extractFingerprint(module, variant, configuration))
    }
}

fun String.extractFingerprint(
    module: String = "app",
    variant: String = "debug",
    configuration: StringCareConfiguration,
): String =
    ExtractFingerprintUseCase.extract(
        reportText = this,
        module = module,
        variant = variant,
        configuration = configuration,
    ) { m, message, tab -> PrintUtils.print(m, message, tab) }
