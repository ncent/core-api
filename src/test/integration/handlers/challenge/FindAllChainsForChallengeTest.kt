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
//class FindAllChainsForChallengeTest : WordSpec() {
//    private lateinit var handler: Handler
//    private lateinit var contxt: Context
//    private lateinit var map: Map<String, Any>
//    private lateinit var NewKeyPairs: List<NewKeyPair>
//    private lateinit var challenge1: Challenge
//
//    override fun beforeTest(description: Description) {
//        Handler.connectAndBuildTables()
//        handler = Handler(true)
//        contxt = mockk()
//        transaction {
//            NewKeyPairs = TestHelper.generateKeyPairs(8)
//            challenge1 = TestHelper.generateChallenge(NewKeyPairs[0].publicKey,1, true)[0]
//            TestHelper.createChainsOfShares(NewKeyPairs, challenge1)
//        }
//    }
//
//    override fun afterTest(description: Description, result: TestResult) {
//        Handler.disconnectAndDropTables()
//    }
//
//    init {
//        "Calling the API with a valid challenge" should {
//            "should return a valid list of chains" {
//                transaction {
//                    map = TestHelper.buildRequest(
//                        NewKeyPairs[0].publicKey,
//                        "/challenge/chains",
//                        "GET",
//                        null,
//                        mapOf(
//                            Pair("challengeId", challenge1.idValue.toString())
//                        )
//                    )
//
//                    val getAllChainsResult = handler.handleRequest(map, contxt)
//                    getAllChainsResult.statusCode shouldBe 200
//
//                    val challenger = JsonHelper.parse<ChallengerNamespace>(getAllChainsResult.body!!.toString())
//                    challenger.challenger shouldBe NewKeyPairs[0].publicKey
//                    challenger.receivers!!.forEachIndexed { index, challenger ->
//                        challenger.challenger shouldBe NewKeyPairs[index + 1].publicKey
//                    }
//                    challenger.receivers!![0].receivers!![0].challenger shouldBe NewKeyPairs[5].publicKey
//                    challenger.receivers!![0].receivers!![1].challenger shouldBe NewKeyPairs[6].publicKey
//
//                    challenger.receivers!![3].receivers!![0].challenger shouldBe NewKeyPairs[7].publicKey
//                }
//            }
//        }
//    }
//}