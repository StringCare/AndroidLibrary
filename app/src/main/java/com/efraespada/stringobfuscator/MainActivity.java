package com.efraespada.stringobfuscator;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.widget.TextView;

import com.stringcare.library.SC;
import com.stringcare.library.SCTextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SC.init(getApplicationContext());
        // SC.initForLib(getApplicationContext(), this);

        int stringId = R.string.hello;

        String message = getString(stringId);
        message += " is ";
        message += SC.deobfuscate(stringId);

        // secret var
        String password = "lalilulelo";

        message += "\n\nFor Metal Gear lovers:\n\n\"Snake, the password is " + SC.obfuscate(password)
            + "\n\n.. or " + SC.deobfuscate(SC.obfuscate(password)) + "\"";

        ((TextView) findViewById(R.id.example_a)).setText(Html.fromHtml(message));

        String numbers = getString(R.string.test_a, "hi", 3) + " is " + SC.deobfuscate(R.string.test_a, "hi", 3);
        ((TextView) findViewById(R.id.example_b)).setText(numbers);
        ((SCTextView) findViewById(R.id.auto_tv)).visible(false);

    }
}
