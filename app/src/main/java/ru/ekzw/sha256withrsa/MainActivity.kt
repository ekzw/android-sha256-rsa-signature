package ru.ekzw.sha256withrsa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ru.ekzw.sha256withrsa.ui.MainScreen
import ru.ekzw.sha256withrsa.ui.theme.SHA256withRSATheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SHA256withRSATheme {
                MainScreen()
            }
        }
    }
}