package ru.ekzw.sha256withrsa.model

data class Document(
    val id: Int,
    var name: String,
    var content: String,
    var hashResult: String = "",
    var signatureResult: String = "",
    var signMetrics: String = ""
)