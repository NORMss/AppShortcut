package com.norm.myappshortcut

import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.norm.myappshortcut.ShortcutType.DYNAMIC
import com.norm.myappshortcut.ShortcutType.PINNED
import com.norm.myappshortcut.ShortcutType.STATIC
import com.norm.myappshortcut.ui.theme.MyAppShortcutTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        setContent {
            MyAppShortcutTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(
                        16.dp, Alignment.CenterVertically
                    )
                ) {
                    when (viewModel.shortcutType) {
                        STATIC -> Text("Static shortcut clicked")
                        DYNAMIC -> Text("Dynamic shortcut clicked")
                        PINNED -> Text("Pined shortcut clicked")
                        null -> Unit
                    }
                    Button(
                        onClick = ::addDynamicShortcut
                    ) {
                        Text(
                            text = "Add dynamic shortcut"
                        )
                    }
                    Button(
                        onClick = ::addPinedShortcut
                    ) {
                        Text(
                            text = "Add pinned shortcut"
                        )
                    }

                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleIntent(it) }
    }

    private fun addPinedShortcut() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val shortcutManager = getSystemService<ShortcutManager>()!!
        if (shortcutManager.isRequestPinShortcutSupported) {
            val shortcut = ShortcutInfo.Builder(applicationContext, "pinned")
                .setShortLabel("Send message")
                .setLongLabel("This sends a massage to a friend")
                .setIcon(
                    Icon.createWithResource(
                        applicationContext, R.drawable.baseline_timer_off_24
                    )
                )
                .setIntent(
                    Intent(applicationContext, MainActivity::class.java).apply {
                        action = Intent.ACTION_VIEW
                        putExtra("shortcut_id", "pinned")
                    }
                )
                .build()

            val callbackIntent = shortcutManager.createShortcutResultIntent(shortcut)
            val successPendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                0,
                callbackIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
            shortcutManager.requestPinShortcut(shortcut, successPendingIntent.intentSender)
        }
    }

    private fun addDynamicShortcut() {
        val shortcut = ShortcutInfoCompat.Builder(applicationContext, "dynamic")
            .setShortLabel("Start Timer")
            .setLongLabel("Clicking this will start timer")
            .setIcon(
                IconCompat.createWithResource(
                    applicationContext, R.drawable.baseline_timer_24
                )
            )
            .setIntent(
                Intent(applicationContext, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    putExtra("shortcut_id", "dynamic")
                }
            )
            .build()
        ShortcutManagerCompat.pushDynamicShortcut(applicationContext, shortcut)
    }

    private fun handleIntent(intent: Intent) {
        when (intent.getStringExtra("shortcut_id")) {
            "static" -> viewModel.onShortcutClicked(STATIC)
            "dynamic" -> viewModel.onShortcutClicked(DYNAMIC)
            "pinned" -> viewModel.onShortcutClicked(PINNED)
        }
    }
}