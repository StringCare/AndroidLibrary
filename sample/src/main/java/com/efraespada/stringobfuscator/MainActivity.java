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

        String hello = AndroidStringObfuscator.getString(R.string.app_name);

        ((TextView) findViewById(R.id.example)).setText(hello);
    }
}
