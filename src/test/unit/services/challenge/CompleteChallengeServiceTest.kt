package test.unit.services.challenge
//
//import framework.models.idValue
//import io.kotlintest.*
//import io.kotlintest.specs.WordSpec
//import io.mockk.junit5.MockKExtension
//import org.junit.jupiter.api.extension.ExtendWith
//import main.daos.*
//import framework.models.Handler
//import kotlinserverless.framework.services.SOAResultType
//import main.services.challenge.ActivateChallengeService
//import main.services.challenge.CompleteChallengeService
//import main.services.challenge.ShareChallengeService
//import org.jetbrains.exposed.sql.transactions.transaction
//import test.TestHelper
//
//@ExtendWith(MockKExtension::class)
//class CompleteChallengeServiceTest : WordSpec() {
//    private lateinit var challenge: Challenge
//    private lateinit var publicKey1: String
//    private lateinit var publicKey2: String
//    private lateinit var publicKey3: String
//
//    override fun beforeTest(description: Description) {
//        Handler.connectAndBuildTables()
//        transaction {
//            val NewKeyPairs = TestHelper.generateKeyPairs(3)
//            publicKey1 = NewKeyPairs[0].publicKey
//            publicKey2 = NewKeyPairs[1].publicKey
//            publicKey3 = NewKeyPairs[2].publicKey
//            /**
//             *          key1 (100)
//             */
//            challenge = TestHelper.generateFullChallenge(publicKey1, publicKey1,1, true)[0]
//        }
//    }
//
//    override fun afterTest(description: Description, result: TestResult) {
//        Handler.disconnectAndDropTables()
//    }
//
//    init {
//        // TODO test off chain
//        // TODO test if multi-tx share fails midway
//        "calling execute with valid data" should {
//            "should complete the challenge by changing the state and distributing rewards" {
//                transaction {
//                    ActivateChallengeService.execute(publicKey1, challenge.idValue)
//
//                    /**
//                     *          key1 (50)
//                     *            |
//                     *          key2 (50)
//                     */
//                    ShareChallengeService.execute(
//                        publicKey1,
//                        challenge,
//                        50,
//                        publicKey2
//                    )
//                    /**
//                     *          key1 (20)
//                     *            |
//                     *          key2 (50, 30)
//                     */
//                    ShareChallengeService.execute(
//                        publicKey1,
//                        challenge,
//                        30,
//                        publicKey2
//                    )
//                    /**
//                     *          key1 (20)
//                     *            |
//                     *          key2 (0)
//                     *            |
//                     *          key3 (80)
//                     */
//                    ShareChallengeService.execute(
//                        publicKey2,
//                        challenge,
//                        80,
//                        publicKey3
//                    )
//
//                    // key 2 should not be able to complete the challenge
//
//                    var result = CompleteChallengeService.execute(
//                        publicKey1,
//                        challenge,
//                        publicKey2
//                    )
//                    result.result shouldBe SOAResultType.FAILURE
//                    result.message shouldBe "Public key must have a share in order to complete"
//
//                    // key 3 should be able to complete the challenge
//                    // should generate 3 transactions paying out keys 3,2,1
//                    var completionResult = CompleteChallengeService.execute(
//                        publicKey1,
//                        challenge,
//                        publicKey3
//                    )
//                    completionResult.result shouldBe SOAResultType.SUCCESS
//                    val distributionResults = completionResult.data!!.transactions
//                    distributionResults.count() shouldBe 3
//                    val state = challenge.getLastStateChangeTransaction()!!
//                    state.action.type shouldBe ActionType.COMPLETE
//                }
//            }
//        }
//    }
//}