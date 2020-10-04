package repo

interface Repo<T : Item> {
    fun add(element: T): Boolean
    fun get(name: String): T?
    fun all(): List<T>
}

