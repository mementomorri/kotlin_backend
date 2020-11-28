import model.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

private fun addTutorByName(name: String, post: String="Professor", id: Int=-1) {
    if (tutors.read().find { it.name == name } == null){
        if (id == -1) tutors.create(Tutor(name = name, post = post)) else tutors.create(Tutor(name, post, id))
    } else return
}

private fun addStudentByName(name: String, group: String= "Main group", id: Int=-1){
    if (students.read().find { it.name == name } == null){
        if (id == -1) students.create(Student(name = name, group = group)) else students.create(Student(name, group, id))
    } else return
}

private fun addCourseByName( name: String, id: Int=-1){
    if (courses.read().find { it.name == name } == null){
        if (id == -1) courses.create(Course(name = name)) else courses.create(Course(name, id))
    } else return
}

private fun putTutorToCourse(tutorName: String, courseName: String){
    val courseId= courses.read().firstOrNull{it.name == courseName}?.id
    val tutorId= tutors.read().firstOrNull{it.name == tutorName}?.id
    if (courseId != null && tutorId != null) {
        transaction {
            courseTutorTable.insert { fill(it, CourseTutorFiller(courseId, tutorId)) }
        }
    } else return
}

private fun putStudentAtCourse(studentName: String, courseName: String){
    val courseId= courses.read().firstOrNull{it.name == courseName}?.id
    val studentId= students.read().firstOrNull{it.name == studentName}?.id
    if (courseId != null && studentId != null) {
        transaction {
            courseStudentTable.insert { fill(it, CourseStudentFiller(courseId, studentId)) }
        }
    } else return
}

fun setGradeTest() {
    val math = courses.read().find { it.name=="Math" } ?: fail()
    math.setGrade("UML", "Howard", 1)
    val umlTask = math.getTasks().find { it.name == "UML" }?: fail()
    assertEquals(
            1,
            math.studentGrades("Howard",umlTask.id)
    )
}

fun studentGradesTest() {
    val math = courses.read().find { it.name=="Math" } ?: fail()
    math.setGrade("Intro", "Penny", 1)
    math.setGrade("Uml lab", "Penny", 3)
    val umlLabTask = math.getTasks().find { it.name == "Uml lab" }?: fail()
    val introTask = math.getTasks().find { it.name == "Intro" }?: fail()
    assertEquals(1, math.studentGrades("Penny",introTask.id))
    assertEquals(3, math.studentGrades("Penny", umlLabTask.id))
}

fun tutorSetToplistTest(){
    val math = courses.read().find { it.name=="Math" } ?: fail()
    math.setGrade("Uml lab", "Howard", 3)
    math.setToplist()
    assertEquals(0.1, math.getRankByName("Penny")?.rank)
    assertEquals(0.3, math.getRankByName("Howard")?.rank)
}

fun studentOrTutorReadRankTest(){
    val math = courses.read().find { it.name=="Math" } ?: fail()
    math.setToplist()
    val howardRank = math.getRankByName("Howard")?: fail()
    val pennyRank = math.getRankByName("Penny") ?: fail()
    assertEquals(0.3, howardRank.rank)
    assertEquals(0.1, pennyRank.rank)
}

fun studentOrTutorReadGradesTest(){
    val math = courses.read().find { it.name=="Math" } ?: fail()
    val umlLabTask = math.getTasks().find { it.name == "Uml lab" }?: fail()
    assertEquals(3, math.studentGrades("Penny", umlLabTask.id))
    assertEquals(5, math.studentGrades("Howard",umlLabTask.id))
}

fun tutorSetGradeTest(){
    val math = courses.read().find { it.name=="Math" } ?: fail()
    math.setGrade("Intro","Howard",1)
    math.setGrade("Uml lab", "Howard",5)
    val umlLabTask = math.getTasks().find { it.name == "Uml lab" }?: fail()
    val introTask = math.getTasks().find { it.name == "Intro" }?: fail()
    assertEquals(1, math.studentGrades("Howard",introTask.id))
    assertEquals(5, math.studentGrades("Howard", umlLabTask.id))
}

fun tutorSetTaskTest(){
    val math = courses.read().find { it.name=="Math" } ?: fail()
    val lecType = transaction {
        typesTable.selectAll().mapNotNull { typesTable.readResult(it) }
    }.find { it.name == "Lecture" }?: fail()
    val labType=transaction {
        typesTable.selectAll().mapNotNull { typesTable.readResult(it) }
    }.find { it.name == "Laboratory" }?: fail()
    transaction {
        tasksTable.insertAndGetIdItem(Task("test1",lecType.id,math.id)).value
        true
    }
    transaction {
        tasksTable.insertAndGetIdItem(Task("test2",labType.id,math.id)).value
        true
    }
    transaction {
        tasksTable.insertAndGetIdItem(Task("test3",lecType.id,math.id)).value
        true
    }
    assertEquals("test1", math.getTasks().find { it.name == "test1" }!!.name)
    assertEquals("test2", math.getTasks().find { it.name == "test2" }!!.name)
    assertEquals("test3", math.getTasks().find { it.name == "test3" }!!.name)
}

fun tutorAddStudentToCourse(){
    val math = courses.read().find { it.name=="Math" } ?: fail()
    addStudentByName("Bob","Newcomers")
    putStudentAtCourse("Bob", "Math")
    addStudentByName("Charlie","Newcomers")
    putStudentAtCourse("Charlie", "Math")
    assertEquals("Bob", math.getStudent("Bob")?.name)
    assertEquals("Charlie", math.getStudent("Charlie")?.name)
}

fun adminSetCourseTest(){
    listOf("Rocket science", "Basic rocket piloting", "Space navigation").forEach {
        addCourseByName(it)
    }
    assertEquals("Rocket science", courses.read().find { it.name=="Rocket science" }?.name)
    assertEquals("Basic rocket piloting", courses.read().find { it.name=="Basic rocket piloting" }?.name)
    assertEquals("Space navigation", courses.read().find { it.name=="Space navigation" }?.name)
}

fun getStudentsByGroupTest(){
    assertEquals(2, students.getStudentSByGroup("Footprint on the Moon")?.size)
}

fun getTutorByPostTest(){
    assertEquals(2, tutors.getTutorsByPost("Professor")?.size)
}

class MainTest {

    @Test
    fun testAllUseCases() {
        Database.connect(
                "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
                driver = "org.h2.Driver"
        )
        transaction {
            SchemaUtils.create(tutorTable)
        }
        transaction {
            SchemaUtils.create(studentTable)
        }
        transaction {
            SchemaUtils.create(courseStudentTable)
        }
        transaction {
            SchemaUtils.create(courseTable)
        }
        transaction {
            SchemaUtils.create(courseTutorTable)
        }
        transaction {
            SchemaUtils.create(tasksTable)
        }
        transaction {
            SchemaUtils.create(gradesTable)
        }
        transaction {
            SchemaUtils.create(toplistTable)
        }

        transaction {
            SchemaUtils.create(typesTable)
        }

        mapOf(
            "Sheldon" to "Professor",
            "Leonard" to "Professor"
        ).forEach {
            addTutorByName(it.key, it.value)
        }
        mapOf(
            "Howard" to "Footprint on the Moon",
            "Raj" to "Footprint on the Moon",
            "Penny" to "Waitress"
        ).forEach {
            addStudentByName(it.key, it.value)
        }
        addCourseByName("Math")
        addCourseByName("Phys")
        addCourseByName("History")
        transaction {
            typesTable.insertAndGetIdItem(Type("Lecture", "lec")).value
            true
        }
        transaction {
            typesTable.insertAndGetIdItem(Type("Laboratory", "lab")).value
            true
        }
        val lecType = transaction {
            typesTable.selectAll().mapNotNull { typesTable.readResult(it) }
        }.find { it.name == "Lecture" }
        val labType=transaction {
            typesTable.selectAll().mapNotNull { typesTable.readResult(it) }
        }.find { it.name == "Laboratory" }

        val math = courses.read().find { it.name =="Math"} ?: fail("Wrong course name")
        courses.read()
                .find {it.name == "Math"}?.run {
            addTutorByName("Sheldon")
            putTutorToCourse("Sheldon", "Math")
            addStudentByName("Howard")
            putStudentAtCourse("Howard", "Math")
            addStudentByName("Penny")
            putStudentAtCourse("Penny", "Math")
            transaction {
                tasksTable.insertAndGetIdItem(Task("Intro",lecType!!.id,math.id)).value
                true
            }
            transaction {
                tasksTable.insertAndGetIdItem(Task("UML",lecType!!.id,math.id)).value
                true
            }
            transaction {
                tasksTable.insertAndGetIdItem(Task("Uml lab",labType!!.id,math.id, maxValue = 5)).value
                true
            }
            setGrade("Uml lab", "Howard", 5)
            setGrade("Uml lab", "Penny", 3)
            setGrade("Intro", "Penny", 1)
        }

        setGradeTest()
        studentGradesTest()
        tutorSetToplistTest()
        studentOrTutorReadRankTest()
        studentOrTutorReadGradesTest()
        tutorSetGradeTest()
        tutorSetTaskTest()
        tutorAddStudentToCourse()
        adminSetCourseTest()
        getStudentsByGroupTest()
        getTutorByPostTest()

        transaction {
            SchemaUtils.drop(courseStudentTable)
        }
        transaction {
            SchemaUtils.drop(courseTutorTable)
        }
        transaction {
            SchemaUtils.drop(tutorTable)
        }
        transaction {
            SchemaUtils.drop(gradesTable)
        }
        transaction {
            SchemaUtils.drop(tasksTable)
        }
        transaction {
            SchemaUtils.drop(toplistTable)
        }
        transaction {
            SchemaUtils.drop(courseTable)
        }
        transaction {
            SchemaUtils.drop(studentTable)
        }
        transaction {
            SchemaUtils.drop(typesTable)
        }
    }
}