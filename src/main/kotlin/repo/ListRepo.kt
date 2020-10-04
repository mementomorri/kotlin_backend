package repo

class ListRepo<T: Item> : Repo<T> {
    private val shadow = ArrayList<T>()

    override fun add(element: T) =
        shadow.add(element)

    override operator fun get(name: String) =
        shadow.find { it.name == name }

    override fun all(): List<T> = shadow
}