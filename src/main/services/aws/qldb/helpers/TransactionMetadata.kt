package main.services.aws.qldb.helpers

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonCreator
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.util.*


@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class TransactionMetadata @JsonCreator constructor(
    val id: String,
    val version: Int,
    val txTime: LocalDate,
    val txId: String
)