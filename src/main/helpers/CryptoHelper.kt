package main.helpers

import main.daos.CryptoKeyPair
import main.daos.CryptoKeyPairType
import java.security.KeyPairGenerator
import java.security.SecureRandom

object CryptoHelper {
    fun generateCryptoKeyPair(keyPairType: CryptoKeyPairType): CryptoKeyPair {
        val generator = KeyPairGenerator
            .getInstance("DSA", "SUN")

        generator.initialize(
                1024,
                SecureRandom.getInstance("SHA1PRNG", "SUN")
            )

        val keyPair = generator.generateKeyPair()
        return CryptoKeyPair(keyPair.public.encoded.toString(), keyPair.private.encoded.toString(), keyPairType.str)
    }
}
