package framework.models

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.google.gson.annotations.Expose
import main.services.aws.qldb.helpers.BlockAddress
import main.services.aws.qldb.helpers.IonLocalDateTimeDeserializer
import main.services.aws.qldb.helpers.IonLocalDateTimeSerializer
import main.services.aws.qldb.helpers.TransactionMetadata
import java.security.MessageDigest
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

interface Table {
    val tableName: String
}

@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
abstract class BaseCryptoObject(
    var publicKey: String?
): BaseDocument() {
    @Expose(serialize = false)
    @JsonIgnore
    var _hashCode: String? = null
        get() = field ?:
            MessageDigest
                .getInstance("MD5")
                .digest(ObjectMapper().writeValueAsBytes(this))
                .toString()
                .padStart(32, '0')

    @Expose(serialize = true)
    @JsonProperty("hashCode")
    var hashCode = _hashCode

    @JsonSerialize(using = IonLocalDateTimeSerializer::class)
    @JsonDeserialize(using = IonLocalDateTimeDeserializer::class)
    @JsonProperty("createdAt")
    @Expose(serialize = true)
    val createdAt = LocalDateTime.now(ZoneId.of("UTC"))
}

@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
abstract class BaseDocument(var documentId: String? = null) : BaseObject

@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
data class BaseObjectWithMetadata<T: BaseDocument>(
    val blockAddress: BlockAddress,
    val hash: String,
    val data: T,
    val metadata: TransactionMetadata)

@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
interface BaseObject