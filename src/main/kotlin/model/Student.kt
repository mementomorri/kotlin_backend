package model

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import repo.Item
import repo.ItemTable

class Student (
    val name: String,
    val group: String,
    override var id: Int=-1
): Item {}

class StudentTable : ItemTable<Student>() {
    val name = varchar("name", 255)
    val group = varchar("goroup", 70)
    override fun fill(builder: UpdateBuilder<Int>, item: Student) {
        builder[name] = item.name
        builder[group] = item.group
    }
    override fun readResult(result: ResultRow) =
            Student(
                    result[name],
                    result[group],
                    result[id].value
            )
}

val studentTable = StudentTable()