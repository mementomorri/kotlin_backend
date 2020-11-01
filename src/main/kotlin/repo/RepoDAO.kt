package repo

import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.sql.transactions.transaction

open class RepoDAO<T : Item>(
        val companion: IntEntityClass<ItemClass<T>>
) : Repo<T> {

    override fun create(element: T) =
            transaction {
                companion.new {
                    fill(element)
                }
                true
            }

    override fun read(id: Int) =
            transaction {
                companion.findById(id)?.obj
            }

    override fun read() =
            transaction {
                companion.all().map {
                    it.obj
                }
            }

    override fun update(id: Int, element: T)=
            transaction {
                companion.findById(id)?.run {
                    fill(element)
                    true
                } ?: false
            }

    override fun delete(id: Int)=
            transaction {
                companion.findById(id)?.run {
                    delete()
                    true
                } ?: false
            }
}