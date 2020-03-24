package kotlinserverless.framework.controllers

import framework.models.BaseObject
import main.helpers.ControllerHelper

/**
 * Controller that receives a request and reply with a message of type {@link M}
 * @param M Model type
 */
interface Controller<M> {
    /**
     * Http router, it receives a class type to return, a request and service to automatically execute and return a response
     * @param incls Class input type
     * @param outcls Class return type
     * @param request Http Client request
     * @param service CRUD service to execute
     */
    fun <T : BaseObject> defaultRouting(
        inputClass: String,
        outcls: Class<T>,
        requestData: ControllerHelper.RequestData,
        restController: RestController<T>,
        method: String
    ): Any {
        if(method == ControllerHelper.HTTP_POST)
            return restController.create(requestData)

        return when(method) {
            ControllerHelper.HTTP_GET -> {
                when {
                    requestData.queryParams.containsKey("identifier") -> {
                        restController.findOne(
                            requestData,
                            requestData.queryParams["identifier"].toString()
                        )
                    }
                    else -> {
                        return restController.findAll(requestData)
                    }
                }
            }
            ControllerHelper.HTTP_PUT -> {
                restController.update(requestData)
            }
            ControllerHelper.HTTP_DELETE -> {
                restController.delete(requestData)
            }
            ControllerHelper.HTTP_PATCH -> {
                restController.update(requestData)
            }

            else -> {
                throw Exception()
            }
        }
    }
}