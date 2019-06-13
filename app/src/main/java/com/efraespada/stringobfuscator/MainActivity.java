package com.efraespada.stringobfuscator;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.stringcare.library.SC;
import com.stringcare.library.SCTextView;
import com.stringcare.library.Version;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SC.init(getApplicationContext());

        String password = getString(R.string.snake_msg_hidden);
        String original = SC.reveal(password, Version.V3);

        String message = "Snake, the password is " + password + original;

        ((TextView) findViewById(R.id.programmatically_obfuscation)).setText(message);

        String numbers = getString(R.string.pattern, "hi", 3) + " is " + SC.reveal(R.string.pattern, "hi", 3);
        ((TextView) findViewById(R.id.pattern)).setText(numbers);

        final SCTextView tvAuto = findViewById(R.id.auto_tv);
        findViewById(R.id.btn_change).setOnClickListener(v -> {
            if (tvAuto.isHtmlEnabled()) {
                tvAuto.htmlEnabled(!tvAuto.isHtmlEnabled());
            } else if (tvAuto.isVisible()){
                tvAuto.visible(!tvAuto.isVisible());
            } else if (!tvAuto.isVisible()){
                tvAuto.visible(!tvAuto.isVisible());
                tvAuto.htmlEnabled(!tvAuto.isHtmlEnabled());
            }
        });

        boolean equals = SC.reveal(R.string.c3po).equals(getString(R.string.c3po_));
        String areEquals = "Same result: " +  equals;
        ((TextView) findViewById(R.id.same_value)).setText(areEquals);
    }
}
