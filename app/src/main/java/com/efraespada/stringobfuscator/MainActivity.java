package com.efraespada.stringobfuscator;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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

        // 108, 97, 108, 105, 108, 117, 108, 101, 108, 111, 10, 104, 111, 108, 105 works
        // 108, 97, 108, 105, 108, 117, 108, 101, 108, 111, 92, 110, 104, 111, 108, 105 not works

        // secret var
        String password = getString(R.string.snake_msg_hidden);
        String original = SC.reveal(password, Version.V3);

        String message = "Snake, the password is " + password + original;

        ((TextView) findViewById(R.id.programmatically_obfuscation)).setText(message);

        String numbers = getString(R.string.test_a, "hi", 3) + " is " + SC.reveal(R.string.test_a, "hi", 3);
        ((TextView) findViewById(R.id.pattern)).setText(numbers);

        Log.e("test", String.valueOf(getString(R.string.snake_msg_original).equals(SC.reveal(R.string.snake_msg_hidden))));

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

        ((TextView) findViewById(R.id.same_value)).setText(
                "same keys?" + SC.reveal(R.string.c3po).equals(getString(R.string.c3po_)));
    }
}
