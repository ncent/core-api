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
//import test.TestHelper
//import org.jetbrains.exposed.sql.transactions.transaction
//
//@ExtendWith(MockKExtension::class)
//class ActivateChallengeTest : WordSpec() {
//    private lateinit var handler: Handler
//    private lateinit var contxt: Context
//    private lateinit var newKeyPairs: List<NewKeyPair>
//    private lateinit var key1: NewKeyPair
//    private lateinit var key2: NewKeyPair
//    private lateinit var challenge: Challenge
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
//        }
//    }
//
//    override fun afterTest(description: Description, result: TestResult) {
//        Handler.disconnectAndDropTables()
//    }
//
//    init {
//        "Calling the Activate Challenge API" should {
//            "should return the transaction from the successful activation" {
//                transaction {
//                    map = TestHelper.buildRequest(
//                        key1.publicKey,
//                        "/challenge/activate",
//                        "PUT",
//                        mapOf(
//                            Pair("challengeId", challenge.idValue)
//                        )
//                    )
//
//                    val activateChallengeResult = handler.handleRequest(map, contxt)
//                    activateChallengeResult.statusCode shouldBe 200
//
//                    val transactionNamespace = JsonHelper.parse<TransactionNamespace>(activateChallengeResult.body!! as String)
//                    transactionNamespace.to shouldBe challenge.cryptoKeyPair.publicKey
//                }
//
//            }
//        }
//    }
//}