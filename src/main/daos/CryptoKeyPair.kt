package main.daos

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import main.helpers.EncryptionHelper

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
data class CryptoKeyPair(
    val publicKey: String,
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) private val _privateKey: String?,
    val keyPairType: String
) {
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Expose(serialize = false)
    var privateKey: String? = null
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Expose(serialize = false)
    private var _privateKeySalt: String? = null

    init {
        if(_privateKey != null) {
            val encryption = EncryptionHelper.encrypt(_privateKey)
            this.privateKey = encryption.first
            _privateKeySalt = encryption.second
        }
    }
}

data class NewCryptoKeyPair(
    val value: CryptoKeyPair,
    val secret: String
)

enum class CryptoKeyPairType(val str: String){
    @SerializedName("account") ACCOUNT("account"),
    @SerializedName("contract") CONTRACT("contract")
}