package framework.models

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.bugsnag.Bugsnag
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import framework.dispatchers.RequestDispatcher
import kotlinserverless.framework.models.ApiGatewayRequest
import kotlinserverless.framework.models.ApiGatewayResponse
import kotlinserverless.framework.models.MyException
import main.services.aws.qldb.LedgerService
import org.apache.log4j.BasicConfigurator
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder


open class Handler(val test: Boolean = false): RequestHandler<Map<String, Any>, ApiGatewayResponse> {

  var requestDispatcher: RequestDispatcher = RequestDispatcher()

  init {
    TEST = test
    if(!test) {
      BasicConfigurator.configure()
    }
  }

  override fun handleRequest(input: Map<String, Any>, context: Context?): ApiGatewayResponse {
    var status = 500
    var body: Any? = null

    try {
      body = requestDispatcher.locate(ApiGatewayRequest(input, context))

      status = when (body == null) {
        true -> 204
        false -> 200
      }
    }
    catch (e: MyException) {
      System.err.println(e.message)
      status = e.code
      body = e.message
    }
    catch (e: Throwable) {
      System.err.println(e.message)
      e.printStackTrace()
      status = 500
      body = "Internal server error " + e.message.toString() + "\n" + e.stackTrace.map { "\n"+it.toString() }
    }
    finally {
      return build {
        statusCode = status
        rawBody = body
      }
    }
  }

  companion object {
    inline fun build(block: ApiGatewayResponse.Builder.() -> Unit) = ApiGatewayResponse.Builder().apply(block).build()

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")

    private val gsonDeserializer = JsonDeserializer<LocalDateTime>  { json, _, _ ->
      LocalDateTime.parse(json.asJsonPrimitive.asString.removeSuffix("Z"), dateTimeFormatter)
    }

    private val gsonSerializer = JsonSerializer<LocalDateTime>  { localDateTime, _, _ ->
      JsonPrimitive(localDateTime.format(dateTimeFormatter))
    }

    val gson = GsonBuilder()
      .registerTypeAdapter(LocalDateTime::class.java, gsonDeserializer)
      .registerTypeAdapter(LocalDateTime::class.java, gsonSerializer)
      .create()!!

    var TEST = false

    private var bugsnagInstance: Bugsnag? = null
    private fun bugsnag(): Bugsnag {
      if(bugsnagInstance == null) {
        bugsnagInstance = Bugsnag(System.getenv("bugsnag_api_key") ?: "local")
        bugsnagInstance!!.setNotifyReleaseStages("production", "development")
        bugsnagInstance!!.setReleaseStage(System.getenv("release_stage") ?: "local")
      }
      return bugsnagInstance!!
    }

    fun disconnectAndDropLedger() {
      LedgerService.dropLedger(LedgerService.LEDGER_NAME)
    }

    fun clearLedger() {
      if(!TEST)
        return
      LedgerService.clearLedgerTables()
    }
  }
}