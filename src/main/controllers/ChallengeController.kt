package main.controllers

import com.google.gson.JsonSyntaxException
import framework.models.Handler
import kotlinserverless.framework.controllers.RestController
import kotlinserverless.framework.controllers.DefaultController
import kotlinserverless.framework.models.InvalidArguments
import main.daos.*
import main.helpers.ControllerHelper.RequestData
import main.services.ChallengeService
import main.services.aws.qldb.LedgerService
import java.lang.IllegalStateException
import java.time.LocalDateTime
import java.time.ZoneId

class ChallengeController: DefaultController<Challenge>(), RestController<Challenge> {
    @Throws(InvalidArguments::class)
    override fun create(requestData: RequestData): Challenge {
        try {
            val challenge = Handler.gson.fromJson(
                requestData.body!!,
                Challenge::class.java
            )
            return ChallengeService.create(challenge)
        } catch(e: TypeCastException) {
            System.err.println("There was an error parsing the input for create challenge: ${requestData.body as String}")
            System.err.println(e.message)
            e.printStackTrace()
            throw InvalidArguments("challenge")
        } catch(e: JsonSyntaxException) {
            System.err.println("There was an error parsing the input for create challenge: ${requestData.body as String}")
            System.err.println(e.message)
            e.printStackTrace()
            throw InvalidArguments("challenge")
        } catch(e: IllegalStateException) {
            System.err.println("There was an error parsing the input for create challenge: ${requestData.body as String}")
            System.err.println(e.message)
            e.printStackTrace()
            throw InvalidArguments("challenge")
        }
    }

    override fun findOne(requestData: RequestData, identifier: String?): Challenge {
        return ChallengeService.findByPublicKey(identifier!!)
    }

    override fun findAll(requestData: RequestData): List<Challenge> {
        return ChallengeService.findAllChallengesByPublicKey(requestData.publicKey)
    }

    fun balances(requestData: RequestData): Map<Challenge, Int> {
        return ChallengeService.findSharesForPublicKey(requestData.publicKey)
    }

//    fun activate(requestData: RequestData): Transaction {
//        return ActivateChallengeService.execute(
//            requestData.publicKey,
//            requestData.body["challengePublicKey"] as Int
//        )
//    }
//
//    fun complete(requestData: RequestData): List<Transaction> {
//        val challenge = ChallengeHelper.findChallengeByPublicKey(
//            requestData.body["challengePublicKey"] as String
//        )
//
//        return CompleteChallengeService.execute(
//            requestData.publicKey,
//            challenge,
//            requestData.body["completerPublicKey"] as String
//        )
//    }

//    fun redeem(requestData: RequestData): List<Transaction> {
//        val challenge = ChallengeHelper.findChallengeByPublicKey(
//            requestData.body["challengePublicKey"] as String
//        )
//
//        return RedeemChallengeService.execute(
//            requestData.publicKey,
//            challenge,
//            requestData.body["completerPublicKey"] as String
//        )
//    }
//
//    fun chains(requestData: RequestData): Challenger {
//        return GetChainsForChallengeService.execute(
//            requestData.queryParams["challengePublicKey"] as String
//        )
//    }
//
    fun share(requestData: RequestData): List<NTransaction> {
    try {
        val shareChallengeData = Handler.gson.fromJson(
            requestData.body!!,
            ShareChallengeData::class.java
        )
        return LedgerService.transaction { txe ->
            val challenge = ChallengeService.findByPublicKey(shareChallengeData.challengePublicKey, txe)
            val toShare =   if(challenge.challengeSettings.offChain)
                                Int.MAX_VALUE
                            else
                                shareChallengeData.shares ?: 0
            ChallengeService.shareChallenge(
                outbound = requestData.publicKey,
                inbound = shareChallengeData.inbound,
                expiration = LocalDateTime.now(ZoneId.of("UTC")).plusDays(
                    challenge.challengeSettings.shareExpirationDays
                ),
                challenge = challenge,
                shares = toShare,
                context = txe
            )
        }
    } catch(e: TypeCastException) {
        System.err.println("There was an error parsing the input for create challenge: ${requestData.body as String}")
        System.err.println(e.message)
        e.printStackTrace()
        throw InvalidArguments("challenge")
    } catch(e: JsonSyntaxException) {
        System.err.println("There was an error parsing the input for create challenge: ${requestData.body as String}")
        System.err.println(e.message)
        e.printStackTrace()
        throw InvalidArguments("challenge")
    } catch(e: IllegalStateException) {
        System.err.println("There was an error parsing the input for create challenge: ${requestData.body as String}")
        System.err.println(e.message)
        e.printStackTrace()
        throw InvalidArguments("challenge")
    }
    }
}