package main.services.reward

//import main.daos.Challenge
//import main.daos.Reward
//import main.daos.Transaction
//import main.services.token.TransferTokenService
//
///**
// * Transfer tokens to reward pool
// */
//object AddToRewardPoolService {
//    fun execute(caller: String, challengePublicKey: String, name: String, amount: Double) : Transaction {
//        val reward = Reward.findById(rewardId)!!
//        return TransferTokenService.execute(
//                caller,
//                reward.pool!!.cryptoKeyPair.publicKey,
//                amount,
//                name
//        )
//    }
//}