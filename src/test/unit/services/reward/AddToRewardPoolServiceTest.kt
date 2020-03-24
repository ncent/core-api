package test.unit.services.reward
//
//import framework.models.idValue
//import io.kotlintest.Description
//import io.kotlintest.TestResult
//import io.kotlintest.shouldBe
//import io.kotlintest.specs.WordSpec
//import io.mockk.junit5.MockKExtension
//import framework.models.Handler
//import kotlinserverless.framework.services.SOAResultType
//import main.daos.*
//import main.services.reward.AddToRewardPoolService
//import main.services.reward.GenerateRewardService
//import main.services.token.GenerateTokenService
//import org.jetbrains.exposed.sql.transactions.transaction
//import org.junit.jupiter.api.extension.ExtendWith
//import test.TestHelper
//
//@ExtendWith(MockKExtension::class)
//class AddToRewardPoolServiceTest : WordSpec() {
//    private lateinit var rewardNamespace: RewardNamespace
//    private lateinit var nCentTokenNamespace: TokenNamespace
//
//    override fun beforeTest(description: Description): Unit {
//        Handler.connectAndBuildTables()
//        rewardNamespace = RewardNamespace(
//            type = RewardTypeNamespace(
//                audience = Audience.FULL,
//                type = RewardTypeName.EVEN
//            ),
//            metadatas = arrayOf(MetadatasNamespace("title", "reward everyone"))
//        )
//        nCentTokenNamespace = TokenNamespace(
//            amount = 100,
//            tokenType = TokenTypeNamespace(
//                id = null,
//                name = "nCent",
//                parentToken = null,
//                parentTokenConversionRate = null
//            )
//        )
//    }
//
//    override fun afterTest(description: Description, result: TestResult) {
//        Handler.disconnectAndDropTables()
//    }
//
//    init {
//        "calling execute with a valid transfer" should {
//            "generate a transaction transfering to the pool" {
//                transaction {
//                    val accounts = TestHelper.generateKeyPairs()
//                    val token = GenerateTokenService.execute(accounts[0].publicKey, nCentTokenNamespace).data!!
//                    var reward = GenerateRewardService.execute(rewardNamespace).data!!
//                    val result = AddToRewardPoolService.execute(
//                        accounts[0].publicKey, reward.idValue, "nCent", 10.0
//                    )
//                    result.result shouldBe SOAResultType.SUCCESS
//
//                    val transaction = result.data!!
//                    transaction.action.type shouldBe ActionType.TRANSFER
//                    transaction.from shouldBe accounts[0].publicKey
//                    transaction.to shouldBe reward.pool.cryptoKeyPair.publicKey
//                    transaction.metadatas.first().value shouldBe "10.0"
//                    transaction.action.data shouldBe token.idValue
//                }
//            }
//        }
//    }
//}
