package rest

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.json
import io.ktor.server.testing.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import repo.RepoMap
import repo.TestItem
import kotlin.test.Test
import kotlin.test.assertEquals


class RestRepoTest {

    private val testPath = "/items"

    @Test
    fun restRepoMapTest() {
        testRest  {
            restRepo(
                    RepoMap(),
                    testPath,
                    TestItem.serializer()
            )
        }
    }


    private fun testRest(
            restModule: Application.() -> Unit
    ) {
        withTestApplication({
            install(ContentNegotiation) {
                json()
            }
            restModule()
        }) {

            // Post
            val itemsJson =
                    arrayOf("one", "two", "three")
                            .map {
                                Json.encodeToString(
                                        TestItem.serializer(),
                                        TestItem(it)
                                )
                            }
            itemsJson.map {
                handleRequest(HttpMethod.Post, testPath) {
                    setBodyAndHeaders(it)
                }.apply {
                    assertStatus(HttpStatusCode.OK)
                }
            }
            handleRequest(HttpMethod.Post, testPath) {
                setBodyAndHeaders("Wrong JSON")
            }.apply {
                assertStatus(HttpStatusCode.BadRequest)
            }

            // Get
            val items = handleRequest(HttpMethod.Get, testPath).run {
                assertStatus(HttpStatusCode.OK)
                parseResponse(
                        ListSerializer(TestItem.serializer())
                )
            }
            assertEquals(3, items?.size)
            handleRequest(HttpMethod.Get, "$testPath/${items?.first()?.id}").run {
                assertStatus(HttpStatusCode.OK)
                val item = parseResponse(TestItem.serializer())
                assertEquals(items?.first()?.name, item?.name)
            }

            // Put

            val three = items?.find { it.name == "three" }!!
            val threeNew = TestItem("three new", three.id)
            handleRequest(HttpMethod.Put, "$testPath/${threeNew.id}") {
                setBodyAndHeaders(Json.encodeToString(TestItem.serializer(), threeNew))
            }.run {
                assertStatus(HttpStatusCode.OK)
            }
            handleRequest(HttpMethod.Get, "$testPath/${threeNew.id}").run {
                assertStatus(HttpStatusCode.OK)
                val item = parseResponse(TestItem.serializer())
                assertEquals("three new", item?.name)
            }

            // Delete
            val two = items.find { it.name == "two" }!!
            handleRequest(HttpMethod.Delete, "$testPath/${two.id}").run {
                assertStatus(HttpStatusCode.OK)
            }

            // Final check
            val itemsNewName = handleRequest(HttpMethod.Get, testPath).run {
                assertStatus(HttpStatusCode.OK)
                parseResponse(
                        ListSerializer(TestItem.serializer())
                )
            }?.map { it.name }!!

            assert(itemsNewName.size == 2)
            assert(itemsNewName.contains("one"))
            assert(itemsNewName.contains("three new"))

        }
    }
}

fun TestApplicationCall.assertStatus(status: HttpStatusCode) =
        assertEquals(status, response.status())

fun TestApplicationRequest.setBodyAndHeaders(body: String) {
    setBody(body)
    addHeader("Content-Type", "application/json")
    addHeader("Accept", "application/json")
}

fun <T> TestApplicationCall.parseResponse(
        serializer: KSerializer<T>
) =
        try {
            Json.decodeFromString(
                    serializer,
                    response.content ?: ""
            )
        } catch (e: Throwable) {
            null
        }