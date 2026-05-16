package ru.ekzw.sha256withrsa.model

import android.util.Base64
import java.math.BigInteger
import kotlin.system.measureNanoTime

class RSAPublicKey(val n: BigInteger, val e: BigInteger)
class RSAPrivateKey(val n: BigInteger, val d: BigInteger)
class KeyPair(val public: RSAPublicKey, val private: RSAPrivateKey)

object CryptoManager {
    fun generateKeyPair(keySize: Int): Pair<KeyPair, CryptoMetrics> {
        var keyPair: KeyPair? = null
        val timeNanos = measureNanoTime {
            keyPair = RSA.generateKeyPair(keySize)
        }
        val size = keyPair!!.public.n.toByteArray().size + keyPair.private.d.toByteArray().size
        return Pair(keyPair, CryptoMetrics(timeNanos / 1_000_000_000.0, size))
    }

    fun calculateSHA256(input: String): Pair<String, CryptoMetrics> {
        var hashHex = ""
        val inputBytes = input.toByteArray(Charsets.UTF_8)
        val timeNanos = measureNanoTime {
            val digest = SHA256.hash(inputBytes)
            hashHex = digest.joinToString("") { "%02x".format(it) }
        }
        return Pair(hashHex, CryptoMetrics(timeNanos / 1_000_000_000.0, 32))
    }

    fun signData(input: String, keyPair: KeyPair): Pair<String, CryptoMetrics> {
        var signatureBase64 = ""
        var sigBytes: ByteArray = byteArrayOf()
        val inputBytes = input.toByteArray(Charsets.UTF_8)

        val timeNanos = measureNanoTime {
            sigBytes = RSA.sign(inputBytes, keyPair.private)
            signatureBase64 = Base64.encodeToString(sigBytes, Base64.NO_WRAP)
        }
        return Pair(signatureBase64, CryptoMetrics(timeNanos / 1_000_000_000.0, sigBytes.size))
    }
}