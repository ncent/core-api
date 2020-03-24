package main.services.reward

//import kotlinserverless.framework.services.SOAResult
//import kotlinserverless.framework.services.SOAResultType
//import main.daos.*
//import main.helpers.CryptoHelper
//import org.jetbrains.exposed.sql.SizedCollection
//
///**
// * Generate a reward if it is valid
// */
//object GenerateRewardService {
//    fun execute(rewardNamespace: RewardNamespace) : SOAResult<Reward> {
//        // find or create a reward type
//        val rewardTypes = RewardType.find {
//            RewardTypes.audience eq rewardNamespace.type.audience
//            RewardTypes.type eq rewardNamespace.type.type
//        }
//
//        val rewardType = if(rewardTypes.empty()) {
//            RewardType.new {
//                audience = rewardNamespace.type.audience
//                type = rewardNamespace.type.type
//            }
//        } else {
//            rewardTypes.first()
//        }
//
//        val keyPair = CryptoHelper.generateCryptoKeyPair()
//
//        val newReward = Reward.new {
//            type = rewardType
//        }
//
//        val rewardPool = RewardPool.new {
//            cryptoKeyPair = keyPair
//        }
//
//        newReward.pool = rewardPool
//
//        if(rewardNamespace.metadatas != null) {
//            newReward.metadatas = SizedCollection(rewardNamespace.metadatas.map {
//                    md -> Metadata.new {
//                    key = md.key
//                    value = md.value
//                }
//            })
//        }
//
//        return SOAResult(SOAResultType.SUCCESS, null, newReward)
//    }
//}