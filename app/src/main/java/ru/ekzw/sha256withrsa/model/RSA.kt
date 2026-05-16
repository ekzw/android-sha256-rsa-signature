package ru.ekzw.sha256withrsa.model

import java.math.BigInteger
import java.security.SecureRandom

object RSA {
    fun generateKeyPair(keySize: Int): KeyPair {
        val random = SecureRandom()
        val e = BigInteger("65537")
        var p: BigInteger
        var q: BigInteger
        var phi: BigInteger
        var n: BigInteger

        do {
            p = BigInteger.probablePrime(keySize / 2, random)
            q = BigInteger.probablePrime(keySize / 2, random)
            n = p.multiply(q)
            val pMinus1 = p.subtract(BigInteger.ONE)
            val qMinus1 = q.subtract(BigInteger.ONE)
            phi = pMinus1.multiply(qMinus1)
        } while (e.gcd(phi) != BigInteger.ONE)

        val d = e.modInverse(phi)
        return KeyPair(RSAPublicKey(n, e), RSAPrivateKey(n, d))
    }

    private val SHA256_ASN1 = byteArrayOf(
        0x30, 0x31, 0x30, 0x0D, 0x06, 0x09, 0x60,
        0x86.toByte(), 0x48, 0x01, 0x65, 0x03, 0x04, 0x02, 0x01, 0x05, 0x00, 0x04, 0x20
    )

    private fun getByteLength(n: BigInteger): Int = (n.bitLength() + 7) / 8

    fun sign(message: ByteArray, privateKey: RSAPrivateKey): ByteArray {
        val hash = SHA256.hash(message)
        val t = SHA256_ASN1 + hash
        val k = getByteLength(privateKey.n)
        if (k < t.size + 11) throw IllegalArgumentException("Ключ слишком короткий")

        val em = ByteArray(k)
        em[0] = 0x00
        em[1] = 0x01
        var i = 2
        while (i < k - t.size - 1) {
            em[i++] = 0xFF.toByte()
        }
        em[i] = 0x00
        System.arraycopy(t, 0, em, i + 1, t.size)

        val m = BigInteger(1, em)
        val s = m.modPow(privateKey.d, privateKey.n) // Шифрование

        val sigBytes = s.toByteArray()
        val result = ByteArray(k)
        if (sigBytes.size >= k) {
            System.arraycopy(sigBytes, sigBytes.size - k, result, 0, k)
        } else {
            System.arraycopy(sigBytes, 0, result, k - sigBytes.size, sigBytes.size)
        }
        return result
    }

    fun verify(message: ByteArray, signature: ByteArray, publicKey: RSAPublicKey): Boolean {
        val k = getByteLength(publicKey.n)
        if (signature.size != k) return false

        val s = BigInteger(1, signature)
        if (s >= publicKey.n) return false
        val m = s.modPow(publicKey.e, publicKey.n)

        val em = m.toByteArray()
        val emPadded = ByteArray(k)
        if (em.size >= k) {
            System.arraycopy(em, em.size - k, emPadded, 0, k)
        } else {
            System.arraycopy(em, 0, emPadded, k - em.size, em.size)
        }

        val hash = SHA256.hash(message)
        val t = SHA256_ASN1 + hash
        val expectedEm = ByteArray(k)
        expectedEm[0] = 0x00
        expectedEm[1] = 0x01
        var i = 2
        while (i < k - t.size - 1) {
            expectedEm[i++] = 0xFF.toByte()
        }
        expectedEm[i] = 0x00
        System.arraycopy(t, 0, expectedEm, i + 1, t.size)

        return emPadded.contentEquals(expectedEm)
    }
}