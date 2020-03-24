package test

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import framework.models.Handler
import main.daos.*
import main.helpers.CryptoHelper
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.sql.Time
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAccessor

object TestHelper {
    // Used to generate a request to the api, used for integration testing
    fun buildRequest(publicKey: String, path: String, httpMethod: String, body: String? = null, queryParams: Map<String, Any?>? = null): Map<String, Any> {
        var request = mutableMapOf<String, Any>()
        request["path"] = path
        request["httpMethod"] = httpMethod
        if(body != null)
            request["body"] = body
        if(queryParams != null)
            request["queryStringParameters"] = queryParams
        val auth = getAuthorizationHeader(publicKey)
        request["headers"] = mapOf(auth.first to auth.second)
        return request
    }

    /**
     * Building a tree:
     *         ARYA
     *           |
     *         ARYA2
     *         /    \
     *      ARYA3   ARYA4
     *             /    \
     *          ARYA5   ARYA6
     *
     * Returns [endTransactionId: ARYA6 ,sideTransactionId: ARYA4]
     */
//    fun buildGenericProvidenceChain(): List<Transaction> {
//        var action = Action(
//            type = ActionType.CREATE,
//            data = 1,
//            dataType = "publicKey"
//        )
//        var metadatas = arrayOf(
//                MetadatasNamespace("city", "san carlos"),
//                MetadatasNamespace("state", "california")
//        )
//
//        return transaction {
//            var transactionNamespace = TransactionNamespace(from = "ARYA", to = "MIKE", action = action, previousTransaction = null, metadatas = metadatas)
//            var tx1 = GenerateTransactionService.execute(transactionNamespace).data!!
//            var transaction2Namespace = TransactionNamespace(from = "ARYA2", to = "MIKE2", action = action, previousTransaction = tx1.idValue, metadatas = metadatas)
//            var tx2 = GenerateTransactionService.execute(transaction2Namespace).data!!
//            var transaction3Namespace = TransactionNamespace(from = "ARYA3", to = "MIKE3", action = action, previousTransaction = tx2.idValue, metadatas = metadatas)
//            var transaction4Namespace = TransactionNamespace(from = "ARYA4", to = "MIKE4", action = action, previousTransaction = tx2.idValue, metadatas = metadatas)
//            var tx3 = GenerateTransactionService.execute(transaction3Namespace).data!!
//            var tx4 = GenerateTransactionService.execute(transaction4Namespace).data!!
//            var transaction5Namespace = TransactionNamespace(from = "ARYA5", to = "MIKE5", action = action, previousTransaction = tx4.idValue, metadatas = metadatas)
//            var transaction6Namespace = TransactionNamespace(from = "ARYA6", to = "MIKE6", action = action, previousTransaction = tx4.idValue, metadatas = metadatas)
//            var tx5 = GenerateTransactionService.execute(transaction5Namespace).data!!
//            var tx6 = GenerateTransactionService.execute(transaction6Namespace).data!!
//            return@transaction listOf(tx1, tx2, tx3, tx4, tx5, tx6)
//        }
//    }
//
//    fun buildGenericReward(
//        publicKey: String,
//        audience: Audience = Audience.PROVIDENCE,
//        type: RewardTypeName = RewardTypeName.EVEN
//    ): Reward {
//        var rewardNamespace = RewardNamespace(
//            type = RewardTypeNamespace(
//                audience = audience,
//                type = type
//            ),
//            metadatas = arrayOf(MetadatasNamespace("title", "reward everyone"))
//        )
//        var nCentTokenNamespace = TokenNamespace(
//            amount = 100,
//            tokenType = TokenTypeNamespace(
//                id = null,
//                name = "nCent" + DateTime.now(DateTimeZone.UTC).millis,
//                parentToken = null,
//                parentTokenConversionRate = null
//            )
//        )
//
//        var completionCriteriaNamespace = CompletionCriteriaNamespace(
//            address = null,
//            reward = rewardNamespace,
//            prereq = listOf()
//        )
//
//        // Create a public key, token, reward, and add to the pool
//        // Create a fake providence chain
//        return transaction {
//            val token = GenerateTokenService.execute(publicKey, nCentTokenNamespace).data!!
//            var reward = GenerateRewardService.execute(rewardNamespace).data!!
//            val rewardPoolTx = AddToRewardPoolService.execute(
//                publicKey,
//                reward.idValue,
//                token.tokenType.name,
//                10.0
//            ).data!!
//
//            var completionCriteria = GenerateCompletionCriteriaService.execute(publicKey, completionCriteriaNamespace).data!!
//            completionCriteria.reward = reward
//
//            return@transaction reward
//        }
//    }

    fun generateKeyPairs(count: Int = 1): List<NewCryptoKeyPair> {
        var newKeyPairs = mutableListOf<NewCryptoKeyPair>()
        for(i in 0..(count - 1)) {
            newKeyPairs.add(NewCryptoKeyPair(
                    CryptoKeyPair(
                    "dev$i+${DateTime.now().millis}",
                    "ncnt$i",
                    CryptoKeyPairType.ACCOUNT.str),
                "ncnt$i"
                )
            )
        }
        return newKeyPairs
    }

//    fun generateFullChallenge(publicKey: String, subChallengePublicKey: String, count: Int = 1, withReward: Boolean = false): List<Challenge> {
//        return DaoService.execute {
//            var challengesToReturn = mutableListOf<Challenge>()
//
//            for(i in 0..(count - 1)) {
//                val challenges = generateChallenge(subChallengePublicKey, 3)
//                val parentChallenge = challenges[0]
//                var subChallengeList = mutableListOf<SubChallengeNamespace>()
//                subChallengeList.add(SubChallengeNamespace(challenges[1].idValue, SubChallengeType.SYNC.toString()))
//                subChallengeList.add(SubChallengeNamespace(challenges[2].idValue, SubChallengeType.ASYNC.toString()))
//                val distributionFeeRewardNamespace = TestHelper.generateRewardNamespace(RewardTypeName.SINGLE)
//                val challengeSettingNamespace = TestHelper.generateChallengeSettingsNamespace(publicKey).first()
//                val completionCriteriasNamespace = TestHelper.generateCompletionCriteriaNamespace(publicKey, 2)
//                val completionCriteria1 = completionCriteriasNamespace[0]
//                val challengeNamespace = ChallengeNamespace(
//                    parentChallenge = parentChallenge.idValue.toString(),
//                    challengeSettings = challengeSettingNamespace,
//                    subChallenges = subChallengeList,
//                    completionCriteria = completionCriteria1,
//                    distributionFeeReward = distributionFeeRewardNamespace
//                )
//                val challengeResult = GenerateChallengeService.execute(publicKey, challengeNamespace)
//                val challenge = challengeResult.data!!
//                if(withReward) {
//                    challenge.completionCriterias = CompletionCriteria.find { CompletionCriterias.reward eq buildGenericReward(publicKey).id }.first()
//                }
//                challengesToReturn.add(challenge)
//            }
//            return@execute challengesToReturn
//        }.data!!
//    }

//    fun generateChallenge(publicKey: String, count: Int = 1, offChain: Boolean = false): List<Challenge> {
//        var challenge = generateChallenge(publicKey, count, offChain)
//        return transaction {
//            var challenges = mutableListOf<Challenge>()
//
//            challengeNamespaces.forEach {
//                val challengeResult = ChallengeService.create(publicKey, it)
//                challenges.add(challengeResult.data!!)
//            }
//            return@execute challenges
//        }.data!!
//    }

    fun generateChallenge(publicKey: String, count: Int = 1, challengeType: ChallengeType? = ChallengeType.ASYNC): List<Challenge> {
        var challengeSettingsList = generateChallengeSettings(publicKey, count, challengeType)
        var challengeDistributionReward = generateReward(RewardType.SINGLE)
        var challengeNamespaces = mutableListOf<Challenge>()

        for(i in 0..(count - 1)) {
            val challengeNamespace = Challenge(
                parentChallenge = null,
                challengeSettings = challengeSettingsList[i],
                distributionFeeReward = challengeDistributionReward,
                completionCriteria = publicKey,
                challengeType = challengeType!!,
                reward = generateReward()
            )
            challengeNamespace.cryptoKeyPair = CryptoHelper.generateCryptoKeyPair(CryptoKeyPairType.CONTRACT)
            challengeNamespaces.add(challengeNamespace)
        }
        return challengeNamespaces
    }

    fun generateChallengeSettings(publicKey: String, count: Int = 1, challengeType: ChallengeType? = ChallengeType.ASYNC): List<ChallengeSetting> {
        val challengeSettingsList = mutableListOf<ChallengeSetting>()
        val exp = LocalDateTime.now(ZoneId.of("UTC")).plusDays(1)
        for(i in 0..(count - 1)) {
            challengeSettingsList.add(
                ChallengeSetting(
                    name = "TESTname$i",
                    description = "TESTdescription$i",
                    imageUrl = "TESTimageUrl$i",
                    sponsorName = "TESTsponsorName$i",
                    expiration = exp,
                    shareExpirationDays = 1,
                    admin = publicKey,
                    maxShares = 100,
                    offChain = challengeType!! == ChallengeType.ASYNC,
                    maxRewards = null,
                    maxDistributionFeeReward = null,
                    maxSharesPerReceivedShare = null,
                    maxDepth = null,
                    maxNodes = null,
                    metadatas = Metadatas(mapOf(Pair("TESTkey$i","TESTvalue$i")))
                )
            )
        }
        return challengeSettingsList.toList()
    }

    fun generateReward(rewardType: RewardType? = RewardType.N_OVER_2): Reward {
        return Reward(rewardType!!, RewardAudience.PROVIDENCE, listOf())
    }

//    fun generateShareTransaction(challenge: Challenge, fromAccount: String, toAccount: String, previousTransaction: Transaction, amount: Int): Transaction {
//        return GenerateTransactionService.execute(TransactionNamespace(
//            from = fromAccount,
//            to = toAccount,
//            previousTransaction = previousTransaction.idValue,
//            metadatas = ChallengeMetadata(
//                            challenge.idValue,
//                            challenge.challengeSettings.offChain,
//                            challenge.challengeSettings.shareExpiration.toString(),
//                            amount
//                        ).getChallengeMetadataNamespaces().toTypedArray(),
//            action = ActionNamespace(
//                type = ActionType.SHARE,
//                data = challenge.idValue,
//                dataType = Challenge::class.simpleName!!
//            )
//        )).data!!
//    }

//    /**
//     *          0
//     *        / \ \  \
//     *       1  2  3  4
//     *      / \       \
//     *     5  6        7
//     */
//    fun createChainsOfShares(newKeyPairs: List<NewKeyPair>, challenge1: Challenge): Challenger {
//        ShareChallengeService.execute(
//            newKeyPairs[0].publicKey,
//            challenge1,
//            1,
//            newKeyPairs[1].publicKey
//        )
//
//        ShareChallengeService.execute(
//            newKeyPairs[0].publicKey,
//            challenge1,
//            1,
//            newKeyPairs[2].publicKey
//        )
//
//        ShareChallengeService.execute(
//            newKeyPairs[0].publicKey,
//            challenge1,
//            1,
//            newKeyPairs[3].publicKey
//        )
//
//        ShareChallengeService.execute(
//            newKeyPairs[0].publicKey,
//            challenge1,
//            1,
//            newKeyPairs[4].publicKey
//        )
//
//        ShareChallengeService.execute(
//            newKeyPairs[1].publicKey,
//            challenge1,
//            1,
//            newKeyPairs[5].publicKey
//        )
//
//        ShareChallengeService.execute(
//            newKeyPairs[1].publicKey,
//            challenge1,
//            1,
//            newKeyPairs[6].publicKey
//        )
//
//        ShareChallengeService.execute(
//            newKeyPairs[4].publicKey,
//            challenge1,
//            1,
//            newKeyPairs[7].publicKey
//        )
//
//        val challenger1 = Challenger(
//            newKeyPairs[1].publicKey,
//            listOf(Challenger(newKeyPairs[5].publicKey), Challenger(newKeyPairs[6].publicKey))
//        )
//        val challenger4 = Challenger(
//            newKeyPairs[4].publicKey,
//            listOf(Challenger(newKeyPairs[7].publicKey))
//        )
//
//        return Challenger(
//            newKeyPairs[0].publicKey,
//            listOf(
//                challenger1,
//                Challenger(newKeyPairs[2].publicKey),
//                Challenger(newKeyPairs[3].publicKey),
//                challenger4
//            )
//        )
//    }

    private fun getAuthorizationHeader(publicKey: String): Pair<String, String> {
        val encodedPublicKey = JWT.create().withClaim("publicKey", publicKey).sign(Algorithm.HMAC256("secret"))
        return Pair("Authorization", encodedPublicKey)
    }
}