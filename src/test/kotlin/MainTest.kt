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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MainTest {

    @BeforeAll
    fun init() {
        mapOf(
            "Lecture" to "Lec",
            "Laboratory" to "Lab",
            "Test" to "Tst"
        ).forEach {
            taskTypes.add(Type(it.key, it.value))
        }
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
            tasks.add(Task("Intro", taskTypes["Lecture"]!!))
            tasks.add(Task("UML", taskTypes["Lecture"]!!))
            tasks.add(Task("Uml lab", taskTypes["Laboratory"]!!, maxValue = 5))
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
    fun setTopListTest(){
        val math = courses["Math"] ?: fail()
        math.setGrade("Intro", "Penny", 1)
        math.setGrade("Uml lab", "Penny", 3)
        math.setGrade("Uml lab", "Howard", 5)
        val sheldon = persons["Sheldon"] as Tutor
        sheldon.setTopList(math)
        math.topList.forEach {
            topListRepo.add(it)
        }
//        assertEquals(0.26, math.getRankByName("Penny"))
//        assertEquals(0.33, math.getRankByName("Howard"))
        val topListFromRepo = topListRepo.all()
        assertEquals(0.26, topListFromRepo.filter { it.name == "Penny's rank" })
        assertEquals(0.33, topListFromRepo.filter { it.name == "Howard's rank" })
    }

    @Test
    fun studentReadGradesTest(){
        val math = courses["Math"] ?: fail()
        math.setGrade("Uml lab", "Penny", 3)
        math.setGrade("Uml lab", "Howard", 5)
        val penny = persons["Penny"] as Student
        val howard = persons["Howard"] as Student
        assertEquals(3, penny.readGradesAtCourse("Math")?.get("Uml lab"))
        assertEquals(5, howard.readGradesAtCourse("Math")?.get("Uml lab"))
    }

    @Test
    fun studentReadRankAtCourseTest(){
        val math = courses["Math"] ?: fail()
        math.setGrade("Intro", "Penny", 1)
        math.setGrade("Uml lab", "Penny", 3)
        math.setGrade("Uml lab", "Howard", 5)
        val sheldon = persons["Sheldon"] as Tutor
        sheldon.setTopList(math)
        val penny = persons["Penny"] as Student
        val howard = persons["Howard"] as Student
        assertEquals(0.26, penny.readRankAtCourse("Math")?.rank.second)
        assertEquals(0.33, howard.readRankAtCourse("Math")?.rank.second)
    }

    @Test
    fun tutorReadGradesTest(){
        val math = courses["Math"] ?: fail()
        math.setGrade("Uml lab", "Penny", 3)
        math.setGrade("Uml lab", "Howard", 5)
        val sheldon = persons["Sheldon"] as Tutor
        assertEquals(3, sheldon.readGrades("Math", "Penny"))
        assertEquals(5, sheldon.readGrades("Math", "Howard"))
    }

    @Test
    fun tutorSetGradeTest(){
        val math = courses["Math"] ?: fail()
        val sheldon = persons["Sheldon"] as Tutor
        sheldon.setGrade("Math","Intro","Howard",4)
        sheldon.setGrade("Math","Uml lab","Howard",3)
        assertEquals(4, math.studentGrades("Howard")["Intro"])
        assertEquals(3, math.studentGrades("Howard")["Uml lab"])
    }

    @Test
    fun tutorSetTaskTest(){
        val math = courses["Math"] ?: fail()
        val sheldon = persons["Sheldon"] as Tutor
        sheldon.setTask("Math","ORM design", taskTypes["Lecture"]!!)
        sheldon.setTask("Math","VScode", taskTypes["Lecture"]!!)
        assertEquals("ORM design", courses["Math"]?.getTask("ORM design")?.name)
        assertEquals("VScode", courses["Math"]?.getTask("VScode")?.name)
    }

    @Test
    fun tutorSetCourseTest(){
        val sheldon = persons["Sheldon"] as Tutor
        sheldon.setCourse("Rocket science")
        sheldon.setCourse("Basic rocket piloting")
        assertEquals("Rocket science", courses["Rocket science"]?.name)
        assertEquals("Basic rocket piloting", courses["Basic rocket piloting"]?.name)
    }

    @Test
    fun tutorAddStudentToCourse(){
        val math = courses["Math"] ?: fail()
        val sheldon = persons["Sheldon"] as Tutor
        sheldon.addStudentToCourse("Math","Bob")
        sheldon.addStudentToCourse("Math","Charlie")
        assertEquals("Bob", math.getStudent("Bob")?.name)
        assertEquals("Charlie", math.getStudent("Charlie")?.name)
    }


}