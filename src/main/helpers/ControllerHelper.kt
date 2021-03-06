package main.helpers

import kotlinserverless.framework.models.Pagination
import kotlinserverless.framework.models.Request
import kotlin.math.ceil
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTCreator
import com.auth0.jwt.algorithms.Algorithm
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import framework.models.Handler

object ControllerHelper {

    // HTTP constants
    const val HTTP_METHOD: String = "httpMethod"
    const val HTTP_GET: String = "get"
    const val HTTP_POST: String = "post"
    const val HTTP_PUT: String = "put"
    const val HTTP_DELETE: String = "delete"
    const val HTTP_PATCH: String = "patch"

    // Pagination constants
    const val LIMIT: Int = 50
    const val MAX_LIMIT: Int = 100
    const val OFFSET: Int = 0
    const val MIN_LIMIT: Int = 0

    private fun anyToInt(value: Any, valueType: String = "page"): Int {
        return when (value) {
            is String -> value.toIntOrNull() ?: throw Exception("$valueType must be a number")
            is Int -> value
            else -> throw Exception("$valueType must be a number")
        }
    }

    private fun getStringAnyMap(request: Request, key: String): Map<String, Any> {
        return if (request.input.containsKey(key) && request.input[key] != null)
            if(request.input[key] !is String)
                request.input[key] as Map<String, Any>
            else
                Handler.gson.fromJson(request.input[key] as String, object : TypeToken<Map<String, Any>>() { }.type)
        else
            emptyMap()
    }

    private fun getResource(request: Request): String? {
        return request.input["resource"] as String?
    }

    private fun getHeaders(request: Request) : Map<String, Any> {
        return getStringAnyMap(request, "headers")
    }

    private fun getPathParameters(request: Request) : Map<String, Any> {
        return getStringAnyMap(request, "pathParameters")
    }

    private fun getQueryStringParameters(request: Request) : Map<String, Any> {
        return getStringAnyMap(request, "queryStringParameters")
    }

    private fun getBody(request: Request) : String? {
        return request.input["body"] as String?
    }

    private fun getPagination(queryParameters: Map<String, Any>): Pagination {
        var page: Int = ceil(OFFSET.toDouble() / LIMIT).toInt()
        var size: Int = LIMIT
        val pagination = Pagination(page, size)

        if (queryParameters.containsKey("page")) {
            page = anyToInt(queryParameters["page"]!!)
            pagination.page = if (page >= MIN_LIMIT + 1) page - 1 else MIN_LIMIT
        }

        if (queryParameters.containsKey("size")) {
            size = anyToInt(queryParameters["size"]!!, "size")
            pagination.size = if (size < MAX_LIMIT) size else MAX_LIMIT
        }

        return pagination

    }

    private fun getPublicKey(headers: Map<String, Any>): String {
        return JWT.decode((headers["Authorization"] as String).replace("Bearer ", "")).getClaim("publicKey").asString()
    }

    fun getRequestData(request: Request): RequestData {
        val queryParams = getQueryStringParameters(request)
        val headers = getHeaders(request)
        return RequestData(
            request,
            getResource(request),
            headers,
            getPathParameters(request),
            queryParams,
            getBody(request),
            getPagination(queryParams),
            getPublicKey(headers)
        )
    }

    data class RequestData(
        val request: Request,
        val resource: String?,
        val headers: Map<String, Any>,
        val pathParams: Map<String, Any>,
        val queryParams: Map<String, Any>,
        val body: String?,
        val pagination: Pagination,
        val publicKey: String
    )
}