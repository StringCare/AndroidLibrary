package dev.vyp.stringcare.plugin.domain.models

/**
 * A `<string>` resource entry marked for obfuscation (`hidden` not false).
 *
 * Example:
 * `StringEntity("welcome_title", attributes, "Welcome", "string", 0, androidTreatment = true)`
 *
 * @param name XML `name` attribute.
 * @param attributes Original XML attributes copied from the source node.
 * @param value Current string value (plain text or obfuscated payload).
 * @param tag XML tag name, usually `string`.
 * @param index DOM order index among all `<string>` nodes (used when rewriting XML).
 * @param androidTreatment Whether Android-style whitespace treatment should be applied before obfuscation.
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
