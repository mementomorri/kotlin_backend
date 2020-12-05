package model

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import repo.PersonTable

@Serializable
class Student (
    override val name: String,
    val group: String,
    override var id: Int=-1
): Person {
    override fun toString(): String {
        return "Student with id=$id, name=$name, group=$group"
    }
}

class StudentTable : PersonTable<Student>() {
    val name = varchar("name", 255)
    val group = varchar("goroup", 70)

    override fun fill(builder: UpdateBuilder<Int>, person: Student) {
        builder[name] = person.name
        builder[group] = person.group
    }

    override fun readResult(result: ResultRow) =
            Student(
                    result[name],
                    result[group],
                    result[id].value
            )
}

val studentTable = StudentTable()