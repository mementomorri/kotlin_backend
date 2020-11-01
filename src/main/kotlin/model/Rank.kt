package model

import courses
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import repo.Item
import repo.ItemTable
import students

class Rank (
        val student_id: Int,
        val course_id: Int,
        override var id: Int=-1
):Item{
    val rank
        get() = calculateRank()
    private val student = students.read(student_id)
    private val course = courses.read(course_id)

    private fun getTasks() : Map<Task, Grade> {
        val result = mutableMapOf<Task, Grade>()
        val tasksList = transaction {
            taskTable.select { taskTable.course_id eq EntityID(course!!.id, courseTable) }
                    .mapNotNull { taskTable.readResult(it) }
        }.toList()
        val studentGrades = transaction {
            Grades.select { Grades.student_id eq EntityID(student!!.id, studentTable) }
                    .mapNotNull { Grades.readResult(it) }
        }.toList()
        tasksList.forEach { task ->
            studentGrades.forEach { grade ->
                if (task.id == grade.task_id) {
                    result[task] = grade
                }
            }
        }
        return result.toMap()
    }

    private fun calculateRank():Double{
        var rankI = 0.0
        val taskToGradeMap = getTasks()
        if (course != null && student != null) {
            taskToGradeMap.forEach{ currentPair ->
                rankI+= (calculateWeight(currentPair.key.type_id)) * (currentPair.value.value / currentPair.key.maxValue)
            }
        }
        return String.format("%.2f", rankI).toDouble()
    }

    private fun calculateWeight(type_id: Int):Double{
        val type = transaction {
            TypeTable.select { TypeTable.id eq EntityID(type_id, taskTable) }
                    .firstOrNull()
                    ?.let {
                        TypeTable.readResult(it)
                    }
        }
        if (type != null) {
            return when (type.name) {
                "Lecture" -> 0.1
                "lecture" -> 0.1
                "LECTURE" -> 0.1
                "Laboratory" -> 0.2
                "laboratory" -> 0.2
                "LABORATORY" -> 0.2
                "Test" -> 0.3
                "test" -> 0.3
                "TEST" -> 0.3
                "Personal project" -> 0.4
                "personal project" -> 0.4
                "PERSONAL PROJECT" -> 0.4
                else -> 0.0
            }
        } else return 0.0
    }
}

class Toplist: ItemTable<Rank>(){
    val student_id = reference("student_id", studentTable)
    val course_id = reference("course_id", courseTable)
    val rank = double("rank")
    override fun fill(builder: UpdateBuilder<Int>, item: Rank) {
        builder[student_id] = item.student_id
        builder[course_id] = item.course_id
        builder[rank] = item.rank
    }

    override fun readResult(result: ResultRow): Rank? =
        Rank(

                result[student_id].value,
                result[course_id].value,
                result[id].value
        )
}

val toplistTable = Toplist()