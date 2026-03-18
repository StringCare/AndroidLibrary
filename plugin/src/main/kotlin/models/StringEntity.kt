package models

open class StringEntity(
    var name: String,
    var attributes: List<SAttribute>,
    var value: String,
    val tag: String,
    val index: Int,
    val androidTreatment: Boolean = true
)

data class SAttribute(val name: String, val value: String)