import model.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

fun Course.addTutorByName(name: String) {
    persons[name]?.let {
        if (it is Tutor)
            tutors.add(it)
    }
}

fun Course.addStudentByName(name: String) {
    persons[name]?.let {
        if (it is Student)
            students.add(it)
    }
}

fun setCourse(courseName: String){
    if (courses[courseName] == null){
        courses.add(Course(courseName))
    }
}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MainTest {

    @BeforeAll
    fun init() {
        mapOf(
            "Sheldon" to "Professor",
            "Leonard" to "Professor"
        ).forEach {
            persons.add(Tutor(it.key, it.value))
        }
        mapOf(
            "Howard" to "Footprint on the Moon",
            "Raj" to "Footprint on the Moon",
            "Penny" to "Waitress"
        ).forEach {
            persons.add(Student(it.key, it.value))
        }
        listOf(
            "Math",
            "Phis",
            "History"
        ).forEach {
            courses.add(Course(it))
        }
        courses["Math"]?.run {
            addTutorByName("Sheldon")
            addStudentByName("Howard")
            addStudentByName("Penny")
            tasks.add(Task("Intro", Type.LECTURE))
            tasks.add(Task("UML", Type.LECTURE))
            tasks.add(Task("Uml lab", Type.LABORATORY, maxValue = 5))
        }
    }

    @Test
    fun initTest() {
        assertEquals(2, persons.all().filterIsInstance<Tutor>().size)
        assertEquals(3, persons.all().filterIsInstance<Student>().size)
        assertEquals(2, persons.all().filter {
            when (it) {
                is Student -> it.group == "Footprint on the Moon"
                else -> false
            }
        }.size)
        assertEquals(2, courses["Math"]?.students?.size)
        assertEquals(1, courses["Math"]?.tutors?.size)
    }

    @Test
    fun setGradeTest() {
        val math = courses["Math"] ?: fail()
        math.setGrade("UML", "Howard", 1)
        assertEquals(
            1,
            math.tasks.find { it.name == "UML" }?.grades?.size
        )
    }

    @Test
    fun studentGradesTest() {
        val math = courses["Math"] ?: fail()
        math.setGrade("Intro", "Penny", 1)
        math.setGrade("Uml lab", "Penny", 3)
        math.setGrade("Uml lab", "Penny", 4)
        val grades = math.studentGrades("Penny")
        assertEquals(1, grades["Intro"])
        assertEquals(4, grades["Uml lab"])
    }

    @Test
    fun tutorSetToplistTest(){
        val math = courses["Math"] ?: fail()
        math.setGrade("Intro", "Penny", 1)
        math.setGrade("Uml lab", "Penny", 3)
        math.setGrade("Uml lab", "Howard", 5)
        math.setToplist()
        assertEquals(0.22, math.getTask("Penny"))
        assertEquals(0.2, math.getRankByName("Howard"))
    }

    @Test
    fun studentOrTutorReadRankTest(){
        val math = courses["Math"] ?: fail()
        math.setGrade("Intro", "Penny", 1)
        math.setGrade("Uml lab", "Penny", 3)
        math.setGrade("Uml lab", "Howard", 5)
        math.setToplist()
        val topListFromRepo = toplistRepo.all()
        assertEquals(0.22, topListFromRepo.filter { it.studentName == "Penny" && it.courseName == "Math"})
        assertEquals(0.2, topListFromRepo.filter { it.studentName == "Howard" && it.courseName == "Math"})
    }

    @Test
    fun studentOrTutorReadGradesTest(){
        val math = courses["Math"] ?: fail()
        math.setGrade("Uml lab", "Penny", 3)
        math.setGrade("Uml lab", "Howard", 5)
        assertEquals(3, math.getTask("Uml lab")?.getGrade("Penny"))
        assertEquals(5, math.getTask("Uml lab")?.getGrade("Howard"))
    }


    @Test
    fun tutorSetGradeTest(){
        val math = courses["Math"] ?: fail()
        math.setGrade("Intro","Howard",4)
        math.setGrade("Uml lab","Howard",3)
        assertEquals(4, math.studentGrades("Howard")["Intro"])
        assertEquals(3, math.studentGrades("Howard")["Uml lab"])
    }

    @Test
    fun tutorSetTaskTest(){
        val math = courses["Math"] ?: fail()
        math.tasks.add(Task.getType(Type.LECTURE))
        math.tasks.add(Task.getType(Type.LABORATORY))
        math.tasks.add(Task.getType(Type.TEST))
        assertEquals("Lecture", math.getTask("Lecture")?.name)
        assertEquals("Laboratory", math.getTask("Laboratory")?.name)
        assertEquals("Test", math.getTask("Test")?.name)
        assertEquals("Personal project", math.getTask("Personal project")?.name)
    }

    @Test
    fun tutorAddStudentToCourse(){
        val math = courses["Math"] ?: fail()
        persons.add(Student("Bob","Newcomers"))
        persons.add(Student("Charlie","Newcomers"))
        math.addStudentByName("Bob")
        math.addStudentByName("Charlie")
        assertEquals("Bob", math.getStudent("Bob")?.name)
        assertEquals("Charlie", math.getStudent("Charlie")?.name)
    }

    @Test
    fun adminSetCourseTest(){
        listOf("Rocket science", "Basic rocket piloting", "Space navigation").forEach {
            setCourse(it)
        }
        assertEquals("Rocket science", courses["Rocket science"]?.name)
        assertEquals("Basic rocket piloting", courses["Basic rocket piloting"]?.name)
        assertEquals("Space navigation", courses["Space navigation"]?.name)
    }
}