package test.integration.handlers.challenge

import io.kotlintest.specs.WordSpec
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith
import io.kotlintest.Description
import io.kotlintest.shouldBe
import framework.models.Handler
import main.daos.*
import main.services.ChallengeService
import test.TestHelper

@ExtendWith(MockKExtension::class)
class FindAllChallengesTest : WordSpec() {
    private val handler: Handler = Handler(true)
    private lateinit var key1: NewCryptoKeyPair
    private lateinit var key2: NewCryptoKeyPair
    private lateinit var map: Map<String, Any>
    private lateinit var notFoundMap: Map<String, Any>

    override fun beforeTest(description: Description) {
        Handler.clearLedger()
        val newKeyPairs = TestHelper.generateKeyPairs(2)
        key1 = newKeyPairs[0]
        key2 = newKeyPairs[1]
        val challenges = TestHelper.generateChallenge(key1.value.publicKey, 4)
        challenges.forEach { challenge ->
            ChallengeService.create(challenge)
        }
        map = TestHelper.buildRequest(
            key1.value.publicKey,
            "/challenges",
            "GET",
            null
        )
        notFoundMap = TestHelper.buildRequest(
            key2.value.publicKey,
            "/challenges",
            "GET",
            null
        )
    }

    init {
        "Calling the API with a public key that has challenges" should {
            "should return all of the challenges for the public key" {
                val findAllChallengesResult = handler.handleRequest(map, null)
                findAllChallengesResult.statusCode shouldBe 200
                val challenges = Handler.gson.fromJson(
                    findAllChallengesResult.body as String,
                    List::class.java as Class<List<Challenge>>
                )
                challenges.size shouldBe 4
            }
        }

        "Calling the API with a public key that has no challenges" should {
            "should return a 404 not found response" {
                val findAllChallengesResult = handler.handleRequest(notFoundMap, null)
                findAllChallengesResult.statusCode shouldBe 404
            }
        }
    }
}