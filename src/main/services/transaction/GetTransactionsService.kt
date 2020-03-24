package main.services.transaction

//import framework.models.Handler
//import main.daos.*
//
///**
// * Retrieve transactions by filter, such as from/to
// * "to" is required for now. need at least one field required
// */
//object GetTransactionsService {
//    fun execute(
//        from: String?,
//        to: String? = null,
//        previousTxId: String? = null,
//        action: Action? = null
//    ) : List<Transaction> {
//        var query = arrayListOf<Pair<String,String>>()
//
//        if(from != null)
//            query.add(Pair("from", from))
//        if(to != null)
//            query.add(Pair("to", to))
//
//        // TODO: think how to correct this.
//        if(previousTxId != null)
//            query.add(Pair("previousTransactionId", previousTxId))
//        if(action != null) {
//            if(action.dataId != null)
//                query.add(Pair("dataId", action.dataId))
//            query.add(Pair("type", action.type.type))
//            query.add(Pair("dataType", action.dataType))
//        }
//
//        return Handler.ledgerClient.readAll(*query.toTypedArray())
//    }
//}