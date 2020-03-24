package main.services.aws.qldb.helpers

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonCreator
import org.slf4j.LoggerFactory
import java.util.*


/**
 * Represents the BlockAddress field of a QLDB document.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class BlockAddress @JsonCreator constructor(
    val strandId: String,
    val sequenceNo: Long
) {

    override fun toString(): String {
        return ("BlockAddress{"
                + "strandId='" + strandId + '\''
                + ", sequenceNo=" + sequenceNo
                + '}')
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }
        val that = o as BlockAddress
        return (sequenceNo == that.sequenceNo
                && strandId == that.strandId)
    }

    override fun hashCode(): Int { // CHECKSTYLE:OFF - Disabling as we are generating a hashCode of multiple properties.
        return Objects.hash(strandId, sequenceNo)
        // CHECKSTYLE:ON
    }

    companion object {
        private val log = LoggerFactory.getLogger(BlockAddress::class.java)
    }

}