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

        int stringId = R.string.hello;

        String message = getString(stringId);
        message += " is ";
        message += SC.getString(stringId);

        // secret
        String mySecret = "lalilulelo";

        message += "\n\nFor Metal Gear lovers:\n\n\"Snake, the password is " + SC.encryptString(message)
            + "\n\n.. or " + SC.decryptString(SC.encryptString(mySecret)) + "\"";

        ((TextView) findViewById(R.id.example)).setText(message);
    }
}
