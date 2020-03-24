package test.integration.handlers.challenge

//import com.amazonaws.services.lambda.runtime.Context
//import framework.models.idValue
//import io.kotlintest.specs.WordSpec
//import io.mockk.junit5.MockKExtension
//import org.junit.jupiter.api.extension.ExtendWith
//import io.kotlintest.Description
//import io.kotlintest.shouldBe
//import io.kotlintest.TestResult
//import framework.models.Handler
//import io.mockk.mockk
//import main.daos.*
//import main.helpers.JsonHelper
//import main.services.challenge.ActivateChallengeService
//import main.services.challenge.ShareChallengeService
//import test.TestHelper
//import org.jetbrains.exposed.sql.transactions.transaction
//
//@ExtendWith(MockKExtension::class)
//class CompleteChallengeTest : WordSpec() {
//    private lateinit var handler: Handler
//    private lateinit var contxt: Context
//    private lateinit var newKeyPairs: List<NewKeyPair>
//    private lateinit var key1: NewKeyPair
//    private lateinit var key2: NewKeyPair
//    private lateinit var challenge: Challenge
//    private lateinit var notActivatedChallenge: Challenge
//    private lateinit var map: Map<String, Any>
//
//    override fun beforeTest(description: Description) {
//        Handler.connectAndBuildTables()
//        handler = Handler(true)
//        contxt = mockk()
//        transaction {
//            newKeyPairs = TestHelper.generateKeyPairs(2)
//            key1 = newKeyPairs[0]
//            key2 = newKeyPairs[1]
//            challenge = TestHelper.generateFullChallenge(key1.publicKey, key1.publicKey, 1, true).first()
//            notActivatedChallenge = TestHelper.generateFullChallenge(key1.publicKey, key1.publicKey, 1, true).first()
//            ActivateChallengeService.execute(key1.publicKey, challenge.idValue)
//            ShareChallengeService.execute(key1.publicKey, challenge, 2, key2.publicKey)
//            ShareChallengeService.execute(key1.publicKey, notActivatedChallenge, 2, key2.publicKey)
//        }
//    }
//
//    override fun afterTest(description: Description, result: TestResult) {
//        Handler.disconnectAndDropTables()
//    }
//
//    init {
//        "Calling the Complete Challenge API" should {
//            "should return the list of reward distribution transactions when passed valid parameters" {
//                transaction {
//                    map = TestHelper.buildRequest(
//                        key1.publicKey,
//                        "/challenge/complete",
//                        "PATCH",
//                        mapOf(
//                            Pair("challengeId", challenge.idValue),
//                            Pair("completerPublicKey", key2.publicKey)
//                        )
//                    )
//
//                    val completeChallengeResult = handler.handleRequest(map, contxt)
//                    completeChallengeResult.statusCode shouldBe 200
//
//                    val transactionNamespaceList = JsonHelper.parse<TransactionNamespaceList>(completeChallengeResult.body!!.toString())
//                    transactionNamespaceList.transactions.size shouldBe 2
//                }
//
//            }
//            "should return a 403 forbidden response when attempted by a public key other than the sponsor's" {
//                transaction {
//                    map = TestHelper.buildRequest(
//                        key2.publicKey,
//                        "/challenge/complete",
//                        "PATCH",
//                        mapOf(
//                            Pair("challengeId", challenge.idValue),
//                            Pair("completerPublicKey", key2.publicKey)
//                        )
//                    )
//
//                    val completeChallengeResult = handler.handleRequest(map, contxt)
//                    completeChallengeResult.statusCode shouldBe 403
//                    completeChallengeResult.body shouldBe "This public key cannot change the challenge state"
//                }
//            }
//            "should return a 403 forbidden response when challenge state cannot change to complete" {
//                transaction {
//                    map = TestHelper.buildRequest(
//                        key1.publicKey,
//                        "/challenge/complete",
//                        "PATCH",
//                        mapOf(
//                            Pair("challengeId", notActivatedChallenge.idValue),
//                            Pair("completerPublicKey", key2.publicKey)
//                        )
//                    )
//
//                    val completeChallengeResult = handler.handleRequest(map, contxt)
//                    completeChallengeResult.statusCode shouldBe 403
//                    completeChallengeResult.body shouldBe "Cannot transition from create to complete"
//                }
//            }
//        }
//
//        "Calling the Redeem Challenge API" should {
//            "should return the list of reward distribution transactions when passed valid parameters" {
//                transaction {
//                    map = TestHelper.buildRequest(
//                        key1.publicKey,
//                        "/challenge/redeem",
//                        "PATCH",
//                        mapOf(
//                            Pair("challengeId", challenge.idValue),
//                            Pair("completerPublicKey", key2.publicKey)
//                        )
//                    )
//
//                    val redeemChallengeResult = handler.handleRequest(map, contxt)
//                    redeemChallengeResult.statusCode shouldBe 200
//
//                    val transactionNamespaceList = JsonHelper.parse<TransactionNamespaceList>(redeemChallengeResult.body!!.toString())
//                    transactionNamespaceList.transactions.size shouldBe 2
//                }
//
//            }
//            "should return a 403 forbidden response when attempted by a public key other than the sponsor's" {
//                transaction {
//                    map = TestHelper.buildRequest(
//                        key2.publicKey,
//                        "/challenge/complete",
//                        "PATCH",
//                        mapOf(
//                            Pair("challengeId", challenge.idValue),
//                            Pair("completerPublicKey", key2.publicKey)
//                        )
//                    )
//
//
//                    val redeemChallengeResult = handler.handleRequest(map, contxt)
//                    redeemChallengeResult.statusCode shouldBe 403
//                    redeemChallengeResult.body shouldBe "This public key cannot change the challenge state"
//                }
//            }
//            "should return a 403 forbidden response when challenge state cannot change to complete" {
//                transaction {
//                    map = TestHelper.buildRequest(
//                        key1.publicKey,
//                        "/challenge/redeem",
//                        "PATCH",
//                        mapOf(
//                            Pair("challengeId", notActivatedChallenge.idValue),
//                            Pair("completerPublicKey", key2.publicKey)
//                        )
//                    )
//
//                    val redeemChallengeResult = handler.handleRequest(map, contxt)
//                    redeemChallengeResult.body shouldBe "Challenge has not been activated"
//                }
//            }
//        }
//    }
//}