package kotlinserverless.framework.controllers

import framework.models.BaseObject
import framework.models.Handler
import kotlinserverless.framework.models.*
import main.helpers.ControllerHelper
import java.lang.reflect.InvocationTargetException

open class DefaultController<T: BaseObject> : Controller<T> {

	@Throws(ForbiddenException::class)
	override fun <T : BaseObject> defaultRouting(
		incls: String,
		outcls: Class<T>,
		requestData: ControllerHelper.RequestData,
		restController: RestController<T>,
		method: String
    ): Any {
		val pathString = requestData.request.input["path"].toString()

		val path = pathString.removePrefix("/").split("/")

		if(path.size > 1) {
			val func = restController::class.members.find { it.name == path[1] }
			if(func != null) {
				try {
					return func.call(restController, requestData)!!
				}
				catch(e: InvocationTargetException) {
					System.err.println(e.message)
					e.printStackTrace()
					throw e.targetException
				}
				catch(e: Exception) {
					System.err.println("There was an error routing")
					e.printStackTrace()
					return super.defaultRouting(
						incls,
						outcls,
						requestData,
						restController,
						method
					)
				}
			}
		}
		return super.defaultRouting(
			incls,
			outcls,
			requestData,
			restController,
			method
		)
	}
}