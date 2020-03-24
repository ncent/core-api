package test.unit.services.transaction

import io.kotlintest.*
import io.kotlintest.specs.WordSpec
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith
import main.daos.*
import framework.models.Handler
import main.services.TransactionService

@ExtendWith(MockKExtension::class)
class GenerateTransactionServiceTest : WordSpec() {
    private lateinit var transaction: NTransaction

    override fun beforeTest(description: Description) {
        Handler.TEST = true
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
    }

    init {
        "calling execute with a valid transaction" should {
            "generate the transaction and associated action" {
                var result = TransactionService.create(transaction)
                result.documentId shouldNotBe null
            }
        }
    }
}