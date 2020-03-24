package main.services.challenge

//import main.daos.*
//import main.services.transaction.GetProvidenceChainService
//import main.services.transaction.GetTransactionsService
//
//object GetChainsForChallengeService {
//    fun execute(challengePublicKey: String): Challenger {
//        val challenge = ChallengeHelper.findChallengeByPublicKey(challengePublicKey)
//        val transaction = getOriginatingTransaction(challenge)
//            ?: throw Exception("Failed to find a correct transaction for this challenge.")
//
//        return getChildrenGraph(transaction)
//    }
//
//    private fun getChildrenGraph(currentTransaction: Transaction): Challenger {
//        var children = GetProvidenceChainService.getChildren(currentTransaction.id).toMutableList()
//        var childrenGraph = mutableListOf<Challenger>()
//        while(children.any()) {
//            val currentChild = children.removeAt(0)
//            val currentChildChildrenGraph = getChildrenGraph(currentChild)
//            childrenGraph.add(currentChildChildrenGraph)
//        }
//        return Challenger(currentTransaction.to!!, childrenGraph)
//    }
//
//    private fun getOriginatingTransaction(challenge: Challenge): Transaction? {
//        val transactionsListResult = GetTransactionsService.execute(
//            from = challenge.cryptoKeyPair.publicKey,
//            to = challenge.challengeSettings.admin,
//            previousTxId = null,
//            action = Action(
//                type = ActionType.SHARE,
//                dataKey = challenge.cryptoKeyPair.publicKey,
//                dataType = Challenge::class.simpleName!!
//            )
//        )
//
//        if(transactionsListResult.count() == 1)
//            return transactionsListResult.first()
//        return null
//    }
//}
