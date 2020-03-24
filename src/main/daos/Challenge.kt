package main.daos

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import framework.models.BaseCryptoObject
import framework.models.BaseObject
import main.services.aws.qldb.helpers.IonLocalDateTimeDeserializer
import main.services.aws.qldb.helpers.IonLocalDateTimeSerializer
import java.time.LocalDateTime

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
data class Challenge(
    val challengeSettings: ChallengeSetting,
    val challengeType: ChallengeType,
    val completionCriteria: String,
    val reward: Reward,
    val distributionFeeReward: Reward,
    var parentChallenge: String? = null,
    @Expose(serialize = false)
    @JsonIgnore
    var _cryptoKeyPair: CryptoKeyPair? = null,
    val preReqs: List<String>? = mutableListOf()
): BaseCryptoObject(_cryptoKeyPair?.publicKey) {
    var cryptoKeyPair: CryptoKeyPair?
        get() = _cryptoKeyPair
        set(value) {
            _cryptoKeyPair = value
            publicKey = value?.publicKey
        }
    fun canTransitionState(fromState: ActionType, toState: ActionType): Boolean {
        val result =  when(fromState) {
            ActionType.COMPLETE -> {
                false
            }
            ActionType.CREATE -> {
                return arrayListOf(
                    ActionType.ACTIVATE,
                    ActionType.EXPIRE,
                    ActionType.INVALIDATE
                ).contains(toState)
            }
            ActionType.EXPIRE -> {
                false
            }
            ActionType.INVALIDATE -> {
                return arrayListOf(
                    ActionType.ACTIVATE,
                    ActionType.EXPIRE
                ).contains(toState)
            }
            ActionType.ACTIVATE -> {
                return arrayListOf(
                    ActionType.COMPLETE,
                    ActionType.EXPIRE,
                    ActionType.INVALIDATE
                ).contains(toState)
            }
            else -> throw Exception("Could not find challenge state to move to for ${fromState.type}")
        }
        return if(result && toState == ActionType.EXPIRE) {
            result && shouldExpire()
        } else {
            result
        }
    }

    private fun shouldExpire(): Boolean {
        return challengeSettings.expiration < LocalDateTime.now()
    }

//    @JsonIgnore
//    private val stateChangeActionTypes = setOf(ActionType.CREATE, ActionType.ACTIVATE, ActionType.COMPLETE, ActionType.INVALIDATE, ActionType.EXPIRE)

//    private fun getTransactions(): List<Transaction>? {
//        return GetTransactionsService.execute(
//            null,
//            cryptoKeyPair!!.publicKey
//        )
//    }

//    fun getLastStateChangeTransaction(): Transaction? {
//        getTransactions()?.forEach {
//            if(stateChangeActionTypes.contains(it.action.type))
//                return it
//        }
//        return null
//    }
}

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class Challenger(
    val challenger: String,
    val receivers: List<Challenger>? = null
): BaseObject

data class ChallengeToUnsharedTransactionNamespace(val challenge: Challenge, val shareNTransactionList: List<NTransaction>)

data class SubChallengeNamespace(val subChallengePublicKey: String?, val type: String?)

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class PublicKeyToChallengeBalanceList(
    val challengePublicKey: String,
    val publicKeyToChallengeBalances: MutableMap<String, Int> = mutableMapOf()
): BaseObject

enum class ChallengeType(val type: String) {
    @SerializedName("sync") SYNC("sync"),
    @SerializedName("async") ASYNC("async")
}

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class ChallengeSetting(
    val name: String,
    val description: String,
    val imageUrl: String,
    val sponsorName: String,
    @JsonSerialize(using = IonLocalDateTimeSerializer::class)
    @JsonDeserialize(using = IonLocalDateTimeDeserializer::class)
    val expiration: LocalDateTime,
    val shareExpirationDays: Long,
    val admin: String,
    val offChain: Boolean,
    val maxShares: Int?,
    val maxRewards: Int?,
    val maxDistributionFeeReward: Double?,
    val maxSharesPerReceivedShare: Int?,
    val maxDepth: Int?,
    val maxNodes: Int?,
    val metadatas: Metadatas?
): BaseObject

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class Reward(
    val type: RewardType,
    val audience: RewardAudience,
    val pool: List<String>,
    val metadatas: Metadatas? = null
) : BaseObject

/**
 * RewardType signifies the way the rewards will be distributed amongst the entire chain or the providence chain
 *
 * @property id
 * @property audience example: [PROVIDENCE, FULL]
 * @property type example: [SINGLE, EVEN, LOGARITHMIC, EXPONENTIAL]
 */
enum class RewardAudience(val str: String) {
    @SerializedName("providence") PROVIDENCE("providence"),
    @SerializedName("full") FULL("full")
}

enum class RewardType(val str: String) {
    @SerializedName("single") SINGLE("single"),
    @SerializedName("even") EVEN("even"),
    @SerializedName("log") LOGARITHMIC("log"),
    @SerializedName("exp") EXPONENTIAL("exp"),
    @SerializedName("nover2") N_OVER_2("nover2")
}

data class ChallengeData(
    val challengePublicKey: String? = null,
    val offChain: Boolean? = null,
    val maxShares: Int? = null
) {
    constructor(challenge: Challenge) :
        this(challenge.publicKey!!, challenge.challengeSettings.offChain, challenge.challengeSettings.maxShares)
}

data class ShareChallengeData(
    val inbound: String,
    val challengePublicKey: String,
    val shares: Int
)