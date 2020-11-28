package repo

import model.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class PersonRepo<T : Person>(
        private val table: PersonTable<T>
) : Repo<T> {

    override fun create(element: T) =
            transaction {
                table.insertAndGetIdItem(element).value
                true
            }

    override fun read(id: Int) =
            transaction {
                table
                        .select { table.id eq id }
                        .firstOrNull()
                        ?.let {
                            table.readResult(it)
                        }
            }

    override fun update(id: Int, element: T) =
            transaction {
                table.updateItem(id, element) > 0
            }

    override fun delete(id: Int) =
            transaction {
                table.deleteWhere { table.id eq id } > 0
            }

    override fun read() =
            transaction {
                table.selectAll()
                        .mapNotNull {
                            table.readResult(it)
                        }
            }
    fun getStudentSByGroup(group: String): List<Student>?{
        return if (table is StudentTable) {
            transaction {
                table.selectAll().mapNotNull { table.readResult(it) }
            }.filter { it.group == group }
        } else null
    }

    fun getTutorsByPost(post: String): List<Tutor>?{
        return if (table is TutorTable){
            transaction {
                table.selectAll().mapNotNull { table.readResult(it) }
            }.filter { it.post == post }
        } else null
    }
}