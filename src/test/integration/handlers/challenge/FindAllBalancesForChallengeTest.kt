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
//class FindAllBalancesForChallengeTest : WordSpec() {
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
//        "Calling the API with the sponsor" should {
//            "should return a valid list of public keys with their balances" {
//                transaction {
//                    map = TestHelper.buildRequest(
//                            key1.publicKey,
//                            "/challenge/balances",
//                            "GET",
//                            null,
//                            mapOf(
//                                Pair("challengeId", challenge.idValue.toString())
//                            )
//                    )
//
//                    val getAllBalancesForChallengeResult = handler.handleRequest(map, contxt)
//                    getAllBalancesForChallengeResult.statusCode shouldBe 200
//
//                    val publicKeyToChallengeBalanceList = JsonHelper.parse<PublicKeyToChallengeBalanceList>(getAllBalancesForChallengeResult.body!!.toString())
//                    var totalBalance = 0
//                    for (key in publicKeyToChallengeBalanceList.publicKeyToChallengeBalances.keys) {
//                        totalBalance += publicKeyToChallengeBalanceList.publicKeyToChallengeBalances[key]!!
//                    }
//
//                    totalBalance shouldBe 100
//                }
//            }
//        }
//
//        "Calling the API with a non-sponsor" should {
//            "should return a 403 forbidden response" {
//                transaction {
//                    map = TestHelper.buildRequest(
//                            key2.publicKey,
//                            "/challenge/balances",
//                            "GET",
//                            null,
//                            mapOf(
//                                Pair("challengeId", challenge.idValue.toString())
//                            )
//                    )
//
//                    val getAllBalancesForChallengeResult = handler.handleRequest(map, contxt)
//                    getAllBalancesForChallengeResult.statusCode shouldBe 403
//                    getAllBalancesForChallengeResult.body shouldBe "Public key not permitted to make this call"
//                }
//            }
//        }
//    }
//}