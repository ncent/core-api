package main.services.aws.qldb.helpers

import com.amazon.ion.IonString
import com.amazon.ion.IonStruct
import com.amazon.ion.IonValue
import com.amazon.ion.system.IonSystemBuilder
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.qldb.AmazonQLDB
import com.amazonaws.services.qldb.AmazonQLDBClientBuilder
import com.amazonaws.services.qldb.model.*
import com.amazonaws.services.qldbsession.AmazonQLDBSessionClientBuilder
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.ion.ionvalue.IonValueMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import framework.models.Handler
import main.daos.Challenge
import main.daos.NTransaction
import main.daos.Token
import main.services.aws.qldb.LedgerService
import software.amazon.qldb.PooledQldbDriver
import software.amazon.qldb.QldbSession
import software.amazon.qldb.Result
import software.amazon.qldb.TransactionExecutor
import software.amazon.qldb.exceptions.QldbClientException


internal object LedgerClient {
    private val driver: PooledQldbDriver
    private val client: AmazonQLDB
    private val credentialsProvider = AWSStaticCredentialsProvider(
        BasicAWSCredentials(
            System.getProperty("access_key_id") ?: System.getenv("access_key_id")!!,
            System.getProperty("secret_key_id") ?: System.getenv("secret_key_id")!!
        )
    )

    var MAPPER: IonValueMapper = IonValueMapper(
        IonSystemBuilder.standard().build()
    ).registerModule(KotlinModule()) as IonValueMapper

    private const val LEDGER_POLL_PERIOD_MS = 1_000L
    private const val RETRY_LIMIT = 10

    private val TABLES_AND_INDEX = listOf(
        TableWithIndex(
            Challenge::class.simpleName!!,
            "publicKey"
        ),
        TableWithIndex(
            Token::class.simpleName!!,
            "publicKey"
        ),
        TableWithIndex(
            NTransaction::class.simpleName!!,
            "outbound",
            "inbound",
            "hashCode",
            "previousTransaction"
        )
    )

    private class TableWithIndex(val tableName: String, vararg val indexes: String)

    init {
        try {
            MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

            client = try {
                println("Initializing Ledger Client")
                val clientBuilder = AmazonQLDBClientBuilder
                    .standard()
                    .withCredentials(credentialsProvider)
                    .withRegion(System.getenv("region") ?: "us-west-2")
                clientBuilder.build()
            } catch (e: QldbClientException) {
                System.err.println("Failed to initialize ledger client: ${e.message}")
                throw e
            }

            driver = try {
                println("Initializing Ledger Client Driver")
                val builder = AmazonQLDBSessionClientBuilder
                    .standard()
                    .withCredentials(credentialsProvider)
                    .withRegion(System.getenv("region") ?: "us-west-2")
                PooledQldbDriver.builder()
                    .withLedger(LedgerService.LEDGER_NAME)
                    .withRetryLimit(RETRY_LIMIT)
                    .withSessionClientBuilder(builder)
                    .build()
            } catch (e: QldbClientException) {
                System.err.println("Failed to initialize ledger client driver: ${e.message}")
                throw e
            }
//            if you wish to alter a table, you must remake the db on all ledgers needed.
//            delete(LedgerService.LEDGER_NAME)
//            create(LedgerService.LEDGER_NAME)
//            initTablesAndIndexes(driver.session)
//            waitForTableActivation(TABLES_AND_INDEX.last().tableName)
//            sleepUntilState(LedgerService.LEDGER_NAME, LedgerState.ACTIVE)

            println("Initializing class: ${LedgerClient::class.simpleName}")
            initializeDriver()

            try {
                describe(LedgerService.LEDGER_NAME)
            } catch (e: ResourceNotFoundException) {
                System.err.println("The Ledger ${LedgerService.LEDGER_NAME} does not exist. Creating...")
                create(LedgerService.LEDGER_NAME)
                initTablesAndIndexes(driver.session)
                waitForTableActivation(TABLES_AND_INDEX.last().tableName)
                sleepUntilState(LedgerService.LEDGER_NAME, LedgerState.ACTIVE)
            }
        } catch(e: Exception) {
            System.err.println(e.message)
            System.err.println(e.cause?.message)
            throw e
        }
    }

    private fun initializeDriver(): QldbSession {
        println("Driver initialized: $driver")
        var sess: QldbSession? = null
        while(sess == null) {
            try {
                println("Initializing Session...")
                sess = session()
                println("Got session $sess")
            } catch(e: Throwable) {
                println("Could not get session: ${e.message}")
                Thread.sleep(LEDGER_POLL_PERIOD_MS)
            }
        }
        println("Session initialized: $sess")
        return sess
    }

    fun session(): QldbSession {
        return driver.session
    }

    @Throws(ResourceNotFoundException::class)
    fun describe(ledgerName: String): DescribeLedgerResult {
        println("Let's describe ledger with name: '$ledgerName'...")
        val request = DescribeLedgerRequest().withName(ledgerName)
        val result = client.describeLedger(request)
        println("Success. Ledger description: $result")
        return result
    }

    @Throws(InterruptedException::class)
    private fun create(ledgerName: String): CreateLedgerResult {
        println("Let's create the ledger with name: '$ledgerName'...")
        val request = CreateLedgerRequest()
            .withName(ledgerName)
            .withPermissionsMode(PermissionsMode.ALLOW_ALL)
            .withDeletionProtection(!Handler.TEST)
        val result = client.createLedger(request)
        println("Success. Ledger state: '${result.state}'.")
        sleepUntilState(ledgerName, LedgerState.ACTIVE)
        return result
    }

    @Throws(InterruptedException::class)
    fun removeDeleteProtection(ledgerName: String): UpdateLedgerResult {
        println("Let's remove delete protection from: '$ledgerName'...")
        val request = UpdateLedgerRequest()
            .withName(ledgerName)
            .withDeletionProtection(false)
        val result = client.updateLedger(request)
        println("Success. Ledger state: '$result.state'.")
        sleepUntilState(ledgerName, LedgerState.ACTIVE)
        return result
    }

    @Throws(InterruptedException::class)
    fun delete(ledgerName: String, retryCount: Int = 0): DeleteLedgerResult? {
        println("Let's delete the ledger with name: '$ledgerName'...")
        val request = DeleteLedgerRequest()
            .withName(ledgerName)
        val result = try {
            val result = client.deleteLedger(request)
            sleepUntilState(ledgerName, LedgerState.DELETED)
            result
        } catch (e: ResourceNotFoundException) {
            System.err.println("The ledger is already deleted.")
            null
        } catch (e: Exception) {
            System.err.println("Failed to delete ledger: ${e.message}, retryCount: $retryCount")
            if(retryCount == 0) {
                println("Attempting to remove delete protection")
                removeDeleteProtection(ledgerName)
                println("Attempting to delete again.")
                return delete(ledgerName, 1)
            }
            throw e
        }
        println("Success. Ledger deleted: ${result?.sdkResponseMetadata}.")
        return result
    }

    private fun initTablesAndIndexes(session: QldbSession) {
        println("Attempting to initialize tables and indexes using session: $session")
        session.execute { txe ->
            TABLES_AND_INDEX.forEach { ti ->
                createTableAndIndexes(txe, ti)
            }
        }
    }

    fun clearTables() {
        println("Clearing tables...")
        try {
            LedgerService.transaction { txe ->
                TABLES_AND_INDEX.forEach { ti ->
                    println("Clearing table...${ti.tableName}")
                    val query = "DELETE FROM ${ti.tableName}"
                    val result = txe.execute(query)
                    println("Deleted: ${result.count()} in ${ti.tableName}...")
                }
            }
        } catch(e: Exception) {
            clearTablesSlow()
        }
    }

    // Need to use this when the documents grow to over 40
    private fun clearTablesSlow() {
        println("Clearing tables...")
        try {
            TABLES_AND_INDEX.forEach { ti ->
                println("Clearing table...${ti.tableName}")
                val results =
                    LedgerService.transaction { txe ->
                        toIonStructs(txe.execute("SELECT hashCode FROM ${ti.tableName}"))
                    }
                results.forEach { result ->
                    val query = "DELETE FROM ${ti.tableName} WHERE hashCode = ?"
                    val parameters: List<IonValue> = listOf(MAPPER.writeValueAsIonValue(result.get("hashCode")))
                    val r =
                        LedgerService.transaction { txe ->
                            toIonStructs(txe.execute(query, parameters))
                        }

                    println("Successfully executed query, result: $r...")
                    val documentId = (r.first().get("documentId") as IonString).stringValue()
                    println("Deleted: $documentId in ${ti.tableName}...")
                }
            }
        } catch(e: Exception) {
            clearTablesSlow()
        }
    }

    private fun createTableAndIndexes(txe: TransactionExecutor, tableAndIndexes: TableWithIndex) {
        createTable(txe, tableAndIndexes.tableName)
        createIndexes(txe, tableAndIndexes.tableName, tableAndIndexes.indexes.asList())
    }

    private fun createTable(txe: TransactionExecutor, tableName: String) {
        println("Creating the '${tableName}' table...")
        val createTable = String.format("CREATE TABLE %s", tableName)
        val result = toIonStructs(txe.execute(createTable))
        println("'${tableName}' table created successfully. $result")
    }

    private fun createIndexes(txe: TransactionExecutor, tableName: String, indexes: List<String>) {
        indexes.forEach { index ->
            println("Creating the '${index}' index...")
            val createIndex = String.format("CREATE INDEX ON %s (%s)", tableName, index);
            txe.execute(createIndex);
            println("'${index}' index created successfully.")
        }
    }

    private fun waitForTableActivation(tableName: String) {
        println("Waiting for $tableName table to activate...")
        while(true) {
            try {
                session().execute { txe ->
                    txe.execute("SELECT * FROM $tableName")
                }
                break
            } catch(e: Exception) {
                println("The table failed to become available... ${e.message}")
                Thread.sleep(LEDGER_POLL_PERIOD_MS)
            }
        }
        println("Table $tableName activated!")
    }

    private fun sleepUntilState(ledgerName: String, ledgerState: LedgerState) {
        println("Waiting for ledger to become ${ledgerState.name}...")
        while (describe(ledgerName).state != ledgerState.name) {
            println("The ledger is still ${ledgerState.name}. Please wait...")
            Thread.sleep(LEDGER_POLL_PERIOD_MS)
        }
        println("Ledger successfully changed state to ${ledgerState.name}...")
    }

    fun toIonStructs(result: Result): List<IonStruct> {
        return result.map { row -> row as IonStruct }
    }

    class TooManyResults(expectedCount: Int):
        Exception(String.format("Too many results found. Expecting %s", expectedCount))
}