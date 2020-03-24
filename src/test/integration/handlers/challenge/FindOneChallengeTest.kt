package test.integration.handlers.challenge

import com.google.gson.Gson
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith
import io.kotlintest.Description
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import framework.models.Handler
import main.daos.*
import main.services.ChallengeService
import test.TestHelper

@ExtendWith(MockKExtension::class)
class FindOneChallengeTest : WordSpec() {
    private val handler: Handler = Handler(true)
    private lateinit var challenge: Challenge
    private lateinit var map: Map<String, Any>
    private lateinit var badMap: Map<String, Any>

    override fun beforeTest(description: Description) {
        Handler.clearLedger()
        val publicKey = TestHelper.generateKeyPairs().first().value.publicKey
        challenge = TestHelper.generateChallenge(publicKey).first()
        ChallengeService.create(challenge)
        map = TestHelper.buildRequest(
            publicKey,
            "/challenge",
            "GET",
                null,
            mapOf(
                Pair("identifier", challenge.publicKey)
            )
        )
        badMap = TestHelper.buildRequest(
            publicKey,
            "/challenge",
            "GET",
                null,
            mapOf(
                Pair("identifier", "404")
            )
        )
    }

    init {
        "calling the API with a valid challenge Id" should {
            "should return a valid challenge" {
                val findOneChallengeResult = handler.handleRequest(map, null)
                findOneChallengeResult.statusCode shouldBe 200

                val challengeData = Handler.gson.fromJson(
                    findOneChallengeResult.body as String,
                    Challenge::class.java
                )
                challengeData.challengeSettings.name shouldBe challenge.challengeSettings.name
            }
        }
        "calling the API with an invalid challenge Id" should {
            "should return a failure response" {
                val findOneChallengeResult = handler.handleRequest(badMap, null)
                findOneChallengeResult.statusCode shouldBe 404
            }
        }
    }
}