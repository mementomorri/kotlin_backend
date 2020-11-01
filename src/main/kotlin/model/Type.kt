package model

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import repo.Item
import repo.ItemTable

class Type(
        val name: String,
        val shortName: String ,
        override var id: Int=-1
): Item

class TypeTable : ItemTable<Type>() {
    val name = varchar("name", 50)
    val shortName = varchar("shortName", 20)
    override fun fill(builder: UpdateBuilder<Int>, item: Type) {
        builder[name] = item.name
        builder[shortName] = item.shortName
    }
    override fun readResult(result: ResultRow) =
            Type(
                    result[name],
                    result[shortName],
                    result[id].value
            )
}

val types = TypeTable()