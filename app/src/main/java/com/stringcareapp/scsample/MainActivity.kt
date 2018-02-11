package com.stringcareapp.scsample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.stringcare.library.SC
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        SC.initOnModule(applicationContext, BuildConfig::class.java)

        val stringId = R.string.hello

        var message = getString(stringId)
        message += " is "
        message += SC.getString(stringId)

        // secret var
        val mySecret = "lalilulelo"

        message += ("\n\nFor Metal Gear lovers:\n\n\"Snake, the password is " + SC.encryptString(message)
                + "\n\n.. or " + SC.decryptString(SC.encryptString(mySecret)) + "\"")

        example_a.text = message
    }

}
