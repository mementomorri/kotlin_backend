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
import tutors
import java.time.LocalDate


class Course(
        val name: String,
        override var id: Int=-1
):Item {

    fun setToplist(){
        var nextID = transaction {
            toplist.selectAll()
                    .mapNotNull {
                        toplist.readResult(it)
                    }
        }.size
        students.read().forEach {student ->
            nextID++
            val currentRank = Rank(student.id, this.id, nextID)
            transaction {
                toplist.insertAndGetIdItem(currentRank).value
                true
            }
        }
    }

    fun setGrade(taskName: String, studentName: String, value: Int, date: LocalDate = LocalDate.now()) {
        val task = this.getTasks().find { it.name == taskName } ?: return
        val student = students.read().find { it.name == studentName } ?: return
        if (value !in 0..task.maxValue) return
        val currentGrade = transaction {
            grades.selectAll().mapNotNull{grades.readResult(it)}
        }.firstOrNull { it.task_id == task.id && it.student_id == student.id }
        if (currentGrade == null || currentGrade.value < value) {
            val grade = Grade(value, date, student.id, task.id)
            transaction {
                grades.insertAndGetId {
                    fill(it, grade)
                }.value
                true
            }
        } else return
    }

    fun studentGrades(studentName: String, task_id:Int) : Int? {

        val student = students.read().find { it.name == studentName } ?: return null
        return transaction {
            grades.selectAll().mapNotNull { grades.readResult(it) }
        }.find { it.task_id == task_id && it.student_id == student.id}?.value
//        val result = mutableMapOf<Int, Int>()
//        transaction {
//            grades.selectAll().mapNotNull { grades.readResult(it) }
//        }.forEach { grade ->
//            if (grade.student_id == student.id) result[grade.task_id] to grade.value
//        }
//        return result.toMap()
    }

    fun getStudent(studentName: String): Student?{
        return students.read().find { it.name == studentName }
    }

    fun getTutor(tutorName: String): Tutor?{
        return tutors.read().find { it.name == tutorName }
    }

    fun getRankByName(studentName: String):Rank?{
        val student = students.read().find{it.name == studentName}
        return transaction {
            toplist.selectAll()
                    .mapNotNull {
                        toplist.readResult(it)
                    }
        }.find { it.student_id == student?.id && this.id == it.course_id}
    }

    fun getTasks() : List<Task>{
        val result = mutableListOf<Task>()
        transaction {
            tasks.selectAll().mapNotNull { tasks.readResult(it) }
        }.forEach { task ->
            if (task.course_id == this.id) result.add(task)
        }
        return result.toList()
    }

    fun getCourseToplist() : List<Rank>{
        val result = mutableListOf<Rank>()
        transaction {
            toplist.selectAll().mapNotNull { toplist.readResult(it) }
        }.forEach { rank ->
            if (rank.course_id == this.id) result.add(rank)
        }
        return result.toList()
    }
}

class CourseTable: ItemTable<Course>(){
    val name = varchar("name",50)
    // Todos: change this to usable version
//    val students = reference("students",CourseStudent)
//    val tutors = reference("tutors", CourseTutor)
//    val toplistRef = reference("toplist", toplist)
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