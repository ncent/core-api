package test.unit.services.challenge
//
//import io.kotlintest.*
//import io.kotlintest.specs.WordSpec
//import io.mockk.junit5.MockKExtension
//import org.junit.jupiter.api.extension.ExtendWith
//import kotlinserverless.framework.services.SOAResultType
//import main.daos.*
//import framework.models.Handler
//import main.services.challenge.GetUnsharedTransactionsService
//import main.services.challenge.ShareAllChallengesService
//import org.jetbrains.exposed.sql.transactions.transaction
//import test.TestHelper
//
//@ExtendWith(MockKExtension::class)
//class ShareAllChallengesServiceTest : WordSpec() {
//    private lateinit var accounts: List<NewKeyPair>
//    private lateinit var sender: NewKeyPair
//    private lateinit var recipient: NewKeyPair
//    private lateinit var foozChallenge: Challenge
//    private lateinit var barChallenge: Challenge
//
//    override fun beforeTest(description: Description) {
//        Handler.connectAndBuildTables()
//        transaction {
//            accounts = TestHelper.generateKeyPairs(2)
//            sender = accounts[0]
//            recipient = accounts[1]
//            foozChallenge = TestHelper.generateChallenge(sender.publicKey,1)[0]
//            barChallenge = TestHelper.generateChallenge(sender.publicKey,1)[0]
//        }
//    }
//
//    override fun afterTest(description: Description, result: TestResult) {
//        Handler.disconnectAndDropTables()
//    }
//
//    init {
//        "calling execute with a valid public key" should {
//            "return transactions for all shares transferred to send address" {
//                transaction {
//                    // Verify that the share transactions were created.
//                    val result = ShareAllChallengesService.execute(
//                            sender.publicKey,
//                            recipient.publicKey)
//                    result.result shouldBe SOAResultType.SUCCESS
//                    result.data!!.first.transactions.count() shouldBe 2
//                    result.data!!.first.transactions[0].action.data shouldBe 2
//                    result.data!!.first.transactions[1].action.data shouldBe 1
//
//                    // Verify that the recipient received the shares.
//                    var unsharedTransactions = GetUnsharedTransactionsService.execute(recipient.publicKey)
//                    unsharedTransactions.data!!.transactionsToShares.count() shouldBe 2
//                    unsharedTransactions.data!!.transactionsToShares[0].shares shouldBe 100
//                    unsharedTransactions.data!!.transactionsToShares[1].shares shouldBe 100
//
//                    // Verify that the sender no longer has any shares.
//                    unsharedTransactions = GetUnsharedTransactionsService.execute(sender.publicKey)
//                    unsharedTransactions.data!!.transactionsToShares.count() shouldBe 0
//                }
//            }
//        }
//    }
//}