package test.integration.handlers.challenge

import io.kotlintest.shouldBe
import io.kotlintest.Description
import io.kotlintest.specs.WordSpec
import io.mockk.junit5.MockKExtension
import framework.models.Handler
import io.kotlintest.shouldNotBe
import main.daos.Challenge
import main.daos.NewCryptoKeyPair
import org.junit.jupiter.api.extension.ExtendWith
import test.TestHelper

@ExtendWith(MockKExtension::class)
class ChallengeCreationTest : WordSpec() {
    private val handler: Handler = Handler(true)
    private lateinit var newKeyPair: NewCryptoKeyPair
    private lateinit var challenge: Challenge
    private lateinit var map: Map<String, Any>
    private lateinit var badMap: Map<String, Any>

    override fun beforeTest(description: Description) {
        Handler.clearLedger()
        val newKeyPairs = TestHelper.generateKeyPairs()
        newKeyPair = newKeyPairs[0]
        challenge = TestHelper.generateChallenge(newKeyPair.value.publicKey).first()
        map = TestHelper.buildRequest(
            newKeyPair.value.publicKey,
            "/challenge",
            "POST",
            Handler.gson.toJson(challenge)
        )

        badMap = TestHelper.buildRequest(
            newKeyPair.value.publicKey,
            "/challenge",
            "POST",
            "null"
        )
    }

    init {
        "correct path" should {
            "should return a valid new challenge" {
                val response = handler.handleRequest(map, null)
                response.statusCode shouldBe 200

                val challengeData = Handler.gson.fromJson(
                    response.body as String,
                    Challenge::class.java
                )
                challengeData.challengeSettings.name shouldBe challenge.challengeSettings.name
                challengeData.publicKey shouldNotBe null
            }
        }

        "calling this API with incorrect parameters" should {
            "should return a failure response" {
                val response = handler.handleRequest(badMap, null)
                response.statusCode shouldBe 400
            }
        }
    }
}