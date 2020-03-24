package main.services.reward

//import kotlinserverless.framework.services.SOAResult
//import kotlinserverless.framework.services.SOAResultType
//import main.daos.*
//import main.helpers.TransferTokenHelper
//import main.services.transaction.GetProvidenceChainService
//
///**
// * Transfer tokens based on rewards
// */
//object DistributeRewardService {
//    fun execute(reward: Reward, transaction: Transaction) : TransactionList {
//        val address = reward.pool
//
//        // calculate rewards
//        // get all the transactions -- verify they have not been spent
//        // check that there are no outbount tx from the completion criteria -- if there are deduct
//
//        val mapOfBalancesResult = TransferTokenHelper.getMapOfBalancesByCurrency(address)
//        if (mapOfBalancesResult.result != SOAResultType.SUCCESS)
//            return SOAResult(SOAResultType.FAILURE, "Failed to retrieve balances.", null)
//        val mapOfBalances = mapOfBalancesResult.data!!
//
//        //TODO what should we do if any of the balances are negative but some are positive?
//        //TODO handle 'ALL' type reward audience -- get all chains
//        val providenceChainResult = GetProvidenceChainService.execute(transaction)
//        if(providenceChainResult.result != SOAResultType.SUCCESS)
//            return SOAResult(SOAResultType.FAILURE, providenceChainResult.message)
//
//        var resultingTransactions = mutableListOf<Transaction>()
//        // for now we will just distribute
//        mapOfBalances.forEach { tokenId, balance ->
//            if(balance <= 0.0)
//                return@forEach
//            val resultingTxs = transferRewardsToChain(reward.type.type, tokenId, balance, providenceChainResult.data!!, address)
//            resultingTransactions.addAll(resultingTxs)
//        }
//        return SOAResult(SOAResultType.SUCCESS, null, TransactionList(resultingTransactions))
//    }
//
////    private fun transferRewardsToAll(rewardTypeName: RewardTypeName, tokenId: Int, balance: Double, providenceChain: TransactionList): Transaction? {
////        // TODO -- handle ALL type reward audience
////    }
//
//    private fun transferRewardsToChain(
//        rewardTypeName: RewardTypeName,
//        tokenId: Int,
//        balance: Double,
//        providenceChain: TransactionList,
//        address: String
//    ): List<Transaction> {
//        var resultingTxs = mutableListOf<Transaction>()
//        val leafToParentProvidenceChain = providenceChain.transactions.asReversed()
//        when(rewardTypeName) {
//            RewardTypeName.EVEN -> {
//                // distribute reward evenly to everyone in the providence chain list
//                val size = leafToParentProvidenceChain.size
//                val amount = balance / size
//                leafToParentProvidenceChain.forEach { tx ->
//                    // TODO Add error handling for transfering.
//                    resultingTxs.add(TransferTokenHelper.transferToken(
//                        address,
//                        tx.to!!,
//                        amount,
//                        ActionType.PAYOUT,
//                        null,
//                        tokenId,
//                        null,
//                        "Reward distribution"
//                    ).data!!)
//                }
//            }
//            RewardTypeName.SINGLE -> {
//                // distribute reward to first person in providence chain list
//                val tx = leafToParentProvidenceChain.first()
//                // TODO Add error handling for transfering.
//                resultingTxs.add(TransferTokenHelper.transferToken(
//                    address,
//                    tx.to!!,
//                    balance,
//                    ActionType.PAYOUT,
//                    null,
//                    tokenId,
//                    null,
//                    "Reward distribution"
//                ).data!!)
//            }
//            RewardTypeName.LOGARITHMIC -> {
//                // distribute reward in logarithmic pattern
//                throw NotImplementedError()
//            }
//            RewardTypeName.EXPONENTIAL -> {
//                // distribute reward in exponential pattern
//                throw NotImplementedError()
//            }
//            RewardTypeName.N_OVER_2 -> {
//                // distribute reward in n/2 pattern
//                var n_over_2 = balance / 2
//                leafToParentProvidenceChain.forEach { tx ->
//                    // TODO Add error handling for transfering.
//                    resultingTxs.add(TransferTokenHelper.transferToken(
//                        address,
//                        tx.to!!,
//                        n_over_2,
//                        ActionType.PAYOUT,
//                        null,
//                        tokenId,
//                        null,
//                        "Reward distribution"
//                    ).data!!)
//                    n_over_2 /= 2
//                }
//            }
//        }
//        return resultingTxs
//    }
//}
