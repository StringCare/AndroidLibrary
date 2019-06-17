package com.stringcare.library;

/*
 * Credits to Narvelan:
 * https://github.com/StringCare/AndroidLibrary/issues/34
 */

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.text.Html;
import android.util.AttributeSet;

import java.util.Formatter;

public class SCTextView extends AppCompatTextView {

    private String text;
    private Boolean isHTML;
    private Boolean androidTreatment;
    private Boolean visible;

    public SCTextView(Context context) {
        super(context);
        isHTML = null;
        visible = null;
        androidTreatment = null;
    }

    public SCTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        isHTML = null;
        visible = null;
        androidTreatment = null;
        loadText(attrs);
    }

    public SCTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        isHTML = null;
        visible = null;
        androidTreatment = null;
        loadText(attrs);
    }

    /**
     * Defines initial vars
     *
     * @param attrs {AttributeSet}
     */
    private void loadText(final AttributeSet attrs) {
        text = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "text");
        if (isHTML == null) {
            isHTML = !"false".equalsIgnoreCase(attrs.getAttributeValue("http://schemas.android.com/apk/res-auto", "html"));
        }
        if (visible == null) {
            visible = !"false".equalsIgnoreCase(attrs.getAttributeValue("http://schemas.android.com/apk/res-auto", "visible"));
        }
        if (androidTreatment == null) {
            androidTreatment = !"false".equalsIgnoreCase(attrs.getAttributeValue("http://schemas.android.com/apk/res-auto", "androidTreatment"));
        }

        reloadText();
    }

    /**
     * Prints text with the given conditions
     */
    private void reloadText() {
        if (text != null) {
            try {
                final Integer val = Integer.parseInt(text.substring(1));
                if (!visible) {
                    setText(getContext().getString(val));
                    return;
                }
                SC.onContextReady(new ContextListener() {
                    @Override
                    public void contextReady() {
                        String value = SC.reveal(val, androidTreatment);
                        if (isHTML) {
                            setText(Html.fromHtml(value));
                        } else {
                            setText(value);
                        }
                    }
                });
            } catch (NumberFormatException e) {
                setText(text);
            }
        }
    }

    /**
     * Enables de-obfuscation before print the value
     *
     * @param visible {true|false}
     */
    public void visible(boolean visible) {
        this.visible = visible;
        reloadText();
    }

    /**
     * Enables HTML printing
     *
     * @param enabled {true|false}
     */
    public void htmlEnabled(boolean enabled) {
        isHTML = enabled;
        reloadText();
    }

    /**
     * Returns true if is the value must be print as HTML or plain text
     *
     * @return Boolean
     */
    public boolean isHtmlEnabled() {
        return isHTML;
    }

    /**
     * Returns true if is de-obfuscating the value before print it
     *
     * @return Boolean
     */
    public boolean isVisible() {
        return visible;
    }

    public String escapeUnicode(String input) {
        StringBuilder b = new StringBuilder(input.length());
        Formatter f = new Formatter(b);
        for (char c : input.toCharArray()) {
            if (c < 128) {
                b.append(c);
            } else {
                f.format("\\u%04x", (int) c);
            }
        }
        return b.toString();
    }

}
