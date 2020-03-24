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
//import main.services.challenge.GetUnsharedTransactionsService
//import org.jetbrains.exposed.sql.transactions.transaction
//import test.TestHelper
//
//@ExtendWith(MockKExtension::class)
//class GetUnsharedTransactionsServiceTest : WordSpec() {
//    private lateinit var NewKeyPairs: List<NewKeyPair>
//    private lateinit var challenge1: Challenge
//    private lateinit var challenge2: Challenge
//
//    override fun beforeTest(description: Description) {
//        Handler.connectAndBuildTables()
//        transaction {
//            NewKeyPairs = TestHelper.generateKeyPairs(2)
//            challenge1 = TestHelper.generateChallenge(NewKeyPairs[0].publicKey,1)[0]
//            challenge2 = TestHelper.generateChallenge(NewKeyPairs[0].publicKey,1)[0]
//        }
//    }
//
//    override fun afterTest(description: Description, result: TestResult) {
//        Handler.disconnectAndDropTables()
//    }
//
//    init {
//        "calling execute with a valid challenge" should {
//            "return the public keys unshared transactions" {
//                transaction {
//                    var result = GetUnsharedTransactionsService.execute(
//                        NewKeyPairs[0].publicKey,
//                        challenge1.idValue
//                    )
//                    result.result shouldBe SOAResultType.SUCCESS
//                    result.data!!.transactionsToShares.count() shouldBe 1
//                    result.data!!.transactionsToShares.first().shares shouldBe 100
//                    TestHelper.generateShareTransaction(
//                        challenge1,
//                        NewKeyPairs[0].publicKey,
//                        NewKeyPairs[1].publicKey,
//                        result.data!!.transactionsToShares.first().transaction,
//                        60
//                    )
//                    result = GetUnsharedTransactionsService.execute(
//                        NewKeyPairs[0].publicKey,
//                        challenge1.idValue
//                    )
//                    result.result shouldBe SOAResultType.SUCCESS
//                    result.data!!.transactionsToShares.count() shouldBe 1
//                    result.data!!.transactionsToShares.first().shares shouldBe 40
//                }
//            }
//        }
//
//        "calling execute without specifying a challenge id" should {
//            "return the public keys unshared transactions for all challenges" {
//                transaction {
//                    var result = GetUnsharedTransactionsService.execute(
//                        NewKeyPairs[0].publicKey
//                    )
//                    result.result shouldBe SOAResultType.SUCCESS
//                    result.data!!.transactionsToShares.count() shouldBe 2
//                    result.data!!.transactionsToShares[0].shares shouldBe 100
//                    result.data!!.transactionsToShares[1].shares shouldBe 100
//                    TestHelper.generateShareTransaction(
//                        challenge1,
//                        NewKeyPairs[0].publicKey,
//                        NewKeyPairs[1].publicKey,
//                        result.data!!.transactionsToShares[0].transaction,
//                        60
//                    )
//                    TestHelper.generateShareTransaction(
//                        challenge1,
//                        NewKeyPairs[0].publicKey,
//                        NewKeyPairs[1].publicKey,
//                        result.data!!.transactionsToShares[1].transaction,
//                        50
//                    )
//
//                    // Test that shares were sent successfully and balances are reflected.
//                    result = GetUnsharedTransactionsService.execute(
//                        NewKeyPairs[0].publicKey
//                    )
//                    result.result shouldBe SOAResultType.SUCCESS
//                    result.data!!.transactionsToShares.count() shouldBe 2
//                    result.data!!.transactionsToShares[0].shares shouldBe 40
//                    result.data!!.transactionsToShares[1].shares shouldBe 50
//                }
//            }
//        }
//    }
//}