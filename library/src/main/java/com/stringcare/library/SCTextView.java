package com.stringcare.library;

import android.content.Context;
import androidx.appcompat.widget.AppCompatTextView;
import android.text.Html;
import android.util.AttributeSet;

/*
 * Credits to Narvelan:
 * https://github.com/StringCare/AndroidLibrary/issues/34
 */

public class SCTextView extends AppCompatTextView {

    private String text;
    private Boolean isHTML;
    private Boolean androidTreatment;
    private Boolean revealed;

    public SCTextView(Context context) {
        super(context);
        isHTML = null;
        revealed = null;
        androidTreatment = null;
    }

    public SCTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        isHTML = null;
        revealed = null;
        androidTreatment = null;
        loadText(attrs);
    }

    public SCTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        isHTML = null;
        revealed = null;
        androidTreatment = null;
        loadText(attrs);
    }

    /**
     * Sets the initial parameters
     *
     * @param attrs {AttributeSet}
     */
    private void loadText(final AttributeSet attrs) {
        String mFalse = "false";

        String mResourceAndroidSchema = "http://schemas.android.com/apk/res/android";
        String textName = "text";

        String mResourceSchema = "http://schemas.android.com/apk/res-auto";
        String htmlName = "htmlSupport";
        String revealValue = "reveal";
        String androidTreatmentName = "androidTreatment";

        text = attrs.getAttributeValue(mResourceAndroidSchema, textName);
        if (isHTML == null) {
            isHTML = !mFalse.equalsIgnoreCase(attrs.getAttributeValue(mResourceSchema, htmlName));
        }
        if (revealed == null) {
            revealed = !mFalse.equalsIgnoreCase(attrs.getAttributeValue(mResourceSchema, revealValue));
        }
        if (androidTreatment == null) {
            androidTreatment = !mFalse.equalsIgnoreCase(attrs.getAttributeValue(mResourceSchema, androidTreatmentName));
        }

        reloadText();
    }

    /**
     * Prints text with the given conditions
     */
    private void reloadText() {
        if (text != null) {
            try {
                final int val = Integer.parseInt(text.substring(1));
                if (!isRevealingValue()) {
                    setText(getContext().getString(val));
                    return;
                }
                SC.onContextReady(new ContextListener() {
                    @Override
                    public void contextReady() {
                        String value = SC.reveal(val, usesAndroidTreatment());
                        if (isHtmlEnabled()) {
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
     * Reveals the value before print it
     *
     * @param revealed {true|false}
     */
    public void setRevealed(boolean revealed) {
        this.revealed = revealed;
        reloadText();
    }

    /**
     * Enables HTML printing
     *
     * @param enabled {true|false}
     */
    public void setHtmlSupport(boolean enabled) {
        isHTML = enabled;
        reloadText();
    }

    /**
     * Enables the Android treatment
     *
     * @param enabled {true|false}
     */
    public void setAndroidTreatment(boolean enabled) {
        androidTreatment = enabled;
        reloadText();
    }

    /**
     * Returns true if is the value must be print as HTML
     *
     * @return Boolean
     */
    public boolean isHtmlEnabled() {
        return Boolean.TRUE.equals(isHTML);
    }

    /**
     * Returns true if the value must be treated as the Android system does
     *
     * @return Boolean
     */
    public boolean usesAndroidTreatment() {
        return Boolean.TRUE.equals(androidTreatment);
    }

    /**
     * Returns true if the value should be setRevealed before print it
     *
     * @return Boolean
     */
    public boolean isRevealingValue() {
        return Boolean.TRUE.equals(revealed);
    }

}
