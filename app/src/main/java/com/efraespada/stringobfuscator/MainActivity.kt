package com.efraespada.stringobfuscator

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.stringcare.library.*
import com.stringcare.library.SC.Companion.init
import com.stringcare.library.SC.Companion.reveal

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init(applicationContext)
        val password = getString(R.string.snake_msg_hidden)
        val original = reveal(password, Version.V3)
        val message = "Snake, the password is $password$original"
        (findViewById<View>(R.id.programmatically_obfuscation) as TextView).text = message
        val numbers =
            getString(R.string.pattern, "hi", 3) + " is " + reveal(R.string.pattern, "hi", 3)
        (findViewById<View>(R.id.pattern) as TextView).text = numbers
        val tvAuto = findViewById<SCTextView>(R.id.auto_tv)
        findViewById<View>(R.id.btn_change).setOnClickListener { v: View? ->
            if (tvAuto.isHtmlEnabled) {
                tvAuto.setHtmlSupport(!tvAuto.isHtmlEnabled)
            } else if (tvAuto.isRevealingValue) {
                tvAuto.setRevealed(!tvAuto.isRevealingValue)
            } else if (!tvAuto.isRevealingValue) {
                tvAuto.setRevealed(!tvAuto.isRevealingValue)
                tvAuto.setHtmlSupport(!tvAuto.isHtmlEnabled)
            }
        }
        val equals = reveal(R.string.hello_world_b) == getString(R.string.hello_world_a)
        val areEquals = "Same result: $equals"
        (findViewById<View>(R.id.same_value) as TextView).text = areEquals
        val jsonObjectName = R.string.asset_json_file.reveal()


        findViewById<TextView>(R.id.json_object).text = jsonObjectName.json().toString()
        findViewById<TextView>(R.id.json_object_original).text =
            String(jsonObjectName.bytes { false })


        val jsonArrayName = R.string.asset_json_raw_file.reveal()
        findViewById<TextView>(R.id.json_array).text = jsonArrayName.jsonArray().toString()
        findViewById<TextView>(R.id.json_array_original).text =
            jsonArrayName.bytes { false }.toString()

    }
}