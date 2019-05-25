package com.efraespada.stringobfuscator;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.stringcare.library.SC;
import com.stringcare.library.SCTextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SC.init(getApplicationContext());

        // secret var
        String password = "lalilulelo";

        String message = "Snake, the password is " + SC.obfuscate(password)
            + " (" + SC.reveal(SC.obfuscate(password)) + ")";

        ((TextView) findViewById(R.id.example_a)).setText(Html.fromHtml(message));

        /*
        String numbers = getString(R.string.test_a, "hi", 3) + " is " + SC.reveal(R.string.test_a, "hi", 3);
        ((TextView) findViewById(R.id.example_b)).setText(numbers);
        final SCTextView tvAuto = findViewById(R.id.auto_tv);
        findViewById(R.id.btn_change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tvAuto.isHtmlEnabled()) {
                    tvAuto.htmlEnabled(!tvAuto.isHtmlEnabled());
                } else if (tvAuto.isVisible()){
                    tvAuto.visible(!tvAuto.isVisible());
                } else if (!tvAuto.isVisible()){
                    tvAuto.visible(!tvAuto.isVisible());
                    tvAuto.htmlEnabled(!tvAuto.isHtmlEnabled());
                }
            }
        });

         */

    }
}
