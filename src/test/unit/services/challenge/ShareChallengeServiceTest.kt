package test.unit.services.challenge

import io.kotlintest.*
import io.kotlintest.specs.WordSpec
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith
import main.daos.*
import framework.models.Handler
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import kotlinserverless.framework.models.MyException
import kotlinserverless.framework.models.NotEnoughSharesException
import main.services.ChallengeService
import org.joda.time.DateTime
import test.TestHelper
import java.time.LocalDateTime
import java.time.ZoneId

@ExtendWith(MockKExtension::class)
class ShareChallengeServiceTest : WordSpec() {
    private lateinit var key1: NewCryptoKeyPair
    private lateinit var key2: NewCryptoKeyPair
    private lateinit var key3: NewCryptoKeyPair
    private lateinit var challenge: Challenge

    override fun beforeTest(description: Description) {
        Handler.clearLedger()
        val newKeyPairs = TestHelper.generateKeyPairs(3)
        key1 = newKeyPairs[0]
        key2 = newKeyPairs[1]
        key3 = newKeyPairs[2]
        challenge = TestHelper.generateChallenge(
            publicKey = key1.value.publicKey,
            challengeType = ChallengeType.SYNC
        ).first()
        challenge = ChallengeService.create(challenge)
        Thread.sleep(1000)
    }

    init {
        // TODO test off chain
        "calling execute with enough shares available in one tx" should {
            "generate a single transaction sharing to the public key" {
                val transactions = ChallengeService.shareChallenge(
                    outbound = key1.value.publicKey,
                    inbound = key2.value.publicKey,
                    challenge = challenge,
                    shares = 50
                )
                transactions.size shouldBe 1
                transactions.first().naction.type shouldBe ActionType.SHARE
                transactions.first().naction.dataKey shouldBe challenge.publicKey
                transactions.first().challengeData?.maxShares shouldBe 50
            }
        }
        // TODO test if multi-tx share fails midway
        "calling execute with enough shares available in multiple tx" should {
            "generate a multiple transaction sharing to the public key" {
                /**
                 * key1 - 100
                 * key2 - 0
                 * key3 - 0
                 */
                ChallengeService.shareChallenge(
                    outbound = key1.value.publicKey,
                    inbound = key2.value.publicKey,
                    challenge = challenge,
                    shares = 50
                )
                Thread.sleep(1000)
                /**
                 * key1 - 50
                 * key2 - 50
                 * key3 - 0
                 */
                ChallengeService.shareChallenge(
                    outbound = key1.value.publicKey,
                    inbound = key3.value.publicKey,
                    challenge = challenge,
                    shares = 50
                )
                Thread.sleep(1000)
                /**
                 * key1 - 0
                 * key2 - 50
                 * key3 - 50
                 */
                ChallengeService.shareChallenge(
                    outbound = key2.value.publicKey,
                    inbound = key3.value.publicKey,
                    challenge = challenge,
                    shares = 10
                )
                Thread.sleep(1000)
                /**
                 * key1 - 0
                 * key2 - 40
                 * key3 - 60
                 */
                val transactions = ChallengeService.shareChallenge(
                    outbound = key3.value.publicKey,
                    inbound = key1.value.publicKey,
                    challenge = challenge,
                    shares = 55
                )

                /**
                 * key1 - 55
                 * key2 - 40
                 * key3 - 5
                 */
                transactions.size shouldBe 2
                transactions.map {
                    it.challengeData?.maxShares
                }.shouldContainExactlyInAnyOrder(listOf(50, 5))
            }
        }
        "calling execute without enough shares available" should {
            "fails to generate any new transactions" {
                shouldThrow<NotEnoughSharesException> {
                    ChallengeService.shareChallenge(
                        outbound = key1.value.publicKey,
                        inbound = key2.value.publicKey,
                        challenge = challenge,
                        shares = 1000
                    )
                }
            }
        }
        "attempting to share after your share has expired" should {
            "fails to generate any new transactions" {
                ChallengeService.shareChallenge(
                    outbound = key1.value.publicKey,
                    inbound = key2.value.publicKey,
                    challenge = challenge,
                    shares = 100,
                    expiration = LocalDateTime.now(ZoneId.of("UTC")).plusDays(-1)
                )
                shouldThrow<NotEnoughSharesException> {
                    ChallengeService.shareChallenge(
                        outbound = key2.value.publicKey,
                        inbound = key1.value.publicKey,
                        challenge = challenge,
                        shares = 50
                    )
                }
            }
        }
    }
}