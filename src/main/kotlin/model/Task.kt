package model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.`java-time`.date
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import repo.ItemTable
import students
import java.time.LocalDate

@Serializable
class Task(
        val name: String,
        val type_id: Int,
        val course_id: Int,
        val description: String = "",
        val maxValue: Int = 1,
        @Contextual
        val deadline: LocalDate = LocalDate.now(),
        override var id: Int=-1,
) : Item {

    fun getGrade(studentName: String): Int{
        val student = students.read().find { it.name == studentName }
        return transaction {
            gradesTable.select {
                gradesTable.student_id eq student?.id
            }.firstOrNull()?.let { gradesTable.readResult(it) }
        }?.value ?: 0
    }

    fun getGrades()= transaction {
        gradesTable.selectAll().mapNotNull { gradesTable.readResult(it) }
    }.filter { it.task_id == this.id }

    fun addGrade(grade: Grade)= transaction {
        gradesTable.insertAndGetId { fill(it, grade) }.value
        true
    }
}

class TaskTable : ItemTable<Task>() {
    val name = varchar("name", 255)
    val type_id = reference("type_ID", typesTable)
    val course_id = reference("course_id", courseTable)
    val description = varchar("description", 255)
    val maxValue = integer("maxValue")
    val deadline = date("deadline")
    override fun fill(builder: UpdateBuilder<Int>, item: Task) {
        builder[name] = item.name
        builder[type_id] = item.type_id
        builder[course_id] = item.course_id
        builder[description] = item.description
        builder[maxValue] = item.maxValue
        builder[deadline] = item.deadline
    }
    override fun readResult(result: ResultRow) =
            Task(
                    result[name],
                    result[type_id].value,
                    result[course_id].value,
                    result[description],
                    result[maxValue],
                    result[deadline],
                    result[id].value
            )
}

val tasksTable= TaskTable()