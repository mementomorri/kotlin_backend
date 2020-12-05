package model

import CourseStudentFiller
import CourseTutorFiller
import courseStudentTable
import courseTutorTable
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import students
import tutors
import java.time.LocalDate

@Serializable
class Course(
        val name: String,
        override var id: Int=-1
): Item {

    fun setToplist() {
        var nextID = transaction {
            toplistTable.selectAll().mapNotNull { toplistTable.readResult(it) }
        }.size
        getStudentsAtCourse().forEach { student ->
            nextID++
            val currentRank = Rank(student.id, this.id, nextID)
            val duplicate = transaction {
                toplistTable.selectAll().mapNotNull { toplistTable.readResult(it) }
            }.firstOrNull { it.student_id == student.id }
            if (duplicate == null) {
                transaction {
                    toplistTable.insertAndGetIdItem(currentRank).value
                    true
                }
            } else{
                transaction {
                    toplistTable.updateItem(duplicate.id, currentRank) > 0
                }
            }
        }
    }

    fun getCourseToplist(): List<Rank> {
        setToplist()
        val result = mutableListOf<Rank>()
        transaction {
            toplistTable.selectAll().mapNotNull { toplistTable.readResult(it) }
        }.forEach { rank ->
            if (rank.course_id == this.id) result.add(rank)
        }
        return result.toList()
    }

    fun setGrade(taskName: String, studentName: String, value: Int, date: LocalDate = LocalDate.now()) {
        val task = this.getTasks().find { it.name == taskName } ?: return
        val student = getStudentsAtCourse().find { it.name == studentName } ?: return
        if (value !in 0..task.maxValue) return
        val currentGrade = transaction {
            gradesTable.selectAll().mapNotNull { gradesTable.readResult(it) }
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

    fun studentGrades(studentName: String, task_id: Int): Int? {

        val student = getStudentsAtCourse().find { it.name == studentName } ?: return null
        return transaction {
            gradesTable.selectAll().mapNotNull { gradesTable.readResult(it) }
        }.find { it.task_id == task_id && it.student_id == student.id }?.value
    }

    fun getStudentsAtCourse(): List<Student> {
        val studentsAtCourse = transaction {
            courseStudentTable.selectAll().mapNotNull { courseStudentTable.readResult(it) }
        }.filter { it.course_id == this.id }
        val result = mutableListOf<Student>()
        studentsAtCourse.forEach {
            result.add(students.read(it.student_id)!!)
        }
        return result.toList()
    }

    fun getTutorsAtCourse(): List<Tutor> {
        val tutorsAtCourse = transaction {
            courseTutorTable.selectAll().mapNotNull { courseTutorTable.readResult(it) }
        }.filter { it.course_id == this.id }
        val result = mutableListOf<Tutor>()
        tutorsAtCourse.forEach {
            result.add(tutors.read(it.tutor_id)!!)
        }
        return result.toList()
    }

    fun getStudent(studentName: String): Student? {
        return getStudentsAtCourse().find { it.name == studentName }
    }

    fun getTutor(tutorName: String): Tutor? {
        return getTutorsAtCourse().find { it.name == tutorName }
    }

    fun getRankByName(studentName: String): Rank? {
        val student = getStudentsAtCourse().find { it.name == studentName }
        return if (student != null) {
            transaction {
                toplistTable.selectAll().mapNotNull { toplistTable.readResult(it) }
            }.find { it.student_id == student.id && this.id == it.course_id }
        } else null
    }

    fun getTask(taskId: Int): Task?{
        return getTasks().find { it.id == taskId }
    }

    fun getTasks(): List<Task> {
        val result = mutableListOf<Task>()
        transaction {
            tasksTable.selectAll().mapNotNull { tasksTable.readResult(it) }
        }.forEach { task ->
            if (task.course_id == this.id) result.add(task)
        }
        return result.toList()
    }

    fun addTask(task: Task)= transaction {
            tasksTable.insertAndGetIdItem(task).value
            true
        }
    fun removeTask(taskId: Int)=  transaction {
            tasksTable.deleteWhere { (tasksTable.course_id eq this@Course.id) and (tasksTable.id eq taskId) } > 0
        }


    fun addTutorToCourse(tutorName: String): Boolean {
        val courseId = this.id
        val tutorId = tutors.read().firstOrNull { it.name == tutorName }?.id
        return if (tutorId != null) {
            transaction {
                courseTutorTable.insert { fill(it, CourseTutorFiller(courseId, tutorId)) }
                true
            }
            true
        } else  false
    }

    fun addTutorById(tutorId: Int): Boolean{
        if (tutors.read(tutorId) == null) return false
        return transaction {
            courseTutorTable.insert { fill(it, CourseTutorFiller(this@Course.id, tutorId)) }
            true
        }
    }

    fun addStudentById(studentId: Int): Boolean{
        if (students.read(studentId) == null) return false
        return transaction {
            courseStudentTable.insert { fill(it, CourseStudentFiller(this@Course.id, studentId)) }
            true
        }
    }

    fun addStudentAtCourse(studentName: String) : Boolean {
        val courseId = this.id
        val studentId = students.read().firstOrNull { it.name == studentName }?.id
        return if (studentId != null) {
            transaction {
                courseStudentTable.insert { fill(it, CourseStudentFiller(courseId, studentId)) }
                true
            }
        } else false
    }

    fun removeTutorFromCourse(tutorId: Int): Boolean{
        val courseId = this.id
        val tutor= tutors.read(tutorId)?.id
        return if (tutor != null){
            transaction {
                courseTutorTable.deleteWhere { (courseTutorTable.course_id eq courseId) and  (courseTutorTable.tutor_id eq tutorId) } > 0
            }
//            true
        } else false
    }

    fun removeStudentFromCourse(studentId: Int): Boolean{
        val courseId = this.id
        val student= students.read(studentId)?.id
        return if (student != null){
            transaction {
                courseTutorTable.deleteWhere { (courseTutorTable.course_id eq courseId) and  (courseTutorTable.tutor_id eq studentId) } > 0
            }
//            true
        } else false
    }

    override fun toString(): String {
        return "Course with id=$id; name=$name"
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