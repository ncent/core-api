package main.daos

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.google.gson.annotations.Expose
import framework.models.BaseCryptoObject

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class Token(
    val name: String,
    val symbol: String,
    val decimals: Double,
    val totalSupply: Double,
    val cryptoKeyPair: CryptoKeyPair,
    val parent: String?,
    val conversion: Double?
) : BaseCryptoObject(cryptoKeyPair.publicKey) {
    @Expose(serialize = true)
    @JsonProperty("wallet")
    val wallet = mutableMapOf<String, Double>()
}