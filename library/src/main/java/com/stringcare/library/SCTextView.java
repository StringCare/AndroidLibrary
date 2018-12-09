package com.stringcare.library;

/**
 * Credits to Narvelan:
 * https://github.com/StringCare/AndroidLibrary/issues/34
 */

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

public class SCTextView extends AppCompatTextView {

    public SCTextView(Context context) {
        super(context);
    }

    public SCTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadText(attrs);
    }

    public SCTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        loadText(attrs);
    }

    private void loadText(final AttributeSet attrs) {
        String text = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "text");
        if (text != null) {
            try {
                final Integer val = Integer.parseInt(text.substring(1));
                SC.onContextReady(new ContextListener() {
                    @Override
                    public void contextReady() {
                        setText(SC.deobfuscate(val));
                    }
                });
            } catch (NumberFormatException e) {
                setText(text);
            }
        }
    }

}
