package main.services.aws.qldb

import com.amazon.ion.IonString
import com.amazon.ion.IonValue
import com.amazonaws.services.qldb.model.ResourceNotFoundException
import framework.models.BaseCryptoObject
import framework.models.Handler
import kotlinserverless.framework.models.InvalidArguments
import kotlinserverless.framework.models.NotFoundException
import main.services.aws.qldb.helpers.LedgerClient
import software.amazon.qldb.TransactionExecutor
import java.io.IOException
import javax.naming.OperationNotSupportedException
import kotlin.reflect.KClass


object LedgerService {
    enum class AndOrOr {
        AND, OR
    }
    val LEDGER_NAME = System.getenv("qldb_ledger_name") ?: "ncent-test"
    private val client = LedgerClient

    @Throws(InterruptedException::class, OperationNotSupportedException::class)
    fun dropLedger(ledgerName: String) {
        println("Drop ledger called -- isTest: ${Handler.TEST}")
        if(!Handler.TEST)
            throw OperationNotSupportedException()
        println("Attempting to drop ledger $ledgerName")
        client.delete(ledgerName)
    }

    @Throws(OperationNotSupportedException::class)
    fun clearLedgerTables() {
        if(!Handler.TEST)
            throw OperationNotSupportedException()
        client.clearTables()
    }

    @Throws(ResourceNotFoundException::class, IOException::class, LedgerClient.TooManyResults::class)
    fun <T: BaseCryptoObject> findBy(
        clazz: KClass<T>,
        txe: TransactionExecutor? = null,
        nestedClazzes: List<KClass<*>> = listOf(),
        keyValues: List<Pair<String, String>>,
        andOrOr: AndOrOr = AndOrOr.AND
    ): T {
        val results = findAllBy(clazz, txe, nestedClazzes, keyValues, andOrOr)
        if(results.count() > 1)
            throw LedgerClient.TooManyResults(1)

        return results.first()
    }

//    fun <T: BaseCryptoObject> findHistoryBy(txe: TransactionExecutor, clazz: KClass<T>, field: String, value: String): List<T> {
//        val tableName = clazz::simpleName
//    }
//


    @Throws(NotFoundException::class, IOException::class)
    fun <T: BaseCryptoObject> findAllBy(
        clazz: KClass<T>,
        txe: TransactionExecutor? = null,
        nestedClazzes: List<KClass<*>> = listOf(),
        keyValues: List<Pair<String, String>> = listOf(),
        andOrOr: AndOrOr = AndOrOr.AND
    ): List<T> {
        val tableName = clazz.simpleName

        println("Querying table: $tableName")
        val query = StringBuilder()
        query.append("SELECT c.* FROM $tableName AS c")
        if(nestedClazzes.any()) {
            val nestedNames = nestedClazzes.map { it.simpleName!!.toLowerCase() }
            // validate values
            if(keyValues.any { keyVal ->
                    !keyVal.first.contains("c.") && !nestedNames.filter { it in keyVal.first }.any()
                }
            )
                throw InvalidArguments("nested queries must have prefix 'c.'  for $tableName or 'c.[${nestedNames.joinToString(", ")}].'")

            nestedClazzes.forEach { nestedClazz ->
                val nestedName = nestedClazz.simpleName!!.toLowerCase()
                println("Using nested class: $nestedClazz")
                query.append(", @c.$nestedName")
            }
        }

        query.append(" WHERE")
        keyValues.forEachIndexed { index, keyValue ->
            println("Using key: ${keyValue.first}, value: ${keyValue.second}")
            if(index != 0)
                query.append(" ${andOrOr.name}")
            query.append(" ${keyValue.first} = ?")
        }

        println("Attempting to query: $query")

        val parameters: List<IonValue> = keyValues.map { client.MAPPER.writeValueAsIonValue(it.second) }
        val results = if(txe != null) {
            client.toIonStructs(txe.execute(query.toString(), parameters))
        } else {
            transaction {
                client.toIonStructs(it.execute(query.toString(), parameters))
            }
        }

        println("Found (${results.count()})")

        if(results.isEmpty())
            throw NotFoundException("Query produced no results")

        return results.map { client.MAPPER.parse(it, clazz.java) }.sortedBy { it.createdAt }
    }
//
//    fun <T: BaseCryptoObject> findAllHistoryBy(txe: TransactionExecutor, clazz: KClass<T>, field: String, value: String): List<List<T>> {
//        val tableName = clazz::simpleName
//
//    }

    fun <T: BaseCryptoObject> insert(clazz: KClass<T>, data: T, txe: TransactionExecutor? = null): String {
        val tableName = clazz.simpleName

        println("Inserting into: '${tableName}'...")
        try {
            val query = "INSERT INTO $tableName ?"
            val params: List<IonValue> = listOf(client.MAPPER.writeValueAsIonValue(data))
            println("Inserting params $params with query $query")
            val result = if(txe == null) {
                transaction {
                    client.toIonStructs(it.execute(query, params))
                }
            } else {
                client.toIonStructs(txe.execute(query, params))
            }

            println("Successfully executed query, result: $result...")
            val documentId = (result.first().get("documentId") as IonString).stringValue()
            println("document id inserted $documentId, adding to document.")
            return documentId
        } catch (ioe: IOException) {
            throw IllegalStateException(ioe)
        } catch (t: Throwable) {
            println("There was an error inserting a document: ${t.message}")
            throw t
        }
    }

    fun <T> transaction(query: (txe: TransactionExecutor) -> T): T {
        var result: T? = null
        val session = client.session()
        session.execute { txe ->
            result = query(txe)
        }
        /**
         * No idea why this is required twice, but it breaks quietly
         * with one and does not end up closing the session. Ends up
         * not having enough sessions once you get to the limit
          */

        session.close()
        session.close()
        return result!!
    }
}