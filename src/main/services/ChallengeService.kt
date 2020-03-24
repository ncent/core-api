package main.services

import kotlinserverless.framework.models.NotEnoughSharesException
import kotlinserverless.framework.models.NotFoundException
import main.daos.*
import main.helpers.CryptoHelper
import main.services.aws.qldb.LedgerService
import software.amazon.qldb.TransactionExecutor
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

object ChallengeService {
    fun create(challenge: Challenge, context: TransactionExecutor? = null) : Challenge {
        challenge.cryptoKeyPair = challenge.cryptoKeyPair ?: CryptoHelper.generateCryptoKeyPair(CryptoKeyPairType.CONTRACT)
        LedgerService.transaction { txe ->
            val documentId = LedgerService.insert(Challenge::class, challenge, context ?: txe)
            challenge.documentId = documentId

            TransactionService.create(
                NTransaction(
                    outbound = challenge.cryptoKeyPair!!.publicKey,
                    inbound = challenge.challengeSettings.admin,
                    naction = NAction(
                        ActionType.SHARE,
                        Challenge::class.simpleName!!,
                        challenge.publicKey
                    ),
                    challengeData = ChallengeData(challenge)
                ),
                context ?: txe
            )
        }

        return challenge
    }

    fun findByPublicKey(publicKey: String, context: TransactionExecutor? = null): Challenge {
        return LedgerService.findBy(
            clazz = Challenge::class,
            txe = context,
            keyValues = listOf(Pair("publicKey", publicKey))
        )
    }

    fun findAllChallengesByPublicKey(
        publicKey: String,
        inboundTxs: List<NTransaction>? = null,
        outboundTxs: List<NTransaction>? = null,
        context: TransactionExecutor? = null
    ): List<Challenge> {
        return LedgerService.transaction { txe ->
            val ctx = context ?: txe
            val inboundTransactions = inboundTxs ?: findAllInboundShareTransactions(publicKey, ctx)
            val outboundTransactions = outboundTxs ?: findAllOutboundShareTransactions(publicKey, ctx)

            val challengePublicKeys = (inboundTransactions + outboundTransactions)
                .map { tx ->
                    tx.challengeData?.challengePublicKey!!
                }.distinct()

            if (challengePublicKeys.isEmpty())
                throw NotFoundException("No challenges found for $publicKey -- something went wrong.")

            LedgerService.findAllBy(
                clazz = Challenge::class,
                txe = ctx,
                keyValues = challengePublicKeys.map { Pair("publicKey", it) },
                andOrOr = LedgerService.AndOrOr.OR
            )
        }
    }

    fun findSharesForPublicKey(
        publicKey: String,
        context: TransactionExecutor? = null
    ): Map<Challenge, Int> {
        return LedgerService.transaction { txe ->
            val transactions = findTransactions(
                publicKey = publicKey,
                action = NAction(
                    type = ActionType.SHARE,
                    dataType = Challenge::class.simpleName!!
                ),
                context = context ?: txe
            )

            transactions.map {
                val inboundShareCount = it.value.first.map { ins ->
                    ins.challengeData?.maxShares!!
                }.sum()
                val outboundShareCount = it.value.second.map { ins ->
                    ins.challengeData?.maxShares!!
                }.sum()
                it.key to  (inboundShareCount - outboundShareCount)
            }.toMap()
        }
    }

    fun shareChallenge(
        outbound: String,
        inbound: String,
        challenge: Challenge,
        shares: Int,
        expiration: LocalDateTime? = null,
        context: TransactionExecutor? = null
    ): List<NTransaction> {
        return LedgerService.transaction { txe ->
            val ctx = context ?: txe
            val availableShares = getAvailableShares(
                outbound = outbound,
                challenge = challenge,
                context = ctx
            )
            val offChain = challenge.challengeSettings.offChain

            val availableShareCount = availableShares.values.sum()
            if(!offChain && availableShareCount < shares)
                throw NotEnoughSharesException("Failed to share $shares, can only share $availableShareCount")

            var sharesConsumed = 0
            val generatedShares = mutableListOf<NTransaction>()

            availableShares.forEach { (tx, maxShares) ->
                if(sharesConsumed >= shares)
                    return@forEach

                val maxToShare =    if(offChain)
                                        Integer.MAX_VALUE
                                    else
                                        maxShares
                val howManyToShare = (shares - sharesConsumed).coerceAtMost(maxToShare)
                generatedShares.add(
                    TransactionService.create(
                        NTransaction(
                            outbound = outbound,
                            inbound = inbound,
                            expiration = expiration,
                            challengeData = ChallengeData(
                                challenge.publicKey!!,
                                challenge.challengeSettings.offChain,
                                howManyToShare
                            ),
                            previousTransaction = tx.hashCode,
                            naction = NAction(
                                type = ActionType.SHARE,
                                dataType = Challenge::class.simpleName!!,
                                dataKey = challenge.publicKey
                            )
                        ),
                        ctx
                    )
                )
                sharesConsumed += howManyToShare
            }
            return@transaction generatedShares
        }
    }

    private fun getAvailableShares(
        outbound: String,
        challenge: Challenge,
        context: TransactionExecutor? = null
    ): SortedMap<NTransaction, Int> {
        val transactions = findTransactions(
            publicKey = outbound,
            context = context,
            action = NAction(
                type = ActionType.SHARE,
                dataType = Challenge::class.simpleName!!,
                dataKey = challenge.publicKey!!
            ),
            challengesList = listOf(challenge)
        ).entries.first().value

        if(challenge.challengeSettings.offChain)
            return transactions.first.associateBy(
                { it },
                { it.challengeData?.maxShares ?: Int.MAX_VALUE }
            ).toSortedMap(compareBy({ it.createdAt }, { it.documentId }, { it.hashCode }))

        val inboundTransactionHashCodes = transactions.first.map { it.hashCode }
        val spentTransactions = transactions.second
            .filter {
                it.outbound == outbound &&
                it.previousTransaction != null &&
                inboundTransactionHashCodes.contains(it.previousTransaction)
            }
        val spentTransactionMap = mutableMapOf<String, MutableList<NTransaction>>()
        spentTransactions.forEach {
            spentTransactionMap.putIfAbsent(it.previousTransaction!!, mutableListOf())
            spentTransactionMap[it.previousTransaction]!!.add(it)
        }

        // Calculate available shares by getting the outbound transactions
        // that use a transaction as its parent, calculate by maxShares sent.
        return transactions.first.associateBy(
            { it },
            {
                val inboundShares = it.challengeData?.maxShares ?: 0
                val outboundShares = spentTransactionMap[it.publicKey]?.sumBy {
                        child -> (child.challengeData?.maxShares ?: 0)
                } ?: 0
                inboundShares - outboundShares
            }
        ).filter { it.value > 0 }.toSortedMap(compareBy({ it.createdAt }, { it.documentId }, { it.hashCode }) )
    }

    /**
     * Accept a wallet public key, an action, and a list of challenges to filter.
     * If no challenges are inputted, will query challenges from the transactions
     *
     * Returns a map of challenges to inbound and outbound transactions
     *
     * All transactions cannot be expired (expired before being shared)
     */
    private fun findTransactions(
        publicKey: String,
        action: NAction,
        challengesList: List<Challenge>? = null,
        context: TransactionExecutor? = null
    ): Map<Challenge, Pair<List<NTransaction>, List<NTransaction>>> {
        val inboundTransactions = TransactionService.findByPublicKeyAndAction(
            inbound = publicKey,
            action = action,
            context = context
        )

        val outboundTransactions = TransactionService.findByPublicKeyAndAction(
            outbound = publicKey,
            action = action,
            context = context
        )

        val challenges = challengesList ?: challengesForTransactions(inboundTransactions, context)

        val challengesMap = challenges.associateBy({ it.publicKey!! }, { it })
        val result = challenges.associateBy({ it }, { Pair(mutableListOf<NTransaction>(), mutableListOf<NTransaction>()) })

        val currentTime = LocalDateTime.now(ZoneId.of("UTC"))
        inboundTransactions.forEach { tx ->
            val challengePublicKey = tx.challengeData?.challengePublicKey!!

            val isNotExpired = tx.expiration?.isAfter(currentTime) ?: true
            var isAlreadyShared = false
            // If it is expired, check if it has been shared already -- thus active
            if(!isNotExpired) {
                isAlreadyShared = outboundTransactions.filter{ out -> out.previousTransaction == tx.publicKey }.any()
            }
            if(tx.inbound == publicKey && (isNotExpired || isAlreadyShared))
                result[challengesMap[challengePublicKey]]?.first?.add(tx)
        }

        outboundTransactions.forEach { tx ->
            val challengePublicKey = tx.challengeData?.challengePublicKey!!

            result[challengesMap[challengePublicKey]]?.second?.add(tx)
        }

        return result
    }

    private fun challengesForTransactions(
        transactions: List<NTransaction>,
        context: TransactionExecutor? = null
    ): List<Challenge> {
        val challengePublicKeys = transactions
            .map { tx ->
                tx.challengeData?.challengePublicKey!!
            }.distinct()

        if(challengePublicKeys.isNullOrEmpty())
            throw NotFoundException()

        return LedgerService.findAllBy(
            clazz = Challenge::class,
            txe = context,
            keyValues = challengePublicKeys.map { Pair("publicKey", it) },
            andOrOr = LedgerService.AndOrOr.OR
        )
    }

    private fun findAllInboundShareTransactions(publicKey: String, context: TransactionExecutor? = null): List<NTransaction> {
        return TransactionService.findByPublicKeyAndAction(
            inbound = publicKey,
            action = NAction(
                type = ActionType.SHARE,
                dataType = Challenge::class.simpleName!!
            ),
            context = context
        )
    }

    private fun findAllOutboundShareTransactions(publicKey: String, context: TransactionExecutor? = null): List<NTransaction> {
        return TransactionService.findByPublicKeyAndAction(
            outbound = publicKey,
            action = NAction(
                type = ActionType.SHARE,
                dataType = Challenge::class.simpleName!!
            ),
            context = context
        )
    }
}