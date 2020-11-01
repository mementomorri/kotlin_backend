package model


import CourseStudent
import CourseTutor
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import repo.Item
import repo.ItemTable
import students
import tasks
import toplist
import tutors
import java.time.LocalDate


class Course(
        val name: String,
        override var id: Int=-1
):Item {

    fun setToplist(){
        var nextID = toplist.read().size
        students.read().forEach {student ->
            nextID++
            val currentRank = Rank(student.id, this.id, nextID)
            toplist.create(currentRank)
        }
    }

    fun setGrade(taskName: String, studentName: String, value: Int, date: LocalDate = LocalDate.now()) {
        val task = tasks.read().find { it.name == taskName } ?: return
        val student = students.read().find { it.name == studentName } ?: return
        if (value !in 0..task.maxValue) return
        val currentGrade = transaction {
            Grades.selectAll().mapNotNull{Grades.readResult(it)}
                    .firstOrNull { it.task_id == task.id && it.student_id == student.id }
        }
        if (currentGrade == null || currentGrade.value < value) {
            val grade = Grade(value, date, student.id, student.id)
            transaction {
                Grades.insertAndGetId {
                    fill(it, grade)
                }
            }
        } else return
    }

    fun studentGrades(studentName: String) : Map<EntityID<Int>, Int>? {
        val student = students.read().find { it.name == studentName } ?: return null
        val studentsGrades = mutableMapOf<EntityID<Int>, Int>()
        transaction {
            Grades.select{
                Grades.student_id eq EntityID(student.id, studentTable)
            }.forEach { studentsGrades[it[Grades.task_id]] = it[Grades.value]  }
        }
        return studentsGrades.toMap()
    }

    fun getStudent(studentName: String): Student?{
        return students.read().find { it.name == studentName }
    }

    fun getTutor(tutorName: String): Tutor?{
        return tutors.read().find { it.name == tutorName }
    }

    fun getRankByName(studentName: String):Rank?{
        val student = students.read().find{it.name == studentName}
        return toplist.read().find { it.student_id == student?.id && this.id == it.course_id}
    }

    fun getTask(taskName:String): Task? {
        return tasks.read().find { it.name == taskName }
    }

}

class CourseTable: ItemTable<Course>(){
    val name = varchar("name",50)
    val student = reference("students",CourseStudent)
    val tutors = reference("tutors", CourseTutor)
    val toplist = reference("toplist", toplistTable)
    override fun fill(builder: UpdateBuilder<Int>, item: Course) {
        builder[name] = item.name
}
    override fun readResult(result: ResultRow) =
            Course(
                    result[name],
                    result[id].value
            )
}

val courseTable = CourseTable()