package kotlinserverless.framework.models

import framework.models.BaseDocument
import framework.models.BaseObject
import framework.models.Handler
import main.services.aws.qldb.helpers.LedgerClient
import java.util.*


/**
 * Instead of having a generic response for everything now the Response class is an interface
 * and we create an specific implementation of it
 */
class ApiGatewayResponse(
        val statusCode: Int = 200,
        var body: Any? = null,
        val headers: Map<String, String>? = Collections.emptyMap(),
        val isBase64Encoded: Boolean = false
): Response {

  /**
   * Uses the Builder pattern to create the response
   */
  class Builder {
    var statusCode: Int = 200
    var rawBody: Any? = null
    var headers: Map<String, String>? = mapOf(
      "X-Powered-By" to "AWS Lambda & Serverless",
      "Access-Control-Allow-Origin" to "*",
      "Access-Control-Allow-Credentials" to "true",
      "Content-Type" to "application/json"
    )

    fun build(): ApiGatewayResponse {
      val body = getBody(rawBody)
      return  if(body != null && body !is String)
                ApiGatewayResponse(
                  statusCode,
                  Handler.gson.toJson(body),
                  headers
                )
              else
                ApiGatewayResponse(statusCode, body, headers)
    }

    companion object {
      private fun getBody(rawBody: Any?): Any? {
        return when (rawBody) {
          is List<*> -> {
            if ((rawBody as? List<*>)?.first() is BaseObject) {
              (rawBody as? List<*>)?.map { (it as? BaseObject) }
            } else {
              (rawBody as? List<*>)?.map { it.toString() }
            }
          }
          else -> rawBody
        }
      }
    }
  }
}