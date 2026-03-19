package dev.vyp.stringcare.plugin.internal

import org.w3c.dom.Node

fun Node.extractHtml(): String {
    val stringBuilder = StringBuilder()
    for (i in 0 until this.childNodes.length) {
        val item = this.childNodes.item(i)
        val type = item.getType()
        when (type) {
            StringType.BR ->
                stringBuilder.append(
                    "<br>${when {
                        item.textContent.isNotEmpty() -> item.extractHtml()
                        else -> ""
                    }
                    }</br>",
                )
            StringType.I ->
                stringBuilder.append(
                    "<i>${when {
                        item.textContent.isNotEmpty() -> item.extractHtml()
                        else -> ""
                    }
                    }</i>",
                )
            StringType.STRONG ->
                stringBuilder.append(
                    "<strong>${when {
                        item.textContent.isNotEmpty() -> item.extractHtml()
                        else -> ""
                    }
                    }</strong>",
                )
            StringType.TEXT -> stringBuilder.append(item.textContent)
        }
    }
    return stringBuilder.toString()
}

enum class StringType {
    BR,
    TEXT,
    I,
    STRONG,
}

fun Node.getType(): StringType =
    when {
        this.toString().contains("[br:") -> StringType.BR
        this.toString().contains("[i:") -> StringType.I
        this.toString().contains("[strong:") -> StringType.STRONG
        this.toString().contains("[#text") -> StringType.TEXT
        else -> StringType.TEXT
    }
