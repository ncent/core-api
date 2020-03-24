package test.unit.services.challenge
//
//import framework.models.idValue
//import io.kotlintest.*
//import io.kotlintest.specs.WordSpec
//import io.mockk.junit5.MockKExtension
//import org.junit.jupiter.api.extension.ExtendWith
//import main.daos.*
//import framework.models.Handler
//import kotlinserverless.framework.services.SOAResultType
//import main.services.challenge.AddSubChallengeService
//import main.services.challenge.ChangeChallengeStateService
//import org.jetbrains.exposed.sql.transactions.transaction
//import test.TestHelper
//
//@ExtendWith(MockKExtension::class)
//class ChangeChallengeStateServiceTest : WordSpec() {
//    private lateinit var challenge1: Challenge
//    private lateinit var challenge2: Challenge
//    private lateinit var publicKey: String
//
//    override fun beforeTest(description: Description) {
//        Handler.connectAndBuildTables()
//        transaction {
//            val newKeyPairs = TestHelper.generateKeyPairs()
//            publicKey = newKeyPairs[0].publicKey
//            val challenges = TestHelper.generateFullChallenge(publicKey, publicKey,2)
//            challenge1 = challenges[0]
//            challenge2 = challenges[1]
//        }
//    }
//
//    override fun afterTest(description: Description, result: TestResult) {
//        Handler.disconnectAndDropTables()
//    }
//
//    init {
//        "calling execute with a valid state change" should {
//            "allow for the state to be change" {
//                transaction {
//                    // start state is created
//                    // try to change to created and fail
//                    var result = ChangeChallengeStateService.execute(
//                        publicKey,
//                        challenge1.idValue,
//                        ActionType.CREATE
//                    )
//                    result.message shouldBe "Cannot transition from create to create"
//                    result.result shouldBe SOAResultType.FAILURE
//                    // change to active successfully
//                    result = ChangeChallengeStateService.execute(
//                        publicKey,
//                        challenge1.idValue,
//                        ActionType.ACTIVATE
//                    )
//                    result.result shouldBe SOAResultType.SUCCESS
//                    result.data!!.action.type shouldBe ActionType.ACTIVATE
//                    // change to invalid successfully
//                    result = ChangeChallengeStateService.execute(
//                        publicKey,
//                        challenge1.idValue,
//                        ActionType.INVALIDATE
//                    )
//                    result.result shouldBe SOAResultType.SUCCESS
//                    result.data!!.action.type shouldBe ActionType.INVALIDATE
//                    // change to active successfully
//                    result = ChangeChallengeStateService.execute(
//                        publicKey,
//                        challenge1.idValue,
//                        ActionType.ACTIVATE
//                    )
//                    result.result shouldBe SOAResultType.SUCCESS
//                    result.data!!.action.type shouldBe ActionType.ACTIVATE
//                    // change to complete successfully
//                    result = ChangeChallengeStateService.execute(
//                        publicKey,
//                        challenge1.idValue,
//                        ActionType.COMPLETE
//                    )
//                    result.result shouldBe SOAResultType.SUCCESS
//                    result.data!!.action.type shouldBe ActionType.COMPLETE
//
//                    // start state is created
//                    // change to active successfully
//                    result = ChangeChallengeStateService.execute(
//                        publicKey,
//                        challenge2.idValue,
//                        ActionType.ACTIVATE
//                    )
//                    result.result shouldBe SOAResultType.SUCCESS
//                    result.data!!.action.type shouldBe ActionType.ACTIVATE
//                    // change to expired successfully
//                    result = ChangeChallengeStateService.execute(
//                        publicKey,
//                        challenge2.idValue,
//                        ActionType.EXPIRE
//                    )
//                    result.result shouldBe SOAResultType.SUCCESS
//                    result.data!!.action.type shouldBe ActionType.EXPIRE
//                    // change to completed fails
//                    result = ChangeChallengeStateService.execute(
//                        publicKey,
//                        challenge2.idValue,
//                        ActionType.COMPLETE
//                    )
//                    result.message shouldBe "Cannot transition from expire to complete"
//                    result.result shouldBe SOAResultType.FAILURE
//                }
//            }
//        }
//    }
//}