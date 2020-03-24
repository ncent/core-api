package main.services.challenge

//import main.daos.*
//import main.services.transaction.GenerateTransactionService
//
///**
// * Trigger a challenge state change.
// */
//object ChangeChallengeStateService {
//    fun execute(caller: String, challengeId: Int, newState: ActionType) : Transaction {
//        val challenge = Challenge.findById(challengeId)!!
//        if(challenge.challengeSettings.admin != caller)
//            return SOAResult(SOAResultType.FAILURE, "Public key with id of ${caller} cannot change the challenge state")
//        var newState = newState
//        val oldTx = challenge.getLastStateChangeTransaction()!!
//        val oldState = oldTx.action.type
//
//        if(!challenge.canTransitionState(oldState, newState))
//            return SOAResult(SOAResultType.FAILURE, "Cannot transition from ${oldState.type} to ${newState.type}")
//
//        if(challenge.shouldExpire() && challenge.canTransitionState(oldState, ActionType.EXPIRE)) {
//            newState = ActionType.EXPIRE
//        }
//        return GenerateTransactionService.execute(TransactionNamespace(
//            from = challenge.cryptoKeyPair.publicKey,
//            to = challenge.cryptoKeyPair.publicKey,
//            previousTransaction = oldTx.idValue,
//            metadatas = null,
//            action = ActionNamespace(
//                type = newState,
//                data = challenge.idValue,
//                dataType = Challenge::class.simpleName!!
//            )
//        ))
//    }
//}