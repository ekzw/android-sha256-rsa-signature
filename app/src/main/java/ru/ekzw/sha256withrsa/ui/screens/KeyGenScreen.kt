package ru.ekzw.sha256withrsa.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.ekzw.sha256withrsa.ui.components.KeyCard
import ru.ekzw.sha256withrsa.ui.components.MetricsCard
import ru.ekzw.sha256withrsa.viewmodel.MainViewModel

@Composable
fun KeyGenScreen(viewModel: MainViewModel) {
    var selectedKeySize by remember { mutableIntStateOf(2048) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Генерация RSA",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Text("Выберите длину ключа", style = MaterialTheme.typography.labelLarge)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val is1024 = selectedKeySize == 1024
            Button(
                onClick = { selectedKeySize = 1024 },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(
                    topStartPercent = 50,
                    bottomStartPercent = 50,
                    topEndPercent = 12,
                    bottomEndPercent = 12
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (is1024) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = if (is1024) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(
                    if (is1024) Icons.Filled.Lock else Icons.Outlined.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("1024 bit")
            }

            val is2048 = selectedKeySize == 2048
            Button(
                onClick = { selectedKeySize = 2048 },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(
                    topStartPercent = 12,
                    bottomStartPercent = 12,
                    topEndPercent = 50,
                    bottomEndPercent = 50
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (is2048) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = if (is2048) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(
                    if (is2048) Icons.Filled.Lock else Icons.Outlined.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("2048 bit")
            }
        }

        Button(
            shapes = ButtonDefaults.shapes(),
            onClick = { viewModel.generateKeys(selectedKeySize) },
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp)
        ) {
            Text("Сгенерировать ключи")
        }

        AnimatedVisibility(visible = viewModel.publicKeyStr.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                MetricsCard("Метрики операции", viewModel.keyMetrics)
                KeyCard("Public Key", viewModel.publicKeyStr)
                KeyCard("Private Key", viewModel.privateKeyStr)
            }
        }

        Spacer(modifier = Modifier.height(120.dp))
    }
}