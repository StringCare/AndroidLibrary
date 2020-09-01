package com.efraespada.stringobfuscator;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
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
                tvAuto.setHtmlSupport(!tvAuto.isHtmlEnabled());
            } else if (tvAuto.isRevealingValue()) {
                tvAuto.setRevealed(!tvAuto.isRevealingValue());
            } else if (!tvAuto.isRevealingValue()) {
                tvAuto.setRevealed(!tvAuto.isRevealingValue());
                tvAuto.setHtmlSupport(!tvAuto.isHtmlEnabled());
            }
        });

        boolean equals = SC.reveal(R.string.hello_world_b).equals(getString(R.string.hello_world_a));
        String areEquals = "Same result: " + equals;
        ((TextView) findViewById(R.id.same_value)).setText(areEquals);

        String jsonObjectName = SC.reveal(R.string.asset_json_file);
        SC.asset().asyncJson(jsonObjectName, json -> ((TextView) findViewById(R.id.json_object)).setText(json.toString()));
        SC.asset().asyncBytes(jsonObjectName, bytes -> ((TextView) findViewById(R.id.json_object_original)).setText(new String(bytes)), false);

        String jsonArrayName = SC.reveal(R.string.asset_json_raw_file);
        SC.asset().asyncJsonArray(jsonArrayName, json -> ((TextView) findViewById(R.id.json_array)).setText(json.toString()));
        SC.asset().asyncBytes(jsonArrayName, bytes -> ((TextView) findViewById(R.id.json_array_original)).setText(new String(bytes)), false);

    }
}
