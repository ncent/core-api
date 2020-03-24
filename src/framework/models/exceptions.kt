package kotlinserverless.framework.models

/**
 * Custom exception for our project
 * @property code HTTP code that the client is going to receive
 * @property message Exception message
 * @constructor By default it will reply with a 500 HTTP method
 */
abstract class MyException(var code: Int = 500, override var message: String? = "Internal Exception"): Exception(message)

/**
 * Exception thrown when the body doesn't contain all the required fields
 * @property body Input body
 */
class InvalidArguments(private var body: String?) :
    MyException(400, "The entity $body doesn't contain all the required fields")

/**
 * Router Exception (request-dispatcher)
 * @property resource The route/resource that failed
 * @constructor The default exception is 404 not found exception
 */
class RouterException(private var resource: String?) :
    MyException(404, "The route/resource $resource doesn't exist")

/**
 * Not Found Exception
 * @property message The error message to display
 * @constructor The default exception is 404 not found
 */
class NotFoundException(override var message: String? = "Not Found") :
    MyException(404, message)

/**
 * Forbidden Exception
 * @property message The error message to display
 * @constructor The default exception is 403 forbidden
 */
class ForbiddenException(override var message: String? = "Operation forbidden") :
    MyException(403, message)

class NotEnoughSharesException(override var message: String? = "Not enough shares") :
    MyException(400, message)

class UnauthorizedError(override var message: String? = "Invalid api credentials"): MyException(401, message)
class DuplicateEntryError(override var message: String? = "Entry already exists"): MyException(409, message)