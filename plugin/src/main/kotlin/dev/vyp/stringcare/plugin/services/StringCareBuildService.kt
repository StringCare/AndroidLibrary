package dev.vyp.stringcare.plugin.services

import dev.vyp.stringcare.plugin.StringCareConfiguration
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import java.util.concurrent.ConcurrentHashMap

/**
 * Per-build shared state for StringCare (paths, variant applicationIds, resolved DSL config).
 */
abstract class StringCareBuildService : BuildService<StringCareBuildService.Params> {
    interface Params : BuildServiceParameters {
        val absoluteProjectPath: Property<String>
    }

    private val variantApplicationIds = ConcurrentHashMap<String, String>()

    @Volatile
    private var resolvedConfiguration: StringCareConfiguration? = null

    fun absoluteProjectPath(): String = parameters.absoluteProjectPath.get()

    fun putVariantApplicationId(
        variantName: String,
        applicationId: String,
    ) {
        variantApplicationIds[variantName] = applicationId
    }

    fun firstVariantKeyOrDefault(default: String): String = variantApplicationIds.keys.firstOrNull() ?: default

    fun setResolvedConfiguration(config: StringCareConfiguration) {
        resolvedConfiguration = config
    }

    fun getResolvedConfiguration(): StringCareConfiguration =
        resolvedConfiguration ?: error("StringCare configuration not initialized")

    fun variantApplicationIdsView(): Map<String, String> = variantApplicationIds.toMap()
}
