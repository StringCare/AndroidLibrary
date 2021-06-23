package com.stringcare.library

import android.content.Context
import android.text.Html
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.stringcare.library.SC.Companion.onContextReady
import com.stringcare.library.SC.Companion.reveal

/*
 * Credits to Narvelan:
 * https://github.com/StringCare/AndroidLibrary/issues/34
 */
class SCTextView : AppCompatTextView {
    private var text: String? = null
    private var isHTML: Boolean?
    private var androidTreatment: Boolean?
    private var revealed: Boolean?

    constructor(context: Context?) : super(context!!) {
        isHTML = null
        revealed = null
        androidTreatment = null
    }

    constructor(context: Context?, attrs: AttributeSet) : super(
        context!!, attrs
    ) {
        isHTML = null
        revealed = null
        androidTreatment = null
        loadText(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet, defStyleAttr: Int) : super(
        context!!, attrs, defStyleAttr
    ) {
        isHTML = null
        revealed = null
        androidTreatment = null
        loadText(attrs)
    }

    /**
     * Sets the initial parameters
     *
     * @param attrs {AttributeSet}
     */
    private fun loadText(attrs: AttributeSet) {
        val mFalse = "false"
        val mResourceAndroidSchema = "http://schemas.android.com/apk/res/android"
        val textName = "text"
        val mResourceSchema = "http://schemas.android.com/apk/res-auto"
        val htmlName = "htmlSupport"
        val revealValue = "reveal"
        val androidTreatmentName = "androidTreatment"
        text = attrs.getAttributeValue(mResourceAndroidSchema, textName)
        if (isHTML == null) {
            isHTML = !mFalse.equals(
                attrs.getAttributeValue(mResourceSchema, htmlName),
                ignoreCase = true
            )
        }
        if (revealed == null) {
            revealed = !mFalse.equals(
                attrs.getAttributeValue(mResourceSchema, revealValue),
                ignoreCase = true
            )
        }
        if (androidTreatment == null) {
            androidTreatment = !mFalse.equals(
                attrs.getAttributeValue(mResourceSchema, androidTreatmentName),
                ignoreCase = true
            )
        }
        reloadText()
    }

    /**
     * Prints text with the given conditions
     */
    private fun reloadText() {
        if (text != null) {
            try {
                val rawValue = text!!.substring(1).toInt()
                if (!isRevealingValue) {
                    setText(context.getString(rawValue))
                    return
                }
                onContextReady(object : ContextListener {
                    override fun contextReady() {
                        val value = reveal(rawValue, usesAndroidTreatment())
                        if (isHtmlEnabled) {
                            setText(Html.fromHtml(value))
                        } else {
                            setText(value)
                        }
                    }
                })
            } catch (e: NumberFormatException) {
                setText(text)
            }
        }
    }

    /**
     * Reveals the value before print it
     *
     * @param revealed {true|false}
     */
    fun setRevealed(revealed: Boolean) {
        this.revealed = revealed
        reloadText()
    }

    /**
     * Enables HTML printing
     *
     * @param enabled {true|false}
     */
    fun setHtmlSupport(enabled: Boolean) {
        isHTML = enabled
        reloadText()
    }

    /**
     * Enables the Android treatment
     *
     * @param enabled {true|false}
     */
    fun setAndroidTreatment(enabled: Boolean) {
        androidTreatment = enabled
        reloadText()
    }

    /**
     * Returns true if is the value must be print as HTML
     *
     * @return Boolean
     */
    val isHtmlEnabled: Boolean
        get() = java.lang.Boolean.TRUE == isHTML

    /**
     * Returns true if the value must be treated as the Android system does
     *
     * @return Boolean
     */
    fun usesAndroidTreatment(): Boolean {
        return java.lang.Boolean.TRUE == androidTreatment
    }

    /**
     * Returns true if the value should be setRevealed before print it
     *
     * @return Boolean
     */
    val isRevealingValue: Boolean
        get() = java.lang.Boolean.TRUE == revealed
}