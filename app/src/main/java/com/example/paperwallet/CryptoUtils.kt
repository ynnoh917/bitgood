package com.example.paperwallet

import org.bitcoinj.core.ECKey
import org.web3j.crypto.Keys
import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom

object CryptoUtils {

    fun generateAddressAndKey(chain: String): Pair<String, String> {
        return when (chain.uppercase()) {
            "BTC", "BCH", "BTG", "LTC", "DOGE" -> generateBitcoinFamily(chain)
            "ETH", "BNB", "ETC" -> generateEvmFamily()
            "TRON" -> generateTron()
            else -> generateMock(chain) // SOL, TON, XMR 等占位模拟
        }
    }

    private fun generateBitcoinFamily(chain: String): Pair<String, String> {
        val key = ECKey()
        val pubKeyHash = key.pubKeyHash

        val versionByte: Byte = when (chain.uppercase()) {
            "BTC", "BCH" -> 0x00.toByte()
            "BTG" -> 0x26.toByte()
            "LTC" -> 0x30.toByte()
            "DOGE" -> 0x1E.toByte()
            else -> 0x00.toByte()
        }
        val address = getBase58CheckAddress(versionByte, pubKeyHash)
        
        val wifVersion: Byte = when (chain.uppercase()) {
            "BTC", "BCH", "BTG" -> 0x80.toByte()
            "LTC" -> 0xB0.toByte()
            "DOGE" -> 0x9E.toByte()
            else -> 0x80.toByte()
        }
        val payload = ByteArray(33)
        payload[0] = wifVersion
        System.arraycopy(key.privKey, 0, payload, 1, 32)
        payload[32] = 0x01
        val privateWIF = Base58.encode(addChecksum(payload))

        return Pair(address, privateWIF)
    }

    private fun generateEvmFamily(): Pair<String, String> {
        val keyPair = Keys.createEcKeyPair()
        val address = "0x" + Keys.getAddress(keyPair.publicKey)
        val privateKey = keyPair.privateKey.toString(16).padStart(64, '0')
        return Pair(address, privateKey)
    }

    private fun generateTron(): Pair<String, String> {
        val keyPair = Keys.createEcKeyPair()
        val pubKeyHash = Keys.getAddress(keyPair.publicKey)
        val addressBytes = ByteArray(21)
        addressBytes[0] = 0x41.toByte()
        System.arraycopy(hexStringToByteArray(pubKeyHash), 0, addressBytes, 1, 20)
        val address = Base58.encode(addChecksum(addressBytes))
        val privateKey = keyPair.privateKey.toString(16).padStart(64, '0')
        return Pair(address, privateKey)
    }

    private fun generateMock(chain: String): Pair<String, String> {
        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        val hex = bytes.joinToString("") { "%02x".format(it) }
        return Pair("${chain.lowercase()}_${hex.take(34)}", hex)
    }

    private fun getBase58CheckAddress(version: Byte, hash: ByteArray): String {
        val payload = ByteArray(21)
        payload[0] = version
        System.arraycopy(hash, 0, payload, 1, 20)
        return Base58.encode(addChecksum(payload))
    }

    private fun addChecksum(data: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        val checksum = digest.digest(digest.digest(data)).copyOfRange(0, 4)
        return data + checksum
    }

    private fun hexStringToByteArray(hex: String): ByteArray {
        val len = hex.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(hex[i], 16) shl 4) +
                    Character.digit(hex[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }

    object Base58 {
        private const val ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
        fun encode(input: ByteArray): String {
            if (input.isEmpty()) return ""
            var zeros = 0
            while (zeros < input.size && input[zeros].toInt() == 0) zeros++
            var num = BigInteger(1, input)
            val sb = StringBuilder()
            while (num > BigInteger.ZERO) {
                val rem = num.mod(BigInteger.valueOf(58))
                sb.append(ALPHABET[rem.toInt()])
                num = num.divide(BigInteger.valueOf(58))
            }
            for (i in 0 until zeros) sb.append(ALPHABET[0])
            return sb.reverse().toString()
        }
    }
}