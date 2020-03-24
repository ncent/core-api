package main.helpers

//import main.daos.*
//import main.services.token.GetTokenService
//import main.services.transaction.GenerateTransactionService
//
//object TransferTokenHelper {
//
//    fun transferToken(
//        from: String,
//        to: String,
//        amount: Double,
//        type: ActionType,
//        name: String? = null,
//        id: Int? = null,
//        previousTransactionId: Int? = null,
//        notes: String? = null): Transaction {
//
//        var tokenId: Int
//        if (id == null && name == null)
//            return SOAResult(SOAResultType.FAILURE, "Must include either a token name or tokenId", null)
//        else if(id == null) {
//            val tokenResult = GetTokenService.execute(name!!)
//            if (tokenResult.result != SOAResultType.SUCCESS)
//                return SOAResult(tokenResult.result, tokenResult.message, null)
//            tokenId = tokenResult.data!!.tokenType.idValue
//        } else {tokenId = id}
//
//            // get the token transfer history for this address
//            val callerTransferHistory = getTransferHistory(from, tokenId)
//
//            // get and validate the public key balance vs what they wish to transfer
//            val callerBalance = calculateBalance(from, callerTransferHistory)
//            if(callerBalance < amount)
//                return SOAResult<Transaction>(SOAResultType.FAILURE, "Insufficient funds", null)
//
//            var metadataList = mutableListOf(MetadatasNamespace("amount", amount.toString()))
//            if(notes != null)
//                metadataList.add(MetadatasNamespace("notes", notes))
//
//            // TODO validate that the to address exists -- maybe in generate transaction
//
//            // generate a transaction moving funds
//            val transactionNamespace = TransactionNamespace(
//                from = from,
//                to = to,
//                action = ActionNamespace(
//                        type = type,
//                        data = tokenId,
//                        dataType = Token::class.simpleName!!
//                ),
//                previousTransaction = previousTransactionId,
//                metadatas = metadataList.toTypedArray()
//            )
//            return GenerateTransactionService.execute(transactionNamespace)
//    }
//
//    // join from and to this caller and the token -- this will get the history of transfers
//    // that this public key was a part of for this particular token
//    fun getTransferHistory(address: String, tokenId: Int? = null): List<Transaction> {
//        val expression = if(tokenId != null) {
//            (Transactions.from.eq(address) or Transactions.to.eq(address)) and
//                Actions.dataType.eq(Token::class.simpleName!!) and
//                Actions.data.eq(tokenId) and
//                Actions.type.eq(ActionType.TRANSFER)
//        } else {
//            (Transactions.from.eq(address) or Transactions.to.eq(address)) and
//                Actions.dataType.eq(Token::class.simpleName!!) and
//                Actions.type.eq(ActionType.TRANSFER)
//        }
//        val query = Transactions
//            .innerJoin(Actions)
//            .innerJoin(TransactionsMetadata)
//            .innerJoin(Metadatas)
//            .select {
//                expression
//            }.withDistinct()
//        return Transaction.wrapRows(query).toList().distinct()
//    }
//
//    // calculate balance based on transfers
//    // must pass list of transactions for a particular currency only
//    fun calculateBalance(address: String, transfers: List<Transaction>): Double {
//        var balance = 0.0
//        transfers.forEach { transfer ->
//            if(transfer.from == address) {
//                balance -= transfer.metadatas.find { it.key == "amount" }!!.value.toDouble()
//            } else if(transfer.to == address) {
//                balance += transfer.metadatas.find { it.key == "amount" }!!.value.toDouble()
//            }
//        }
//        return balance
//    }
//
//    fun getMapOfTransfersByCurrency(transfers: List<Transaction>): Map<String, MutableList<Transaction>> {
//        var currencyToTransactions = mutableMapOf<String, MutableList<Transaction>>()
//        transfers.forEach { transaction ->
//            currencyToTransactions.putIfAbsent(transaction.action.dataKey!!, mutableListOf())
//            currencyToTransactions[transaction.action.dataKey]!!.add(transaction)
//        }
//        return currencyToTransactions
//    }
//
//    fun getMapOfBalancesByCurrency(address: String): Map<String, Double> {
//        val mapOfTransfers = getMapOfTransfersByCurrency(
//            getTransferHistory(address, null)
//        )
//
//        val currencyToBalances = mutableMapOf<String, Double>()
//        mapOfTransfers.forEach { (currency_key, transactions) ->
//            currencyToBalances.putIfAbsent(currency_key, 0.0)
//            currencyToBalances[currency_key] =
//                    currencyToBalances[currency_key]!! + calculateBalance(address, transactions)
//        }
//        return currencyToBalances
//    }
//}