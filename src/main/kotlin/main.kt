import model.*
import org.jetbrains.exposed.dao.id.IntIdTable
import repo.RepoDSL

val tutors= RepoDSL(tutorTable)
val students= RepoDSL(studentTable)
val courses= RepoDSL(courseTable)


object CourseStudent: IntIdTable(){
    val course_id = reference("course_id", courseTable)
    val student_id = reference("student_id", studentTable)
}

object CourseTutor: IntIdTable(){
    val course_id = reference("course_id", courseTable)
    val tutor_id = reference("tutor_id", tutorTable)
}
