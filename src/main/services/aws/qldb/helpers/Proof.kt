package main.services.aws.qldb.helpers

import com.amazon.ion.IonReader
import com.amazon.ion.system.IonSystemBuilder
import java.util.*


/**
 * A Java representation of the [Proof] object.
 * Returned from the [com.amazonaws.services.qldb.AmazonQLDB.getRevision] api.
 */
class Proof(val internalHashes: List<ByteArray>) {

    companion object {
        private val SYSTEM = IonSystemBuilder.standard().build()
        /**
         * Decodes a [Proof] from an ion text String. This ion text is returned in
         * a [GetRevisionResult.getProof]
         *
         * @param ionText
         * The ion text representing a [Proof] object.
         * @return [JournalBlock] parsed from the ion text.
         * @throws IllegalStateException if failed to parse the [Proof] object from the given ion text.
         */
        fun fromBlob(ionText: String?): Proof {
            return try {
                val reader: IonReader = SYSTEM.newReader(ionText)
                val list: MutableList<ByteArray> = ArrayList()
                reader.next()
                reader.stepIn()
                while (reader.next() != null) {
                    list.add(reader.newBytes())
                }
                Proof(list)
            } catch (e: Exception) {
                throw IllegalStateException("Failed to parse a Proof from byte array")
            }
        }
    }

}