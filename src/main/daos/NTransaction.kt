package main.daos

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.google.gson.annotations.SerializedName
import framework.models.*
import main.services.aws.qldb.helpers.IonLocalDateTimeDeserializer
import main.services.aws.qldb.helpers.IonLocalDateTimeSerializer
import java.time.LocalDateTime

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class NTransaction(
    val outbound: String,
    val inbound: String,
    val naction: NAction,
    @JsonSerialize(using = IonLocalDateTimeSerializer::class)
    @JsonDeserialize(using = IonLocalDateTimeDeserializer::class)
    val expiration: LocalDateTime? = null,
    val amount: Double? = null,
    val previousTransaction: String? = null,
    val challengeData: ChallengeData? = null,
    val metadatas: Metadatas? = null
): BaseCryptoObject(null) {
    constructor(from: String, to: String, type: ActionType, data: BaseCryptoObject) :
        this(from, to, NAction(type, data::class.simpleName!!, data.publicKey))
}

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
data class TransactionToShareNamespace(
    val NTransaction: NTransaction,
    val shares: Int
)

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
data class TransactionWithNewUserNamespace(
    val NTransactions: List<NTransaction>,
    val newKeypair: NewCryptoKeyPair? = null
)

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
data class NAction(
    val type: ActionType,
    val dataType: String,
    val dataKey: String?=null
)

enum class ActionType(val type: String) {
    @SerializedName("transfer") TRANSFER("transfer"),
    @SerializedName("create") CREATE("create"),
    @SerializedName("share") SHARE("share"),
    @SerializedName("payout") PAYOUT("payout"),
    @SerializedName("activate") ACTIVATE("activate"),
    @SerializedName("complete") COMPLETE("complete"),
    @SerializedName("invalidate") INVALIDATE("invalidate"),
    @SerializedName("expire") EXPIRE("expire"),
    @SerializedName("update") UPDATE("update")
}