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
        String password = getString(R.string.snake);
        String original = SC.reveal(R.string.snake, Version.V2);

        String message = "Snake, the password is " + password + original;

        ((TextView) findViewById(R.id.example_a)).setText(message);

        String numbers = getString(R.string.test_a, "hi", 3) + " is " + SC.reveal(R.string.test_a, "hi", 3);
        ((TextView) findViewById(R.id.example_b)).setText(numbers);

        Log.e("test", String.valueOf(getString(R.string.snake2).equals(SC.reveal(R.string.snake))));

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

    }
}
