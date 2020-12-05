package rest

import courseStudentTable
import courseTutorTable
import courses
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.server.testing.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import model.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Test
import students
import tutors
import kotlin.test.assertEquals
import kotlin.test.fail

class CourseRepoTest{
    private val testPath = "/course"

    @Test
    fun courseRepoMapTest(){
        Database.connect(
                "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
                driver = "org.h2.Driver"
        )
        transaction {
            SchemaUtils.create(tutorTable, studentTable, courseStudentTable, courseTable, courseTutorTable, tasksTable, gradesTable, toplistTable, typesTable)
        }

        mapOf("Sheldon" to "Professor",
                "Leonard" to "Professor",
                "Ada" to "Invited lecturer"
        ).forEach { tutors.create(Tutor(it.key, it.value)) }

        mapOf("Howard" to "Footprint on the Moon",
                "Raj" to "Footprint on the Moon",
                "Penny" to "Waitress").forEach { students.create(Student(it.key, it.value)) }
        mapOf("Lecture" to "Lec",
                "Practice" to "Pract",
                "Test" to "Test"
        ).forEach { type ->
            transaction {
                typesTable.insert { fill(it, Type(type.key, type.value)) }
            }
        }
        testRest {
            courseRestRepo(
                    courses,
                    testPath,
                    Course.serializer(),
                    Task.serializer(),
                    Grade.serializer()
            )
        }
        transaction {
            SchemaUtils.drop(courseStudentTable, courseTutorTable, tutorTable, gradesTable, tasksTable, toplistTable, courseTable, studentTable, typesTable)
        }
    }

    private fun testRest(
            restModule: Application.() -> Unit
    ){
        withTestApplication({
            install(ContentNegotiation){
                json()
            }
            restModule()
        }) {

            //Post
            val courseJson=
                    listOf("Math",
                            "Phys",
                            "Hist"
                    ).map {
                        Json.encodeToString(
                                Course.serializer(),
                                Course(it)
                        )
                    }
            courseJson.map {
                handleRequest(HttpMethod.Post, testPath){
                    setBodyAndHeaders(it)}.apply {
                    assertStatus(HttpStatusCode.OK)
                }
            }
            handleRequest(HttpMethod.Post, testPath){
                setBodyAndHeaders("Wrong JSON")
            }.apply {
                assertStatus(HttpStatusCode.BadRequest)
            }

            val shel= tutors.read().firstOrNull { it.name == "Sheldon" }?: fail()
            val leon= tutors.read().firstOrNull {it.name == "Leonard"}?: fail()
            val tutorsJson=
                    listOf(
                            Json.encodeToString(Tutor.serializer(),shel),
                            Json.encodeToString(Tutor.serializer(),leon)
                    )

            handleRequest(HttpMethod.Post, "$testPath/1/tutor/1"){
                setBodyAndHeaders(tutorsJson.first())
            }.apply {
                println(handleRequest(HttpMethod.Get, "$testPath/1").run { parseResponse(Course.serializer())?.getTutorsAtCourse() })
                println(response.status())
                assertStatus(HttpStatusCode.OK)
            }

            handleRequest(HttpMethod.Post, "$testPath/1/tutor/2"){
                setBodyAndHeaders(tutorsJson.last())
            }.apply {
                assertStatus(HttpStatusCode.OK)
            }

            handleRequest(HttpMethod.Post, "$testPath/1/tutor"){
                setBodyAndHeaders("Wrong JSON")
            }.apply {
                assertStatus(HttpStatusCode.BadRequest)
            }

            val studentsJson=
                    mapOf("Howard" to "Footprint on the Moon",
                            "Raj" to "Footprint on the Moon",
                            "Penny" to "Waitress"
                    ).map {
                        Json.encodeToString(
                                Student.serializer(),
                                Student(it.key, it.value)
                        )
                    }
            studentsJson.map {
                handleRequest(HttpMethod.Post, "$testPath/1/student"){
                    setBodyAndHeaders(it)}.apply {
                    assertStatus(HttpStatusCode.OK)
                }
            }
            handleRequest(HttpMethod.Post, "$testPath/1/student"){
                setBodyAndHeaders("Wrong JSON")
            }.apply {
                assertStatus(HttpStatusCode.BadRequest)
            }

            val firstTaskJson= Json.encodeToString(
                    Task.serializer(),
                    Task("first task", 1, 1, maxValue = 5)
            )
            val secondTaskJson= Json.encodeToString(
                    Task.serializer(),
                    Task("second task", 1, 1, maxValue = 5)
            )
            listOf(firstTaskJson, secondTaskJson).map {
                handleRequest(HttpMethod.Post,"$testPath/1/task" ){
                    setBodyAndHeaders(it)
                }.apply {
                    assertStatus(HttpStatusCode.OK)
                }
            }
            handleRequest(HttpMethod.Post,"$testPath/1/task"){
                setBodyAndHeaders("Wrong JSON")
            }.apply {
                assertStatus(HttpStatusCode.BadRequest)
            }

            val firstGradeJson= Json.encodeToString(
                    Grade.serializer(),
                    Grade(5, student_id = 1, task_id = 1)
            )
            val secondGradeJson= Json.encodeToString(
                    Grade.serializer(),
                    Grade(3, student_id = 2, task_id = 1)
            )
            handleRequest(HttpMethod.Post, "$testPath/1/task/1/grade"){
                setBodyAndHeaders(firstGradeJson)
            }.apply {
                assertStatus(HttpStatusCode.OK)
            }
            handleRequest(HttpMethod.Post, "$testPath/1/task/1/grade"){
                setBodyAndHeaders(secondGradeJson)
            }.apply {
                assertStatus(HttpStatusCode.OK)
            }
            handleRequest(HttpMethod.Post, "$testPath/1/task/1/grade"){
                setBodyAndHeaders("Wrong JSON")
            }.apply {
                assertStatus(HttpStatusCode.BadRequest)
            }

            //Get
            val courses= handleRequest(HttpMethod.Get, testPath).run {
                assertStatus(HttpStatusCode.OK)
                parseResponse(
                        ListSerializer(Course.serializer())
                )
            }
            assertEquals(3, courses?.size)
            handleRequest(HttpMethod.Get, "$testPath/${courses?.first()?.id}").run {
                assertStatus(HttpStatusCode.OK)
                val course= parseResponse(Course.serializer())
                assertEquals(courses?.first()?.name, course?.name)
            }
            val tutorsAtCourse= handleRequest(HttpMethod.Get, "$testPath/${courses?.first()?.id}/tutor").run {
                assertStatus(HttpStatusCode.OK)
                parseResponse(ListSerializer(Course.serializer()))
            }
            assertEquals(2, tutorsAtCourse?.size)


            //Put
            val math= courses?.find { it.name == "Math" }!!
            val newMath= Course("MathNew", math.id)
            handleRequest(HttpMethod.Put, "$testPath/${newMath.id}"){
                setBodyAndHeaders(Json.encodeToString(Course.serializer(), newMath))
            }.run {
                assertStatus(HttpStatusCode.OK)
            }
            handleRequest(HttpMethod.Get, "$testPath/${newMath.id}").run {
                assertStatus(HttpStatusCode.OK)
                val course= parseResponse(Course.serializer())
                assertEquals("MathNew", course?.name)
            }

            //Delete
            val history= courses.find { it.name == "Hist" }!!
            handleRequest(HttpMethod.Delete, "$testPath/${history.id}").run {
                assertStatus(HttpStatusCode.OK)
            }

            //Final check
            val coursesNewName = handleRequest(HttpMethod.Get, testPath).run {
                assertStatus(HttpStatusCode.OK)
                parseResponse(ListSerializer(Course.serializer()))
            }?.map { it.name }!!

            assert(coursesNewName.size == 2)
            assert(coursesNewName.contains("MathNew"))
        }
    }
}