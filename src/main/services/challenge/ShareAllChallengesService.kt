package main.services.challenge

//import kotlinserverless.framework.services.SOAResult
//import kotlinserverless.framework.services.SOAResultType
//import main.daos.*
//
///**
// * Share all challenges from a public key.
// */
//object ShareAllChallengesService {
//    fun execute(
//        caller: String,
//        publicKey: String
//    ) : SOAResult<Pair<TransactionList, String>> {
//
//       // Pull list of non-transferred shares.
//        val unsharedTransactions = GetUnsharedTransactionsService.execute(caller)
//        if(unsharedTransactions.result != SOAResultType.SUCCESS)
//            return SOAResult(SOAResultType.FAILURE, unsharedTransactions.message)
//
//        // Iterate through unshared transactions and share all.
//        var sharedTransactions = mutableListOf<Transaction>()
//        unsharedTransactions.data!!.transactionsToShares.forEach {
//            val challenge = Challenge.findById(it.transaction.action.data)!!
//            val numShares = it.shares
//            var result = ShareChallengeService.execute(
//                    caller, challenge, numShares, publicKey)
//            if(result.result != SOAResultType.SUCCESS)
//                return SOAResult(SOAResultType.FAILURE, result.message)
//
//            sharedTransactions.addAll(result.data!!.transactions)
//        }
//
//        return SOAResult(
//                SOAResultType.SUCCESS,
//                "Successfully transferred all challenge shares.",
//                Pair(TransactionList(sharedTransactions), publicKey))
//    }
//}