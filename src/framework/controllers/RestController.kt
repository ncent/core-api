package kotlinserverless.framework.controllers

/**
 * Service that exposes the capabilities of a {@link T} element
 * @param <K> Natural Key type
 * @param <T> Element type
 * @param <F> Filter type
 */
interface RestController<T> : ReadableController<T>, WritableController<T>