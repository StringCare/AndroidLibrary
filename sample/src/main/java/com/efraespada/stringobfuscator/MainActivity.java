package com.efraespada.stringobfuscator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.efraespada.androidstringobfuscator.AndroidStringObfuscator;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AndroidStringObfuscator.init(this);

        int stringId = R.string.hello;

        String message = getString(stringId);
        message += " is ";
        message += AndroidStringObfuscator.getString(stringId);

        // secret
        String mySecret = "lalilulelo";

        message += "\n \n For Metal Gear lovers: Snake, the pass is " + AndroidStringObfuscator.simulateString(message)
            + " or " + mySecret;

        ((TextView) findViewById(R.id.example)).setText(message);
    }
}
