package repo


import model.Person
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.update

abstract class PersonTable<T: Person>
    : IntIdTable(){
    abstract fun fill(builder: UpdateBuilder<Int>, person: T): Unit
    abstract fun readResult(result: ResultRow): T?

    fun insertAndGetIdItem(person: T) =
            insertAndGetId {
                fill(it, person)
            }

    fun updateItem(id: Int, dto: T) =
            update({
                this@PersonTable.id eq id
            }){
                fill(it, dto)
            }
}