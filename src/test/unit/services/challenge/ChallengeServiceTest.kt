package test.unit.services.challenge

import io.kotlintest.*
import io.kotlintest.specs.WordSpec
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith
import main.daos.*
import framework.models.Handler
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import main.services.ChallengeService
import main.services.aws.qldb.LedgerService
import test.TestHelper

@ExtendWith(MockKExtension::class)
class ChallengeServiceTest : WordSpec() {
    private lateinit var challenge: Challenge
    private lateinit var challenge2: Challenge
    private lateinit var publicKey: String

    override fun beforeTest(description: Description) {
        Handler.TEST = true
        Handler.clearLedger()
        val newKeyPairs = TestHelper.generateKeyPairs()
        publicKey = newKeyPairs[0].value.publicKey
        val challenges = TestHelper.generateChallenge(publicKey, 7)
        val challengeSettings = TestHelper.generateChallengeSettings(publicKey, 2)
        challenge = Challenge(
            parentChallenge = challenges[0].publicKey,
            challengeSettings = challengeSettings[0],
            preReqs = listOf(
                challenges[1].publicKey!!,
                challenges[2].publicKey!!,
                challenges[3].publicKey!!,
                challenges[4].publicKey!!,
                challenges[5].publicKey!!,
                challenges[6].publicKey!!
            ),
            completionCriteria = publicKey,
            distributionFeeReward = TestHelper.generateReward(RewardType.SINGLE),
            challengeType = ChallengeType.ASYNC,
            reward = TestHelper.generateReward(RewardType.N_OVER_2)
        )

        challenge2 = Challenge(
            parentChallenge = challenges[0].publicKey,
            challengeSettings = challengeSettings[1],
            preReqs = listOf(
                challenges[1].publicKey!!,
                challenges[2].publicKey!!,
                challenges[3].publicKey!!,
                challenges[4].publicKey!!,
                challenges[5].publicKey!!,
                challenges[6].publicKey!!
            ),
            completionCriteria = publicKey,
            distributionFeeReward = TestHelper.generateReward(RewardType.SINGLE),
            challengeType = ChallengeType.ASYNC,
            reward = TestHelper.generateReward(RewardType.N_OVER_2)
        )

        challenge = ChallengeService.create(challenge)
        challenge2 = ChallengeService.create(challenge2)
    }

    init {
        "calling create with a valid challenge" should {
            "insert the challenge into the ledger with a share transaction" {
                challenge.publicKey shouldNotBe null
                val foundChallenge = LedgerService.findBy(
                    clazz = Challenge::class,
                    keyValues = listOf(Pair("publicKey", challenge.publicKey!!))
                )
                foundChallenge shouldNotBe null
                foundChallenge.hashCode shouldBe challenge.hashCode

                val foundShare = LedgerService.findBy(
                    clazz = NTransaction::class,
                    nestedClazzes = listOf(NAction::class),
                    keyValues = listOf(Pair("c.naction.dataKey", challenge.publicKey!!))
                )
                foundShare.outbound shouldBe challenge.publicKey
                foundShare.inbound shouldBe challenge.challengeSettings.admin
            }
        }

        "calling findByPublicKey with a valid challenge public key" should {
            "return the challenge" {
                val foundChallenge = ChallengeService.findByPublicKey(challenge.publicKey!!)
                foundChallenge.hashCode shouldBe challenge.hashCode
            }
        }

        "calling findAllChallengesByPublicKey with a valid wallet public key" should {
            "return all challenges" {
                val foundChallenges = ChallengeService.findAllChallengesByPublicKey(publicKey)
                foundChallenges.size shouldBe 2

                val foundChallengeHashCodes = foundChallenges.map { it.hashCode }
                foundChallengeHashCodes.shouldContainExactlyInAnyOrder(
                    listOf(
                        challenge.hashCode,
                        challenge2.hashCode
                    )
                )
            }
        }

        "calling findSharesForPublicKey with a valid public key" should {
            "return all challenges and how many shares are available for that public key" {
                val foundChallengesAndShares = ChallengeService.findSharesForPublicKey(publicKey)
                foundChallengesAndShares.size shouldBe 2

                val foundChallengeHashCodesAndShares = foundChallengesAndShares.map {
                    Pair(it.key.hashCode, it.value)
                }.toMap()

                foundChallengeHashCodesAndShares[challenge.hashCode] shouldBe challenge.challengeSettings.maxShares
                foundChallengeHashCodesAndShares[challenge2.hashCode] shouldBe challenge2.challengeSettings.maxShares
            }
        }
    }
}