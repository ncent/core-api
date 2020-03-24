package kotlinserverless.framework.controllers

import kotlinserverless.framework.healthchecks.InvalidEndpoint
import main.helpers.ControllerHelper.RequestData

/**
 * Service that exposes the capabilities of a {@link T} element
 * @param <K> Natural Key type
 * @param <T> Element type
 * @param <F> Filter type
 */
interface WritableController<T> {
    /**
     * Creates a [T] in the system
     * @param element [T] that is going to be saved
     */
    fun create(requestData: RequestData): T {
        throw InvalidEndpoint()
    }

    /**
     * Updates a [T]
     * @param element [T] that is going to be updated
     */
    fun update(requestData: RequestData): T {
        throw InvalidEndpoint()
    }

    /**
     * Deletes a [T] given a unique ID
     * @param id Unique ID of the [T]
     */
    fun delete(requestData: RequestData): Boolean {
        throw InvalidEndpoint()
    }
}