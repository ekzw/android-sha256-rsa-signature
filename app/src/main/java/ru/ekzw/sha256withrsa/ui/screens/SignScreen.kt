package ru.ekzw.sha256withrsa.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.InputChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.ekzw.sha256withrsa.ui.components.KeyCard
import ru.ekzw.sha256withrsa.ui.components.MetricsCard
import ru.ekzw.sha256withrsa.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignScreen(viewModel: MainViewModel) {
    val currentDoc = viewModel.currentDoc

    if (viewModel.isProcessing) {
        BasicAlertDialog(
            onDismissRequest = { },
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(28.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 3.dp
                    )

                    Text(
                        text = viewModel.loadingMessage.ifBlank { "Обработка.." },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (viewModel.processingProgress > 0f) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = { viewModel.processingProgress },
                                modifier = Modifier.fillMaxWidth(),
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            )
                            Text(
                                text = "${(viewModel.processingProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Подпись файлов",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.simulateApkFiles() },
                modifier = Modifier.weight(1f),
                enabled = !viewModel.isProcessing
            ) {
                Text("Создать 100 файлов")
            }
            Button(
                shapes = ButtonDefaults.shapes(),
                onClick = { viewModel.signAllFiles() },
                modifier = Modifier.weight(1f),
                enabled = viewModel.keyPair != null && !viewModel.isProcessing
            ) {
                Text("Подписать все")
            }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(viewModel.documents) { doc ->
                InputChip(
                    selected = viewModel.selectedDocId == doc.id,
                    onClick = { if (!viewModel.isProcessing) viewModel.selectedDocId = doc.id },
                    label = {
                        Text(doc.name.substringAfterLast('/'))
                    },
                    trailingIcon = {
                        if (viewModel.documents.size > 1 && !viewModel.isProcessing) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Удалить",
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { viewModel.deleteFile(doc.id) }
                            )
                        }
                    }
                )
            }
            item {
                IconButton(
                    shapes = IconButtonDefaults.shapes(),
                    onClick = { viewModel.createNewFile() },
                    enabled = !viewModel.isProcessing
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить файл")
                }
            }
        }

        OutlinedTextField(
            value = currentDoc?.content ?: "",
            onValueChange = { viewModel.updateCurrentFileContent(it) },
            label = { Text("Содержимое файла") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 150.dp, max = 300.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = !viewModel.isProcessing
        )

        if (viewModel.keyPair == null) {
            Text(
                "Сначала сгенерируйте ключи на первой вкладке",
                color = MaterialTheme.colorScheme.error
            )
        }

        AnimatedVisibility(visible = viewModel.signAllMetrics.isNotEmpty()) {
            MetricsCard("Итого по всем файлам", viewModel.signAllMetrics)
        }

        AnimatedVisibility(visible = currentDoc?.signatureResult?.isNotEmpty() == true) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                currentDoc?.let {
                    MetricsCard("Метрики операции", it.signMetrics)
                    KeyCard("SHA-256", it.hashResult)
                    KeyCard("Base64", it.signatureResult)
                }
            }
        }

        Spacer(modifier = Modifier.height(120.dp))
    }
}