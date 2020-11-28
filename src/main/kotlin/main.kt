import model.*
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import repo.CourseRepo
import repo.PersonRepo
import repo.ItemRepo

val tutors= PersonRepo(tutorTable)
val students= PersonRepo(studentTable)
val courses= CourseRepo(courseTable)

class CourseStudentFiller(
        val course_id: Int,
        val student_id: Int,
        val id: Int= -1
)

class CourseTutorFiller(
        val course_id: Int,
        val tutor_id: Int,
        val id: Int= -1
)

class CourseStudentTable: IntIdTable(){
    val course_id = reference("course_id", courseTable)
    val student_id = reference("student_id", studentTable)

    fun fill(builder: UpdateBuilder<Int>, item: CourseStudentFiller) {
        builder[course_id] = item.course_id
        builder[student_id] = item.student_id
    }
    fun readResult(result: ResultRow) =
            CourseStudentFiller(
                    result[course_id].value,
                    result[student_id].value,
                    result[id].value
            )
}

class CourseTutorTable: IntIdTable(){
    val course_id = reference("course_id", courseTable)
    val tutor_id = reference("tutor_id", tutorTable)

    fun fill(builder: UpdateBuilder<Int>, item: CourseTutorFiller) {
        builder[course_id] = item.course_id
        builder[tutor_id] = item.tutor_id
    }
    fun readResult(result: ResultRow) =
            CourseTutorFiller(
                    result[course_id].value,
                    result[tutor_id].value,
                    result[id].value
            )
}

val courseStudentTable= CourseStudentTable()
val courseTutorTable= CourseTutorTable()