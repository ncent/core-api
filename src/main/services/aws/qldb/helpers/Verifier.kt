package main.services.aws.qldb.helpers

import com.amazonaws.util.Base64
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import java.util.function.BinaryOperator
import kotlin.experimental.xor


/**
 * Encapsulates the logic to verify the integrity of revisions or blocks in a QLDB ledger.
 *
 * The main entry point is [.verify].
 *
 * This code expects that you have AWS credentials setup per:
 * http://docs.aws.amazon.com/java-sdk/latest/developer-guide/setup-credentials.html
 */
object Verifier {
    val log = LoggerFactory.getLogger(Verifier::class.java)
    private const val HASH_LENGTH = 32
    private const val UPPER_BOUND = 8
    /**
     * Compares two hashes by their *signed* byte values in little-endian order.
     */
    private val hashComparator =
        Comparator { h1: ByteArray, h2: ByteArray ->
            if (h1.size != HASH_LENGTH || h2.size != HASH_LENGTH) {
                throw IllegalArgumentException("Invalid hash.")
            }
            var i = h1.size - 1
            while (i >= 0) {
                val byteEqual = java.lang.Byte.compare(h1[i], h2[i])
                if (byteEqual != 0) {
                    return@Comparator byteEqual
                }
                i--
            }
            0
        }

    /**
     * Verify the integrity of a document with respect to a QLDB ledger digest.
     *
     * The verification algorithm includes the following steps:
     *
     * 1. [.buildCandidateDigest] build the candidate digest from the internal hashes
     * in the [Proof].
     * 2. Check that the `candidateLedgerDigest` is equal to the `ledgerDigest`.
     *
     * @param documentHash
     * The hash of the document to be verified.
     * @param digest
     * The QLDB ledger digest. This digest should have been retrieved using
     * [com.amazonaws.services.qldb.AmazonQLDB.getDigest]
     * @param proofBlob
     * The ion encoded bytes representing the [Proof] associated with the supplied
     * `digestTipAddress` and `address` retrieved using
     * [com.amazonaws.services.qldb.AmazonQLDB.getRevision].
     * @return `true` if the record is verified or `false` if it is not verified.
     */
    fun verify(
        documentHash: ByteArray,
        digest: ByteArray?,
        proofBlob: String?
    ): Boolean {
        val proof: Proof = Proof.fromBlob(proofBlob)
        val candidateDigest =
            buildCandidateDigest(proof, documentHash)
        return Arrays.equals(digest, candidateDigest)
    }

    /**
     * Build the candidate digest representing the entire ledger from the internal hashes of the [Proof].
     *
     * @param proof
     * A Java representation of [Proof]
     * returned from [com.amazonaws.services.qldb.AmazonQLDB.getRevision].
     * @param leafHash
     * Leaf hash to build the candidate digest with.
     * @return a byte array of the candidate digest.
     */
    private fun buildCandidateDigest(proof: Proof, leafHash: ByteArray): ByteArray {
        return calculateRootHashFromInternalHashes(
            proof.internalHashes,
            leafHash
        )
    }

    /**
     * Get a new instance of [MessageDigest] using the SHA-256 algorithm.
     *
     * @return an instance of [MessageDigest].
     * @throws IllegalStateException if the algorithm is not available on the current JVM.
     */
    fun newMessageDigest(): MessageDigest {
        return try {
            MessageDigest.getInstance("SHA-256")
        } catch (e: NoSuchAlgorithmException) {
            log.error("Failed to create SHA-256 MessageDigest", e)
            throw IllegalStateException("SHA-256 message digest is unavailable", e)
        }
    }

    /**
     * Takes two hashes, sorts them, concatenates them, and then returns the
     * hash of the concatenated array.
     *
     * @param h1
     * Byte array containing one of the hashes to compare.
     * @param h2
     * Byte array containing one of the hashes to compare.
     * @return the concatenated array of hashes.
     */
    fun joinHashesPairwise(h1: ByteArray, h2: ByteArray): ByteArray {
        if (h1.size == 0) {
            return h2
        }
        if (h2.size == 0) {
            return h1
        }
        val concatenated = ByteArray(h1.size + h2.size)
        if (hashComparator.compare(h1, h2) < 0) {
            System.arraycopy(h1, 0, concatenated, 0, h1.size)
            System.arraycopy(h2, 0, concatenated, h1.size, h2.size)
        } else {
            System.arraycopy(h2, 0, concatenated, 0, h2.size)
            System.arraycopy(h1, 0, concatenated, h2.size, h1.size)
        }
        val messageDigest = newMessageDigest()
        messageDigest.update(concatenated)
        return messageDigest.digest()
    }

    /**
     * Starting with the provided `leafHash` combined with the provided `internalHashes`
     * pairwise until only the root hash remains.
     *
     * @param internalHashes
     * Internal hashes of Merkle tree.
     * @param leafHash
     * Leaf hashes of Merkle tree.
     * @return the root hash.
     */
    fun calculateRootHashFromInternalHashes(
        internalHashes: List<ByteArray>,
        leafHash: ByteArray
    ): ByteArray {
        return internalHashes.stream().reduce(leafHash,
            BinaryOperator { h1: ByteArray, h2: ByteArray ->
                joinHashesPairwise(
                    h1,
                    h2
                )
            }
        )
    }

    /**
     * Flip a single random bit in the given byte array. This method is used to demonstrate
     * QLDB's verification features.
     *
     * @param original
     * The original byte array.
     * @return the altered byte array with a single random bit changed.
     */
    fun flipRandomBit(original: ByteArray): ByteArray {
        if (original.size == 0) {
            throw IllegalArgumentException("Array cannot be empty!")
        }
        val alteredPosition = ThreadLocalRandom.current().nextInt(original.size)
        val b = ThreadLocalRandom.current().nextInt(UPPER_BOUND)
        val altered = ByteArray(original.size)
        System.arraycopy(original, 0, altered, 0, original.size)
        altered[alteredPosition] = (altered[alteredPosition] xor (1 shl b).toByte()) as Byte
        return altered
    }

    fun toBase64(arr: ByteArray?): String {
        return String(Base64.encode(arr), StandardCharsets.UTF_8)
    }

    /**
     * Convert a [ByteBuffer] into byte array.
     *
     * @param buffer
     * The [ByteBuffer] to convert.
     * @return the converted byte array.
     */
    fun convertByteBufferToByteArray(buffer: ByteBuffer): ByteArray {
        val arr = ByteArray(buffer.remaining())
        buffer[arr]
        return arr
    }
}