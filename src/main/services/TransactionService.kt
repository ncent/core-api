package main.services

import kotlinserverless.framework.models.NotFoundException
import main.daos.ActionType
import main.daos.Challenge
import main.daos.NAction
import main.daos.NTransaction
import main.services.aws.qldb.LedgerService
import software.amazon.qldb.TransactionExecutor

object TransactionService {
    fun create(transaction: NTransaction, context: TransactionExecutor? = null) : NTransaction {
        val documentId = LedgerService.insert(
            clazz = NTransaction::class,
            data = transaction,
            txe = context
        )
        transaction.documentId = documentId
        return transaction
    }

    fun findByHashCode(hashCode: String, context: TransactionExecutor? = null): NTransaction {
        return LedgerService.findBy(
            clazz = NTransaction::class,
            txe = context,
            keyValues = listOf(Pair("hashCode", hashCode))
        )
    }

    fun findByInboundPublicKey(publicKey: String, context: TransactionExecutor? = null): List<NTransaction> {
        return LedgerService.findAllBy(
            clazz = NTransaction::class,
            txe = context,
            keyValues = listOf(Pair("inbound", publicKey))
        )
    }

    fun findByPublicKeyAndAction(
        inbound: String? = null,
        outbound: String? = null,
        action: NAction? = null,
        context: TransactionExecutor? = null
    ): List<NTransaction> {
        val keyValues = mutableListOf<Pair<String, String>>()
        if(inbound != null)
            keyValues.add(Pair("c.inbound", inbound))
        if(outbound != null)
            keyValues.add(Pair("c.outbound", outbound))
        if(action?.dataKey != null)
            keyValues.add(Pair("c.naction.dataKey", action.dataKey))
        if(action != null) {
            keyValues.add(Pair("c.naction.dataType", action.dataType))
            keyValues.add(Pair("c.naction.type", action.type.name))
        }

        return try {
            LedgerService.findAllBy(
                clazz = NTransaction::class,
                txe = context,
                nestedClazzes = listOf(NAction::class),
                keyValues = keyValues
            )
        } catch(e: NotFoundException) {
            listOf()
        }
    }
}