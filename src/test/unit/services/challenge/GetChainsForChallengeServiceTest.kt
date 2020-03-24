package test.unit.services.challenge
//
//import framework.models.idValue
//import io.kotlintest.*
//import io.kotlintest.specs.WordSpec
//import io.mockk.junit5.MockKExtension
//import org.junit.jupiter.api.extension.ExtendWith
//import kotlinserverless.framework.services.SOAResultType
//import main.daos.*
//import framework.models.Handler
//import main.services.challenge.GetChainsForChallengeService
//import main.services.challenge.GetUnsharedTransactionsService
//import main.services.challenge.ShareChallengeService
//import org.jetbrains.exposed.sql.transactions.transaction
//import test.TestHelper
//
//@ExtendWith(MockKExtension::class)
//class GetChainsForChallengeServiceTest : WordSpec() {
//    private lateinit var NewKeyPairs: List<NewKeyPair>
//    private lateinit var challenge1: Challenge
//    private lateinit var challengerGraph: Challenger
//
//    override fun beforeTest(description: Description) {
//        Handler.connectAndBuildTables()
//        transaction {
//            NewKeyPairs = TestHelper.generateKeyPairs(8)
//            challenge1 = TestHelper.generateChallenge(NewKeyPairs[0].publicKey,1, true)[0]
//            challengerGraph = TestHelper.createChainsOfShares(NewKeyPairs, challenge1)
//        }
//    }
//
//    override fun afterTest(description: Description, result: TestResult) {
//        Handler.disconnectAndDropTables()
//    }
//
//    init {
//        "calling execute with a valid challenge" should {
//            "return the chains of emails" {
//                transaction {
//                    val chainsResult = GetChainsForChallengeService.execute(
//                        NewKeyPairs[0].publicKey,
//                        challenge1.idValue
//                    )
//
//                    chainsResult.result shouldBe SOAResultType.SUCCESS
//                    chainsResult.data!!.challenger shouldBe NewKeyPairs[0].publicKey
//                    chainsResult.data!!.receivers!!.forEachIndexed { index, challenger ->
//                        challenger.challenger shouldBe NewKeyPairs[index + 1].publicKey
//                    }
//                    chainsResult.data!!.receivers!![0].receivers!![0].challenger shouldBe NewKeyPairs[5].publicKey
//                    chainsResult.data!!.receivers!![0].receivers!![1].challenger shouldBe NewKeyPairs[6].publicKey
//
//                    chainsResult.data!!.receivers!![3].receivers!![0].challenger shouldBe NewKeyPairs[7].publicKey
//                }
//            }
//        }
//    }
//}