package com.efraespada.stringobfuscator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.stringcare.library.SC;

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
        message += SC.getString(stringId);

        // secret var
        String mySecret = "lalilulelo";

        message += "\n\nFor Metal Gear lovers:\n\n\"Snake, the password is " + SC.encryptString(message)
            + "\n\n.. or " + SC.decryptString(SC.encryptString(mySecret)) + "\"";

        ((TextView) findViewById(R.id.example_a)).setText(message);

        String numbers = getString(R.string.test_a, "hi", 3) + " is " + SC.getString(R.string.test_a, "hi", 3);
        ((TextView) findViewById(R.id.example_b)).setText(numbers);

    }
}
