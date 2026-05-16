package ru.ekzw.sha256withrsa.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.ekzw.sha256withrsa.viewmodel.MainViewModel

@Composable
fun VerifyScreen(viewModel: MainViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Проверка подписи v1",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Схема v1 проверяет цифровую подпись каждого отдельного файла внутри APK. Если хотя бы один файл изменен, проверка завершится с ошибкой",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Button(
            shapes = ButtonDefaults.shapes(),
            onClick = { viewModel.verifyAllFiles() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = viewModel.keyPair != null && !viewModel.isProcessing,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Запустить проверку")
        }

        AnimatedVisibility(visible = viewModel.isProcessing) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularWavyProgressIndicator(modifier = Modifier.padding(8.dp))
                Text(
                    "Идет проверка ${viewModel.documents.size} файлов..",
                    style = MaterialTheme.typography.labelMedium
                )
                LinearWavyProgressIndicator(
                    progress = { viewModel.processingProgress },
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .padding(top = 8.dp)
                )
            }
        }

        AnimatedVisibility(visible = viewModel.massVerifyShow && !viewModel.isProcessing) {
            val hasErrors = viewModel.massVerifyResults.any { !it.isSuccess }

            val containerColor =
                if (hasErrors) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
            val contentColor =
                if (hasErrors) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = containerColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        if (hasErrors) "Несостыковка" else "Все хорошо",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )

                    if (viewModel.verifyMetrics.isNotEmpty()) {
                        Text(
                            viewModel.verifyMetrics,
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor.copy(alpha = 0.8f)
                        )
                    }

                    HorizontalDivider(color = contentColor.copy(alpha = 0.2f))

                    viewModel.massVerifyResults.forEach { res ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (res.isSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (res.isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = res.docName,
                                style = MaterialTheme.typography.bodySmall,
                                color = contentColor
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(120.dp))
    }
}