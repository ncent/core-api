package test.unit.services.transaction

import io.kotlintest.*
import io.kotlintest.specs.WordSpec
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith
import main.daos.*
import framework.models.Handler
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import kotlinserverless.framework.models.NotFoundException
import main.services.TransactionService

@ExtendWith(MockKExtension::class)
class GetTransactionServiceTest : WordSpec() {
    private lateinit var transaction: NTransaction
    private lateinit var transaction2: NTransaction

    override fun beforeTest(description: Description) {
        Handler.TEST = true
        Handler.clearLedger()
        transaction = NTransaction(
            outbound = "ARYA",
            inbound = "RODRIGO",
            naction = NAction(
                type = ActionType.CREATE,
                dataKey = "1",
                dataType = "publicKey"
            ),
            previousTransaction = null,
            metadatas = Metadatas(
                mapOf(Pair("city", "san carlos"), Pair("state", "california"))
            )
        )
        transaction = TransactionService.create(transaction)
    }

    init {
        "calling execute with a valid transaction hashcode" should {
            "return the transaction and associated objects" {
                transaction2 = NTransaction(
                    outbound = "ARYA2",
                    inbound = "ARYA",
                    naction = NAction(
                        type = ActionType.CREATE,
                        dataKey = "2",
                        dataType = "publicKey"
                    ),
                    previousTransaction = transaction.hashCode,
                    metadatas = Metadatas(
                        mapOf(Pair("city", "san carlos"), Pair("state", "california"))
                    )
                )
                val txGenerateResult = TransactionService.create(transaction2)
                txGenerateResult.documentId shouldNotBe null

                val foundNewTx = TransactionService.findByHashCode(txGenerateResult.hashCode!!)
                foundNewTx.previousTransaction shouldBe transaction.hashCode
                foundNewTx.naction.dataKey shouldBe "2"
            }
        }

        "calling execute with a valid inbound public key" should {
            "return the transactions" {
                transaction2 = NTransaction(
                    outbound = "ARYA2",
                    inbound = "RODRIGO",
                    naction = NAction(
                        type = ActionType.CREATE,
                        dataKey = "3",
                        dataType = "publicKey"
                    ),
                    previousTransaction = transaction.hashCode,
                    metadatas = Metadatas(
                        mapOf(Pair("city", "san carlos"), Pair("state", "california"))
                    )
                )
                val txGenerateResult = TransactionService.create(transaction2)
                txGenerateResult.documentId shouldNotBe null

                val foundTxs = TransactionService.findByInboundPublicKey("RODRIGO")
                foundTxs.count() shouldBe 2
                foundTxs.map { it.outbound }.shouldContainExactlyInAnyOrder(listOf("ARYA", "ARYA2"))
            }
        }

        "calling execute with an invalid inbound public key" should {
            "return an error" {
                shouldThrow<NotFoundException> {
                    TransactionService.findByHashCode("BADHASHCODE")
                }
                shouldThrow<NotFoundException> {
                    TransactionService.findByInboundPublicKey("BADPUBLICKEY")
                }
            }
        }
    }
}