package framework.dispatchers

import kotlinserverless.framework.models.*
import java.io.File
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import main.helpers.ControllerHelper
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.full.createInstance

/**
 * Request Dispatcher implementation
 */
open class RequestDispatcher: Dispatcher<ApiGatewayRequest, Any> {
    @Throws(RouterException::class, NotFoundException::class)
    override fun locate(request: ApiGatewayRequest): Any? {
        val path = request.input["path"]
        for ((regex, inputModel, outputModel, controller) in ROUTER.routes) {
			if (!Regex(regex).matches(path as CharSequence)) {
				continue
			}

            val outputModelClass = Class.forName(outputModel).kotlin
            val controllerClass = Class.forName(controller).kotlin
            val controllerInstance = controllerClass.createInstance()

            val func = controllerClass.members.find { it.name == "defaultRouting" }
            val requestData = ControllerHelper.getRequestData(request)
            val method = (requestData.request.input[ControllerHelper.HTTP_METHOD] as String).toLowerCase()

            try {
                return func?.call(
                    controllerInstance,
                    inputModel,
                    outputModelClass::class.java,
                    requestData,
                    controllerInstance,
                    method
                )
            }
            catch(e: InvocationTargetException) {
                throw e.targetException
            }
            catch(e: KotlinNullPointerException) {
                e.printStackTrace()
                throw NotFoundException()
            }
        }
		
		throw RouterException(path as? String ?: "")
    }

    /**
     * Singleton that loads the routes once and keep them on memory
     */
    companion object BackendRouter {
        private val FILE = File("routes.yml")
        val ROUTER: Routes = ObjectMapper(YAMLFactory()).readValue(FILE, Routes::class.java)
    }
}