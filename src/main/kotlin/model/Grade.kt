package model

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.`java-time`.date
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import java.time.LocalDate

class Grade (
    val value: Int,
    val date: LocalDate = LocalDate.now(),
    val student_id: Int,
    val task_id: Int
)

class GradesTable : IntIdTable() {
    val value = integer("value")
    val date = date("date")
    val student_id = reference("student_ID", studentTable)
    val task_id = reference("task_ID", tasksTable)
    fun fill(builder: UpdateBuilder<Int>, grade: Grade) {
        builder[value] = grade.value
        builder[date] = grade.date
        builder[student_id] = grade.student_id
        builder[task_id] = grade.task_id
    }
    fun readResult(result: ResultRow)=
            Grade(
                    result[value],
                    result[date],
                    result[student_id].value,
                    result[task_id].value
            )
}

val gradesTable= GradesTable()