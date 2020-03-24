package main.services.aws.qldb.helpers

import com.amazon.ion.*
import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionhash.IonHashReaderBuilder
import com.amazon.ionhash.MessageDigestIonHasherProvider
import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.dataformat.ion.IonTimestampSerializers.IonTimestampJavaDateSerializer
import org.slf4j.LoggerFactory
import java.io.IOException
import java.math.BigDecimal
import java.time.ZoneId
import java.util.*


/**
 * Represents a QldbRevision including both user data and metadata.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class QldbRevision @JsonCreator constructor(
    blockAddress: BlockAddress,
    metadata: Metadata,
    hash: ByteArray,
    data: IonStruct
) {
    /**
     * Represents the metadata field of a QLDB Document
     */
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    class Metadata @JsonCreator constructor(
        /**
         * Gets the unique ID of a QLDB document.
         *
         * @return the document ID.
         */
        val id: String,
        /**
         * Gets the version number of the document in the document's modification history.
         * @return the version number.
         */
        val version: Long,
        /**
         * Gets the time during which the document was modified.
         *
         * @return the transaction time.
         */
        @field:JsonSerialize(using = IonTimestampJavaDateSerializer::class) val txTime: Date,
        /**
         * Gets the transaction ID associated with this document.
         *
         * @return the transaction ID.
         */
        val txId: String
    ) {

        /**
         * Converts a [Metadata] object to a string.
         *
         * @return the string representation of the [QldbRevision] object.
         */
        override fun toString(): String {
            return ("Metadata{"
                    + "id='" + id + '\''
                    + ", version=" + version
                    + ", txTime=" + txTime
                    + ", txId='" + txId
                    + '\''
                    + '}')
        }

        /**
         * Check whether two [Metadata] objects are equivalent.
         *
         * @return `true` if the two objects are equal, `false` otherwise.
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other == null || javaClass != other.javaClass) {
                return false
            }
            val metadata = other as Metadata
            return version == metadata.version && id == metadata.id && txTime == metadata.txTime && txId == metadata.txId
        }

        /**
         * Generate a hash code for the [Metadata] object.
         *
         * @return the hash code.
         */
        override fun hashCode(): Int { // CHECKSTYLE:OFF - Disabling as we are generating a hashCode of multiple properties.
            return Objects.hash(id, version, txTime, txId)
            // CHECKSTYLE:ON
        }

        companion object {
            fun fromIon(ionStruct: IonStruct?): Metadata {
                if (ionStruct == null) {
                    throw IllegalArgumentException("Metadata cannot be null")
                }
                return try {
                    val id = ionStruct["id"] as IonString
                    val version = ionStruct["version"] as IonInt
                    val txTime = ionStruct["txTime"] as IonTimestamp
                    val txId = ionStruct["txId"] as IonString
                    Metadata(
                        id.stringValue(),
                        version.longValue(),
                        Date(txTime.millis),
                        txId.stringValue()
                    )
                } catch (e: ClassCastException) {
                    log.error("Failed to parse ion document")
                    throw IllegalArgumentException("Document members are not of the correct type", e)
                }
            }
        }

    }

    private val blockAddress: BlockAddress
    /**
     * Gets the metadata of the revision.
     *
     * @return the [Metadata] object.
     */
    val metadata: Metadata
    /**
     * Gets the SHA-256 hash value of the data.
     *
     * @return the byte array representing the hash.
     */
    val hash: ByteArray
    /**
     * Gets the revision data.
     *
     * @return the revision data.
     */
    val data: IonStruct

    /**
     * Gets the unique ID of a QLDB document.
     *
     * @return the [BlockAddress] object.
     */
    fun getBlockAddress(): BlockAddress {
        return blockAddress
    }

    /**
     * Converts a [QldbRevision] object to string.
     *
     * @return the string representation of the [QldbRevision] object.
     */
    override fun toString(): String {
        return "QldbRevision{" +
                "blockAddress=" + blockAddress +
                ", metadata=" + metadata +
                ", hash=" + Arrays.toString(hash) +
                ", data=" + data +
                '}'
    }

    /**
     * Check whether two [QldbRevision] objects are equivalent.
     *
     * @return `true` if the two objects are equal, `false` otherwise.
     */
    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o !is QldbRevision) {
            return false
        }
        return getBlockAddress() == o.getBlockAddress() && metadata == o.metadata && hash.contentEquals(o.hash) && data == o.data
    }

    /**
     * Create a hash code for the [QldbRevision] object.
     *
     * @return the hash code.
     */
    override fun hashCode(): Int { // CHECKSTYLE:OFF - Disabling as we are generating a hashCode of multiple properties.
        var result = Objects.hash(blockAddress, metadata, data)
        // CHECKSTYLE:ON
        result = 31 * result + hash.contentHashCode()
        return result
    }

    companion object {
        private val log = LoggerFactory.getLogger(QldbRevision::class.java)
        private val SYSTEM = IonSystemBuilder.standard().build()
        private val ionHasherProvider = MessageDigestIonHasherProvider("SHA-256")
        private val UTC = ZoneId.of("UTC")
        private val ONE_THOUSAND = BigDecimal.valueOf(1000L)
        /**
         * Constructs a new [QldbRevision] from an [IonStruct].
         *
         * The specified [IonStruct] must include the following fields
         *
         * - blockAddress -- a [BlockAddress],
         * - metadata -- a [Metadata],
         * - hash -- the document's hash calculated by QLDB,
         * - data -- an [IonStruct] containing user data in the document.
         *
         * If any of these fields are missing or are malformed, then throws [IllegalArgumentException].
         *
         * If the document hash calculated from the members of the specified [IonStruct] does not match
         * the hash member of the [IonStruct] then throws [IllegalArgumentException].
         *
         * @param ionStruct
         * The [IonStruct] that contains a [QldbRevision] object.
         * @return the converted [QldbRevision] object.
         * @throws IOException if failed to parse parameter [IonStruct].
         */
        @Throws(IOException::class)
        fun fromIon(ionStruct: IonStruct): QldbRevision {
            return try {
                val blockAddress: BlockAddress = LedgerClient.MAPPER.readValue(
                    ionStruct["blockAddress"],
                    BlockAddress::class.java
                )
                val hash = ionStruct["hash"] as IonBlob
                val metadataStruct = ionStruct["metadata"] as IonStruct
                val data = ionStruct["data"] as IonStruct
                val candidateHash =
                    computeHash(
                        metadataStruct,
                        data
                    )
                if (!Arrays.equals(candidateHash, hash.bytes)) {
                    throw IllegalArgumentException(
                        "Hash entry of QLDB revision and computed hash "
                                + "of QLDB revision do not match"
                    )
                }
                val metadata =
                    Metadata.fromIon(
                        metadataStruct
                    )
                QldbRevision(blockAddress, metadata, hash.bytes, data)
            } catch (e: ClassCastException) {
                log.error("Failed to parse ion document")
                throw IllegalArgumentException("Document members are not of the correct type", e)
            }
        }

        /**
         * Calculate the digest of two QLDB hashes.
         *
         * @param metadata
         * The metadata portion of a document.
         * @param data
         * The data portion of a document.
         * @return the converted [QldbRevision] object.
         */
        fun computeHash(metadata: IonStruct, data: IonStruct): ByteArray {
            val metaDataHash =
                hashIonValue(metadata)
            val dataHash = hashIonValue(data)
            return Verifier.joinHashesPairwise(metaDataHash, dataHash)
        }

        /**
         * Builds a hash value from the given [IonValue].
         *
         * @param ionValue
         * The [IonValue] to hash.
         * @return a byte array representing the hash value.
         */
        private fun hashIonValue(ionValue: IonValue): ByteArray {
            val reader = SYSTEM.newReader(ionValue)
            val hashReader = IonHashReaderBuilder.standard()
                .withHasherProvider(ionHasherProvider)
                .withReader(reader)
                .build()
            while (hashReader.next() != null) {
            }
            return hashReader.digest()
        }
    }

    init {
        this.blockAddress = blockAddress
        this.metadata = metadata
        this.hash = hash
        this.data = data
    }
}