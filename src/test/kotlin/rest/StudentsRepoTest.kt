package rest

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.server.testing.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import model.Student
import org.junit.Test
import repo.StudentMap
import kotlin.test.assertEquals

class StudentsRepoTest{
    private val testPath = "/students"

    @Test
    fun studentRepoMapTest(){
        testRest {
            studentsRestRepo(
                    StudentMap(),
                    testPath,
                    Student.serializer()
            )
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
            val studentsJson=
                    mapOf("Howard" to "Footprint on the Moon",
                            "Raj" to "Footprint on the Moon",
                            "Penny" to "Waitress").map {
                        Json.encodeToString(
                                Student.serializer(),
                                Student(it.key, it.value)
                        )
                    }
            studentsJson.map {
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

            //Get
            val students= handleRequest(HttpMethod.Get, testPath).run {
                assertStatus(HttpStatusCode.OK)
                parseResponse(
                        ListSerializer(Student.serializer())
                )
            }
            assertEquals(3, students?.size)
            handleRequest(HttpMethod.Get, "$testPath/groups/${students?.first()?.group}").run {
                assertStatus(HttpStatusCode.OK)
                val studentsFromGroup= parseResponse(ListSerializer(Student.serializer()))
                assertEquals(students?.first()?.group, studentsFromGroup?.first()?.group)
            }
            handleRequest(HttpMethod.Get, "$testPath/${students?.first()?.id}").run {
                assertStatus(HttpStatusCode.OK)
                val student= parseResponse(Student.serializer())
                assertEquals(students?.first()?.name, student?.name)
            }

            //Put
            val howard= students?.find { it.name == "Howard" }!!
            val newHoward= Student("Howard New", howard.group, howard.id)
            handleRequest(HttpMethod.Put, "$testPath/${newHoward.id}"){
                setBodyAndHeaders(Json.encodeToString(Student.serializer(), newHoward))
            }.run {
                assertStatus(HttpStatusCode.OK)
            }
            handleRequest(HttpMethod.Get, "$testPath/${newHoward.id}").run {
                assertStatus(HttpStatusCode.OK)
                val student= parseResponse(Student.serializer())
                assertEquals("Howard New", student?.name)
            }

            //Delete
            val penny= students.find { it.name == "Penny" }!!
            handleRequest(HttpMethod.Delete, "$testPath/${penny.id}").run {
                assertStatus(HttpStatusCode.OK)
            }

            //Final check
            val studentsNewName = handleRequest(HttpMethod.Get, testPath).run {
                assertStatus(HttpStatusCode.OK)
                parseResponse(ListSerializer(Student.serializer()))
            }?.map { it.name }!!

            assertEquals(2, studentsNewName.size)
            assertEquals(true, studentsNewName.contains("Howard New"))
        }
    }
}