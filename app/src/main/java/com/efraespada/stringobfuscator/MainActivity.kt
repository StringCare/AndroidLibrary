package com.efraespada.stringobfuscator

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.efraespada.stringobfuscator.ui.theme.StringObfuscatorSampleTheme
import dev.vyp.stringcare.library.SCTextView
import dev.vyp.stringcare.library.Version
import dev.vyp.stringcare.library.bytes
import dev.vyp.stringcare.library.json
import dev.vyp.stringcare.library.jsonArray
import dev.vyp.stringcare.library.reveal

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StringObfuscatorSampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    StringCareSampleScreen(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                    )
                }
            }
        }
    }
}

@Composable
private fun StringCareSampleScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val snakePassword = remember { context.getString(R.string.snake_msg_hidden) }
    val snakeOriginal = remember {
        runCatching { dev.vyp.stringcare.library.SC.reveal(snakePassword, Version.V3) }
            .getOrDefault("error")
    }
    val programmaticMessage =
        remember {
            "Snake, the password is $snakePassword$snakeOriginal"
        }

    val patternDemo =
        remember {
            context.getString(R.string.pattern, "hi", 3) +
                " is " +
                runCatching { R.string.pattern.reveal("hi", 3) }.getOrDefault("error")
        }

    val sameHello =
        remember {
            runCatching { R.string.hello_world_b.reveal() == context.getString(R.string.hello_world_a) }
                .getOrDefault(false)
        }

    val jsonObjectPath = remember { runCatching { R.string.asset_json_file.reveal() }.getOrDefault("") }
    val jsonObjectPretty =
        remember(jsonObjectPath) {
            runCatching { jsonObjectPath.json().toString(2) }.getOrDefault("error")
        }
    val jsonObjectRaw =
        remember(jsonObjectPath) {
            runCatching { String(jsonObjectPath.bytes { false }) }.getOrDefault("error")
        }

    val jsonArrayPath =
        remember { runCatching { R.string.asset_json_raw_file.reveal() }.getOrDefault("") }
    val jsonArrayPretty =
        remember(jsonArrayPath) {
            runCatching { jsonArrayPath.jsonArray().toString(2) }.getOrDefault("error")
        }
    val jsonArrayRaw =
        remember(jsonArrayPath) {
            runCatching { jsonArrayPath.bytes { false }.toString() }.getOrDefault("error")
        }

    val helloTvHolder = remember { mutableStateOf<SCTextView?>(null) }

    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = context.getString(R.string.app_name),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "StringCare sample (Compose): reveals, patterns, assets, and SCTextView.",
            style = MaterialTheme.typography.bodyMedium,
        )

        DemoCard(title = context.getString(R.string.html_treatment), color = Color(0xFFE3F2FD)) {
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { ctx ->
                    LayoutInflater.from(ctx).inflate(R.layout.sc_text_hello, null, false) as SCTextView
                },
                update = { tv -> helloTvHolder.value = tv },
            )
            TextButton(
                onClick = {
                    val tvAuto = helloTvHolder.value ?: return@TextButton
                    if (tvAuto.isHtmlEnabled) {
                        tvAuto.setHtmlSupport(!tvAuto.isHtmlEnabled)
                    } else if (tvAuto.isRevealingValue) {
                        tvAuto.setRevealed(!tvAuto.isRevealingValue)
                    } else if (!tvAuto.isRevealingValue) {
                        tvAuto.setRevealed(!tvAuto.isRevealingValue)
                        tvAuto.setHtmlSupport(!tvAuto.isHtmlEnabled)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = "Change")
            }
        }

        DemoCard(
            title = context.getString(R.string.programmatically_obfuscation),
            color = Color(0xFFFFF3E0),
        ) {
            DemoRow(label = "Message", value = programmaticMessage)
        }

        DemoCard(title = context.getString(R.string.patterns), color = Color(0xFFF3E5F5)) {
            DemoRow(label = "Pattern demo", value = patternDemo)
        }

        DemoCard(title = context.getString(R.string.long_new_line_comparison), color = Color(0xFFE8F5E9)) {
            DemoRow(label = "Same result", value = sameHello.toString())
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Text(
                text = context.getString(R.string.hello_world_a),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { ctx ->
                    LayoutInflater.from(ctx).inflate(R.layout.sc_text_hello_world_b, null, false) as SCTextView
                },
            )
        }

        DemoCard(title = context.getString(R.string.string_resource_disabling_android_treatment), color = Color(0xFFFFEBEE)) {
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { ctx ->
                    LayoutInflater.from(ctx).inflate(R.layout.sc_text_hello_world_c, null, false) as SCTextView
                },
            )
        }

        DemoCard(title = context.getString(R.string.json_object_asset), color = Color(0xFFE0F7FA)) {
            DemoRow(label = "Path (revealed)", value = jsonObjectPath)
            HorizontalDivider(Modifier.padding(vertical = 6.dp))
            DemoRow(label = "JSON", value = jsonObjectPretty)
            DemoRow(label = "Raw bytes as String", value = jsonObjectRaw)
        }

        DemoCard(title = context.getString(R.string.json_array_asset), color = Color(0xFFF1F8E9)) {
            DemoRow(label = "Path (revealed)", value = jsonArrayPath)
            HorizontalDivider(Modifier.padding(vertical = 6.dp))
            DemoRow(label = "JSONArray", value = jsonArrayPretty)
            DemoRow(label = "Raw bytes", value = jsonArrayRaw)
        }
    }
}

@Composable
private fun DemoCard(
    title: String,
    color: Color,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            content()
        }
    }
}

@Composable
private fun DemoRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "$label:",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = value,
            modifier = Modifier.weight(2f),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
