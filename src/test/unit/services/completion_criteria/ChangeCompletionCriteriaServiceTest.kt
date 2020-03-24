package test.unit.services.completion_criteria
//
//import framework.models.idValue
//import io.kotlintest.*
//import io.kotlintest.specs.WordSpec
//import io.mockk.junit5.MockKExtension
//import org.junit.jupiter.api.extension.ExtendWith
//import kotlinserverless.framework.services.SOAResultType
//import main.daos.*
//import framework.models.Handler
//import main.services.completion_criteria.ChangeCompletionCriteriaService
//import org.jetbrains.exposed.sql.transactions.transaction
//import test.TestHelper
//
//@ExtendWith(MockKExtension::class)
//class ChangeCompletionCriteriaServiceTest : WordSpec() {
//    private lateinit var publicKey: String
//    private lateinit var publicKey2: String
//    private lateinit var completionCriteria: CompletionCriteria
//
//    override fun beforeTest(description: Description): Unit {
//        Handler.connectAndBuildTables()
//        transaction {
//            val NewKeyPairs = TestHelper.generateKeyPairs(2)
//            publicKey = NewKeyPairs[0].publicKey
//            publicKey2 = NewKeyPairs[1].publicKey
//            TestHelper.buildGenericReward(publicKey)
//            completionCriteria = CompletionCriteria.all().first()
//            completionCriteria.address = publicKey
//        }
//    }
//
//    override fun afterTest(description: Description, result: TestResult) {
//        Handler.disconnectAndDropTables()
//    }
//
//    init {
//        "calling execute" should {
//            "succeed with the correct address passed" {
//                transaction {
//                    val result = ChangeCompletionCriteriaService.execute(
//                        publicKey,
//                        completionCriteria.idValue,
//                        publicKey2
//                    )
//                    result.result shouldBe SOAResultType.SUCCESS
//                    result.data!!.address shouldBe publicKey2
//                }
//            }
//            "fail with the incorrect address passed" {
//                transaction {
//                    val result = ChangeCompletionCriteriaService.execute(
//                        publicKey2,
//                        completionCriteria.idValue,
//                        publicKey2
//                    )
//                    result.result shouldBe SOAResultType.FAILURE
//                }
//            }
//        }
//    }
//}