package ru.ekzw.sha256withrsa.viewmodel

import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.ekzw.sha256withrsa.model.CryptoManager
import ru.ekzw.sha256withrsa.model.Document
import ru.ekzw.sha256withrsa.model.KeyPair
import ru.ekzw.sha256withrsa.model.MassVerifyResult
import ru.ekzw.sha256withrsa.model.RSA
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class MainViewModel : ViewModel() {
    private val df = DecimalFormat("0.000000", DecimalFormatSymbols(Locale.US))

    var keyPair: KeyPair? = null
        private set
    var publicKeyStr by mutableStateOf("")
        private set
    var privateKeyStr by mutableStateOf("")
        private set
    var keyMetrics by mutableStateOf("")
        private set

    var signAllMetrics by mutableStateOf("")
        private set

    val documents = mutableStateListOf(
        Document(1, "META-INF/MANIFEST.MF", "Name: classes.dex\nSHA-256-Digest: /pG7yO+1qL3z...=")
    )
    var selectedDocId by mutableIntStateOf(1)
    val currentDoc: Document? get() = documents.find { it.id == selectedDocId }

    var isProcessing by mutableStateOf(false)
        private set
    var processingProgress by mutableFloatStateOf(0f)
        private set
    var loadingMessage by mutableStateOf("")
        private set

    fun createNewFile() {
        if (isProcessing) return
        val newId = (documents.maxOfOrNull { it.id } ?: 0) + 1
        documents.add(Document(newId, "Файл $newId", "Текст нового файла.."))
        selectedDocId = newId
    }

    fun deleteFile(id: Int) {
        if (isProcessing) return
        if (documents.size <= 1) return
        val index = documents.indexOfFirst { it.id == id }
        if (index != -1) {
            documents.removeAt(index)
            if (selectedDocId == id) selectedDocId = documents.first().id
        }
    }

    fun updateCurrentFileContent(newText: String) {
        if (isProcessing) return
        val index = documents.indexOfFirst { it.id == selectedDocId }
        if (index != -1) {
            documents[index] = documents[index].copy(content = newText)
        }
    }

    private fun buildContent(name: String, sizeKB: Int): String {
        val target = sizeKB * 1024
        val header = "// file: $name | size: ~$sizeKB KB\n"
        if (header.length >= target) return header

        val seed = name.hashCode().toLong() and 0xFFFFFFFFL
        val block = buildString(256) {
            var x = seed
            repeat(32) {
                x = x * 6364136223846793005L + 1442695040888963407L
                append(x.toULong().toString(16).padStart(16, '0'))
            }
            append('\n')
        }

        return buildString(target) {
            append(header)
            while (length < target) append(block)
        }.substring(0, target)
    }

    fun simulateApkFiles() {
        if (isProcessing) return

        viewModelScope.launch {
            isProcessing = true
            loadingMessage = "Генерация структуры APK.."
            processingProgress = 0f

            val dummyFiles = withContext(Dispatchers.Default) {
                val apkEntries = mutableListOf(
                    // META-INF
                    "META-INF/MANIFEST.MF" to 3,
                    "META-INF/CERT.SF" to 4,
                    "META-INF/CERT.RSA" to 2,
                    // Основной код
                    "classes.dex" to 3800,
                    // Ресурсы
                    "resources.arsc" to 1450,
                    "AndroidManifest.xml" to 22,
                    // Нативные библиотеки
                    "lib/arm64-v8a/libnative-lib.so" to 385,
                    // Kotlin
                    "kotlin/kotlin.kotlin_builtins" to 8,
                    "kotlin/collections/collections.kotlin_builtins" to 5,
                    "kotlin/reflect/reflect.kotlin_builtins" to 3,
                    "kotlin/coroutines/coroutines.kotlin_builtins" to 4,
                    // Ресурсы
                    "res/drawable/ic_launcher_background.xml" to 1,
                    "res/drawable/ic_launcher_foreground.xml" to 2,
                    "res/drawable-hdpi/ic_notification.png" to 6,
                    "res/drawable-xhdpi/ic_notification.png" to 10,
                    "res/drawable-xxhdpi/ic_notification.png" to 18,
                    "res/drawable-xxxhdpi/ic_notification.png" to 28,
                    "res/drawable-xxxhdpi/splash_logo.png" to 195,
                    "res/drawable-xxxhdpi/banner_home.png" to 340,
                    "res/drawable-xxxhdpi/banner_promo.png" to 287,
                    "res/drawable-xxxhdpi/bg_gradient.png" to 44,
                    "res/drawable-night/ic_launcher_background.xml" to 1,
                    "res/layout/activity_main.xml" to 5,
                    "res/layout/fragment_home.xml" to 9,
                    "res/layout/fragment_profile.xml" to 7,
                    "res/layout/fragment_settings.xml" to 6,
                    "res/layout/item_product_card.xml" to 4,
                    "res/layout/item_notification.xml" to 3,
                    "res/layout/dialog_confirmation.xml" to 3,
                    "res/layout/dialog_loading.xml" to 2,
                    // Навигация
                    "res/navigation/nav_graph.xml" to 6,
                    "res/menu/bottom_nav_menu.xml" to 1,
                    "res/menu/menu_main.xml" to 1,
                    // XML конфиги
                    "res/xml/network_security_config.xml" to 1,
                    "res/xml/file_provider_paths.xml" to 1,
                    "res/xml/backup_rules.xml" to 1,
                    // Значения
                    "res/values/strings.arsc" to 24,
                    "res/values-ru/strings.arsc" to 22,
                    "res/values-de/strings.arsc" to 20,
                    "res/values-fr/strings.arsc" to 19,
                    "res/values-es/strings.arsc" to 18,
                    "res/values-zh/strings.arsc" to 21,
                    "res/values-ja/strings.arsc" to 19,
                    "res/values-night/themes.arsc" to 5,
                    "res/values/colors.arsc" to 3,
                    "res/values/themes.arsc" to 5,
                    "res/values/dimens.arsc" to 2,
                    "res/font/inter_regular.ttf" to 178,
                    "res/font/inter_bold.ttf" to 170,
                    "res/raw/lottie_loading.json" to 32,
                    "res/raw/lottie_success.json" to 28,
                    "res/raw/config_prod.json" to 6,
                )

                val imageNames = listOf(
                    "bg_onboarding_%d.png" to listOf(380, 410, 355),
                    "img_category_%d.png" to listOf(92, 88, 95, 84, 79),
                    "img_product_%d.png" to listOf(145, 132, 158, 121, 167, 139),
                    "img_avatar_%d.png" to listOf(34, 38, 29, 41),
                )
                var id = apkEntries.size + 1
                var imgIdx = 1
                while (apkEntries.size < 100) {
                    val (pattern, sizes) = imageNames[imgIdx % imageNames.size]
                    val sizeKB = sizes[imgIdx % sizes.size]
                    apkEntries.add("res/drawable-xxxhdpi/${pattern.format(imgIdx)}" to sizeKB)
                    imgIdx++
                    id++
                }

                val result = mutableListOf<Document>()
                val total = apkEntries.size.coerceAtMost(100)
                apkEntries.take(total).forEachIndexed { index, (name, sizeKB) ->
                    result.add(Document(index + 1, name, buildContent(name, sizeKB)))
                    withContext(Dispatchers.Main) {
                        processingProgress = (index + 1) / total.toFloat()
                        loadingMessage = "Генерация файлов... ${index + 1}/$total"
                    }
                }
                result
            }

            documents.clear()
            documents.addAll(dummyFiles)
            selectedDocId = 1
            isProcessing = false
            loadingMessage = ""
        }
    }

    fun signAllFiles() {
        if (isProcessing) return
        if (keyPair == null) return

        viewModelScope.launch {
            isProcessing = true
            processingProgress = 0f
            var totalHashNs = 0L
            var totalSigNs = 0L

            val updatedDocs = withContext(Dispatchers.Default) {
                val result = mutableListOf<Document>()
                val total = documents.size
                documents.forEachIndexed { index, doc ->
                    withContext(Dispatchers.Main) {
                        loadingMessage =
                            "Подпись: ${doc.name.substringAfterLast('/')}\n${index + 1} из $total"
                    }

                    try {
                        val (hash, hashMetrics) = CryptoManager.calculateSHA256(doc.content)
                        val (sig, sigMetrics) = CryptoManager.signData(doc.content, keyPair!!)

                        totalHashNs += (hashMetrics.timeSeconds * 1_000_000_000).toLong()
                        totalSigNs += (sigMetrics.timeSeconds * 1_000_000_000).toLong()

                        val hashTime = df.format(hashMetrics.timeSeconds)
                        val sigTime = df.format(sigMetrics.timeSeconds)

                        result.add(
                            doc.copy(
                                hashResult = hash,
                                signatureResult = sig,
                                signMetrics = "Хэш: $hashTime сек.\nRSA: $sigTime сек.\nРазмер: ${sigMetrics.sizeBytes} байт"
                            )
                        )
                    } catch (e: Exception) {
                        result.add(
                            doc.copy(
                                signMetrics = "Ошибка: ${e.message?.take(80) ?: "неизвестная"}"
                            )
                        )
                    }

                    val progress = (index + 1) / total.toFloat()
                    withContext(Dispatchers.Main) { processingProgress = progress }
                }
                result
            }

            withContext(Dispatchers.Main) {
                signAllMetrics = buildString {
                    appendLine("Файлов подписано: ${documents.size}")
                    appendLine("Хэш суммарно: ${df.format(totalHashNs / 1e9)} сек.")
                    appendLine("RSA суммарно: ${df.format(totalSigNs / 1e9)} сек.")
                    append("Итого: ${df.format((totalHashNs + totalSigNs) / 1e9)} сек.")
                }
            }

            documents.clear()
            documents.addAll(updatedDocs)
            isProcessing = false
            loadingMessage = ""
        }
    }

    fun generateKeys(size: Int) {
        if (isProcessing) return

        viewModelScope.launch {
            isProcessing = true
            loadingMessage = "Генерация ключей RSA-$size..."
            processingProgress = 0f

            try {
                withContext(Dispatchers.Default) {
                    val (kp, metrics) = CryptoManager.generateKeyPair(size)
                    keyPair = kp
                    publicKeyStr = Base64.encodeToString(kp.public.n.toByteArray(), Base64.NO_WRAP)
                    privateKeyStr =
                        Base64.encodeToString(kp.private.d.toByteArray(), Base64.NO_WRAP)
                    val time = df.format(metrics.timeSeconds)
                    keyMetrics =
                        "Время генерации: $time сек.\nРазмер Public Key: ${kp.public.n.toByteArray().size} байт"
                }
            } catch (e: Exception) {
                keyMetrics = "Ошибка генерации: ${e.message?.take(120) ?: "неизвестная"}"
            }

            isProcessing = false
            loadingMessage = ""
        }
    }

    var massVerifyResults by mutableStateOf<List<MassVerifyResult>>(emptyList())
        private set
    var massVerifyShow by mutableStateOf(false)
        private set
    var verifyMetrics by mutableStateOf("")
        private set

    fun verifyAllFiles() {
        if (isProcessing) return
        if (keyPair == null) return

        massVerifyShow = false

        viewModelScope.launch {
            isProcessing = true
            processingProgress = 0f

            val results = withContext(Dispatchers.Default) {
                val list = mutableListOf<MassVerifyResult>()
                val start = System.nanoTime()
                val total = documents.size

                documents.forEachIndexed { index, doc ->
                    withContext(Dispatchers.Main) {
                        loadingMessage =
                            "Проверка: ${doc.name.substringAfterLast('/')}\n${index + 1} из $total"
                    }

                    var isValid = false
                    if (doc.signatureResult.isNotBlank()) {
                        try {
                            val sigBytes = Base64.decode(doc.signatureResult, Base64.NO_WRAP)
                            isValid = RSA.verify(
                                doc.content.toByteArray(Charsets.UTF_8),
                                sigBytes,
                                keyPair!!.public
                            )
                        } catch (_: Exception) {
                            isValid = false
                        }
                    }
                    list.add(MassVerifyResult(doc.name, isValid))

                    val progress = (index + 1) / total.toFloat()
                    withContext(Dispatchers.Main) { processingProgress = progress }
                }

                val end = System.nanoTime()
                val timeSec = (end - start) / 1_000_000_000.0
                withContext(Dispatchers.Main) {
                    verifyMetrics =
                        "Общее время проверки ${documents.size} файлов: ${df.format(timeSec)} сек."
                }
                list
            }

            massVerifyResults = results
            massVerifyShow = true
            isProcessing = false
            loadingMessage = ""
        }
    }
}