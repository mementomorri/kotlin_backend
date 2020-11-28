package model

import courseStudentTable
import courseTutorTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
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
            toplistTable.selectAll().mapNotNull { toplistTable.readResult(it) }
        }.size
        getStudentsAtCourse()?.forEach {student ->
            nextID++
            val currentRank = Rank(student.id, this.id, nextID)
            transaction {
                toplistTable.insertAndGetIdItem(currentRank).value
            }
        }
    }

    fun setGrade(taskName: String, studentName: String, value: Int, date: LocalDate = LocalDate.now()) {
        val task = this.getTasks().find { it.name == taskName } ?: return
        val student = getStudentsAtCourse()?.find { it.name == studentName } ?: return
        if (value !in 0..task.maxValue) return
        val currentGrade = transaction {
            gradesTable.selectAll().mapNotNull{gradesTable.readResult(it)}
        }.firstOrNull { it.task_id == task.id && it.student_id == student.id }
        if (currentGrade == null || currentGrade.value < value) {
            val grade = Grade(value, date, student.id, task.id)
            transaction {
                gradesTable.insertAndGetId {
                    fill(it, grade)
                }.value
                true
            }
        } else return
    }

    fun studentGrades(studentName: String, task_id:Int) : Int? {

        val student = getStudentsAtCourse()?.find { it.name == studentName } ?: return null
        return transaction {
            gradesTable.selectAll().mapNotNull { gradesTable.readResult(it) }
        }.find { it.task_id == task_id && it.student_id == student.id}?.value
    }

    fun getStudentsAtCourse(): List<Student>?{
        val studentsAtCourse= transaction {
            courseStudentTable.selectAll().mapNotNull { courseStudentTable.readResult(it) }
        }.filter { it.course_id == this.id }
        return if (studentsAtCourse.isNotEmpty()) {
            val result = mutableListOf<Student>()
            studentsAtCourse.forEach {
                result.add(students.read(it.student_id)!!)
            }
            result.toList()
        } else null
    }

    fun getTutorsAtCourse(): List<Tutor>?{
        val tutorsAtCourse= transaction {
            courseTutorTable.selectAll().mapNotNull { courseTutorTable.readResult(it) }
        }.filter { it.course_id == this.id }
        return if (tutorsAtCourse.isNotEmpty()) {
            val result = mutableListOf<Tutor>()
            tutorsAtCourse.forEach {
                result.add(tutors.read(it.tutor_id)!!)
            }
            result.toList()
        } else null
    }

    fun getStudent(studentName: String): Student?{
        return getStudentsAtCourse()?.find { it.name == studentName }
    }

    fun getTutor(tutorName: String): Tutor?{
        return getTutorsAtCourse()?.find { it.name == tutorName }
    }

    fun getRankByName(studentName: String):Rank?{
        val student = getStudentsAtCourse()?.find{it.name == studentName}
        return if (student != null) {
            transaction {
                toplistTable.selectAll().mapNotNull { toplistTable.readResult(it) }
            }.find { it.student_id == student.id && this.id == it.course_id }
        } else null
    }

    fun getTasks() : List<Task>{
        val result = mutableListOf<Task>()
        transaction {
            tasksTable.selectAll().mapNotNull { tasksTable.readResult(it) }
        }.forEach { task ->
            if (task.course_id == this.id) result.add(task)
        }
        return result.toList()
    }

    fun getCourseToplist() : List<Rank>{
        val result = mutableListOf<Rank>()
        transaction {
            toplistTable.selectAll().mapNotNull { toplistTable.readResult(it) }
        }.forEach { rank ->
            if (rank.course_id == this.id) result.add(rank)
        }
        return result.toList()
    }
}

class CourseTable: IntIdTable(){
    val name = varchar("name",50)

    fun fill(builder: UpdateBuilder<Int>, item: Course) {
        builder[name] = item.name
}
    fun readResult(result: ResultRow) =
            Course(
                    result[name],
                    result[id].value
            )
    fun insertAndGetIdItem(course: Course) =
            insertAndGetId {
                fill(it, course)
            }

    fun updateItem(id: Int, dto: Course) =
            update({
                this@CourseTable.id eq id
            }){
                fill(it, dto)
            }
}

val courseTable = CourseTable()