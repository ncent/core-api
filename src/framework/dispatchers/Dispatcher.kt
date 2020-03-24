package framework.dispatchers

interface Dispatcher<in K, out T> {
    fun locate(request: K): T?
}