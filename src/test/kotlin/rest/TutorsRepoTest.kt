package rest

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.server.testing.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import model.Tutor
import org.junit.Test
import repo.StudentMap
import repo.TutorMap
import kotlin.test.assertEquals

class TutorsRepoTest{
    private val testPath = "/tutors"

    @Test
    fun tutorRepoMapTest(){
        testRest {
            tutorsRestRepo(
                    TutorMap(),
                    testPath,
                    Tutor.serializer()
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
        val tutorsJson=
                mapOf("Sheldon" to "Professor",
                        "Leonard" to "Professor"
                ).map {
                    Json.encodeToString(
                            Tutor.serializer(),
                            Tutor(it.key, it.value)
                    )
                }
        tutorsJson.map {
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
            val tutors= handleRequest(HttpMethod.Get, testPath).run {
                assertStatus(HttpStatusCode.OK)
                parseResponse(
                        ListSerializer(Tutor.serializer())
                )
            }
            assertEquals(2, tutors?.size)
            handleRequest(HttpMethod.Get, "$testPath/posts/${tutors?.first()?.post}").run {
                assertStatus(HttpStatusCode.OK)
                val tutorsFromPost= parseResponse(ListSerializer(Tutor.serializer()))
                assertEquals(tutors?.first()?.post, tutorsFromPost?.first()?.post)
            }
            handleRequest(HttpMethod.Get, "$testPath/${tutors?.first()?.id}").run {
                assertStatus(HttpStatusCode.OK)
                val tutor= parseResponse(Tutor.serializer())
                assertEquals(tutors?.first()?.name, tutor?.name)
            }

            //Put
            val leo= tutors?.find { it.name == "Leonard" }!!
            val newLeo= Tutor("Leonard New", leo.post, leo.id)
            handleRequest(HttpMethod.Put, "$testPath/${newLeo.id}"){
                setBodyAndHeaders(Json.encodeToString(Tutor.serializer(), newLeo))
            }.run {
                assertStatus(HttpStatusCode.OK)
            }
            handleRequest(HttpMethod.Get, "$testPath/${newLeo.id}").run {
                assertStatus(HttpStatusCode.OK)
                val tutor= parseResponse(Tutor.serializer())
                assertEquals("Leonard New", tutor?.name)
            }

            //Delete
            val sheldon= tutors.find { it.name == "Sheldon" }!!
            handleRequest(HttpMethod.Delete, "$testPath/${sheldon.id}").run {
                assertStatus(HttpStatusCode.OK)
            }

            //Final check
            val tutorsNewName = handleRequest(HttpMethod.Get, testPath).run {
                assertStatus(HttpStatusCode.OK)
                parseResponse(ListSerializer(Tutor.serializer()))
            }?.map { it.name }!!

            assert(tutorsNewName.size == 1)
            assert(tutorsNewName.contains("Leonard New"))
        }
    }
}