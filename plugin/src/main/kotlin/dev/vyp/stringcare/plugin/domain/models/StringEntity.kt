package dev.vyp.stringcare.plugin.domain.models

/**
 * A `<string>` resource entry marked for obfuscation (`hidden` not false).
 *
 * @param index DOM order index among all `<string>` nodes (used when rewriting XML).
 */
data class StringEntity(
    val name: String,
    val attributes: List<SAttribute>,
    val value: String,
    val tag: String,
    val index: Int,
    val androidTreatment: Boolean = true,
) {
    init {
        require(name.isNotBlank()) { "StringEntity.name must not be blank" }
        require(index >= 0) { "StringEntity.index must be >= 0" }
    }

    /** Returns a copy with an updated obfuscated or plain value. */
    fun withValue(newValue: String): StringEntity = copy(value = newValue)
}
