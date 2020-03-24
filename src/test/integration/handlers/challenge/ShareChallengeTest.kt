package test.integration.handlers.challenge

import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import io.kotlintest.Description
import framework.models.Handler
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith
import main.daos.*
import main.services.ChallengeService
import test.TestHelper

@ExtendWith(MockKExtension::class)
class ShareChallengeTest : WordSpec() {
    private val handler: Handler = Handler(true)
    private lateinit var key1: NewCryptoKeyPair
    private lateinit var key2: NewCryptoKeyPair
    private lateinit var syncChallenge: Challenge
    private lateinit var asyncChallenge: Challenge

    override fun beforeTest(description: Description) {
        Handler.clearLedger()
        val newKeyPairs = TestHelper.generateKeyPairs(2)
        key1 = newKeyPairs[0]
        key2 = newKeyPairs[1]
        syncChallenge = TestHelper.generateChallenge(
            publicKey = key1.value.publicKey,
            challengeType = ChallengeType.SYNC
        ).first()
        syncChallenge = ChallengeService.create(syncChallenge)

        asyncChallenge = TestHelper.generateChallenge(
            publicKey = key1.value.publicKey
        ).first()
        asyncChallenge = ChallengeService.create(asyncChallenge)
    }

    init {
        "attempting to share with enough share transactions available on a synchronous challenge" should {
            "should return a successful transaction" {
                val request = TestHelper.buildRequest(
                    key1.value.publicKey,
                    "/challenge/share",
                    "PATCH",
                    Handler.gson.toJson(
                        ShareChallengeData(
                            inbound = key2.value.publicKey,
                            challengePublicKey = syncChallenge.publicKey!!,
                            shares = 3
                        )
                    )
                )

                val response = handler.handleRequest(request, null)
                response.statusCode shouldBe 200

                val transactions = Handler.gson.fromJson(
                    response.body as String,
                    List::class.java as Class<List<NTransaction>>
                )

                transactions.size shouldBe 1
            }
        }

        "attempting to share without enough share transactions available on a synchronous challenge" should {
            "should return a failure response" {
                val request = TestHelper.buildRequest(
                    key1.value.publicKey,
                    "/challenge/share",
                    "PATCH",
                    Handler.gson.toJson(
                        ShareChallengeData(
                            inbound = key2.value.publicKey,
                            challengePublicKey = syncChallenge.publicKey!!,
                            shares = 9000
                        )
                    )
                )
                val response = handler.handleRequest(request, null)
                response.statusCode shouldBe 400
                response.body shouldBe "Failed to share 9000, can only share 100"
            }
        }

        "attempting to share with an off chain valid share regardless of shares count" should {
            "should return a successful transaction" {
                val request = TestHelper.buildRequest(
                    key1.value.publicKey,
                    "/challenge/share",
                    "PATCH",
                    Handler.gson.toJson(
                        ShareChallengeData(
                            inbound = key2.value.publicKey,
                            challengePublicKey = asyncChallenge.publicKey!!,
                            shares = Integer.MAX_VALUE
                        )
                    )
                )
                val response = handler.handleRequest(request, null)
                response.statusCode shouldBe 200
            }
        }
    }
}