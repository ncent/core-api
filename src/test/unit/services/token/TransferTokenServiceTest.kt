package test.unit.services.token
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
//import main.services.token.GenerateTokenService
//import main.services.token.TransferTokenService
//import org.jetbrains.exposed.sql.transactions.transaction
//import org.junit.jupiter.api.extension.ExtendWith
//import test.TestHelper
//
//@ExtendWith(MockKExtension::class)
//class TransferTokenServiceTest : WordSpec() {
//    private lateinit var nCentTokenNamespace: TokenNamespace
//
//    override fun beforeTest(description: Description): Unit {
//        Handler.connectAndBuildTables()
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
//        "calling execute with a public key transfer, having sufficient funds" should {
//            "return the transaction generated" {
//                val NewKeyPairs = TestHelper.generateKeyPairs(2)
//
//                transaction {
//                    val token = GenerateTokenService.execute(NewKeyPairs[0].publicKey, nCentTokenNamespace).data!!
//
//                    var result = TransferTokenService.execute(
//                        NewKeyPairs[0].publicKey,
//                        NewKeyPairs[1].publicKey,
//                        5.0,
//                        "nCent")
//                    result.result shouldBe SOAResultType.SUCCESS
//                    val tx = result.data as Transaction
//                    tx.from shouldBe NewKeyPairs[0].publicKey
//                    tx.to shouldBe NewKeyPairs[1].publicKey
//                    tx.action.type shouldBe ActionType.TRANSFER
//                    tx.action.dataType shouldBe Token::class.simpleName!!
//                    tx.action.data shouldBe token.idValue
//                    tx.metadatas.first().value shouldBe "5.0"
//                }
//            }
//        }
//
//        "calling execute with a public key transfer, having insufficient funds" should {
//            "return failure" {
//                val NewKeyPairs = TestHelper.generateKeyPairs(2)
//
//                transaction {
//                    GenerateTokenService.execute(NewKeyPairs[0].publicKey, nCentTokenNamespace)
//
//                    var result = TransferTokenService.execute(
//                        NewKeyPairs[0].publicKey,
//                        NewKeyPairs[1].publicKey,
//                        105.0,
//                        "nCent")
//                    result.result shouldBe SOAResultType.FAILURE
//                    result.message shouldBe "Insufficient funds"
//                }
//            }
//        }
//    }
//}