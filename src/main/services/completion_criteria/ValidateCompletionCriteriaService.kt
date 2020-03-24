package main.services.completion_criteria
//
//import kotlinserverless.framework.services.SOAResult
//import kotlinserverless.framework.services.SOAResultType
//import main.daos.CompletionCriteria
//
///**
// * For the time being, just validate the caller is the criteria address
// */
//object ValidateCompletionCriteriaService {
//    fun execute(caller: String, completionCriteria: CompletionCriteria) : SOAResult<Boolean> {
//        // TODO -- eventually move this logic to the completion criteria
//        // TODO -- in future there will be different completion criteria types, and
//        // TODO -- the object should decide if it is valid or not
//
//        val success = caller == completionCriteria.address
//        return SOAResult(SOAResultType.SUCCESS, if(!success) "This public key cannot change the challenge state" else null, success)
//    }
//}