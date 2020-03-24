package kotlinserverless.framework.controllers

import kotlinserverless.framework.healthchecks.InvalidEndpoint
import main.daos.Healthcheck
import main.helpers.ControllerHelper.RequestData

/**
 * Service that exposes the capabilities of a {@link T} element
 * @param <K> Natural Key type
 * @param <T> Element type
 * @param <F> Filter type
 */
interface ReadableController<T> {
//    /**
//     * Find a set of [T] by a given set of optional parameters
//     * @param publickKey [P] who is requesting (to verify permissions)
//     * @param filters Optional parameters
//     * @param pagination How to paginate the result
//     * @return list of [T]
//     */
//    fun findAll(publickKey: P, filters: Map<String, Any> = mapOf( "order" to "creationDate" ),
//                pagination: Pagination = Pagination(0, 50)): SOAResult<Page<T>>{
//        throw InvalidEndpoint()
//    }

    fun findAll(requestData: RequestData): List<T> {
        throw InvalidEndpoint()
    }

    /**
     * Finds one [T] by the unique ID
     * @param id Unique id
     * @return [T] that has that ID
     */
    fun findOne(requestData: RequestData, identifier: String?): T {
        throw InvalidEndpoint()
    }

    /**
     * Returns the amount or [T] entities in the system
     * @param filters Set of filters
     * @return list of [T]
     */
    fun count(requestData: RequestData): Int {
        throw InvalidEndpoint()
    }

    /**
     * Verifies if a entity with a specific ID exists
     * @param id Unique id
     * @return [Boolean] value indicating true if exists or false if not
     */
    fun exists(requestData: RequestData): Boolean {
        throw InvalidEndpoint()
    }

    /**
     * Provide a basic healthcheck for this object type and function
     * ex: used to verify access to the database/cache layer is functioning properly
     * @return [Healthcheck] object representing the health
     */
    fun health(requestData: RequestData): Healthcheck {
        throw InvalidEndpoint()
    }
}