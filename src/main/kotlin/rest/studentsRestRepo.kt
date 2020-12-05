package rest

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import model.Student
import repo.Repo

fun Application.studentsRestRepo(
        repo: Repo<Student>,
        path:String= "/students",
        serializer: KSerializer<Student>
){
    routing {
        route(path){
            get {
                call.respond(repo.read())
            }
            post{
                call.respond(
                        parseStudentBody(serializer)?.let {elem ->
                        if (repo.create(elem))
                            HttpStatusCode.OK
                        else
                            HttpStatusCode.NotFound
                    }?: HttpStatusCode.BadRequest
                )
            }
        }
        route("$path/{studentId}"){
            get{
                parseStudentId()?.let {id ->
                    repo.read(id)?.let { elem ->
                        call.respond(elem)
                    }?: call.respond(HttpStatusCode.NotFound)
                }?: call.respond(HttpStatusCode.BadRequest)
            }
            put {
                call.respond(
                        parseStudentBody(serializer)?.let { elem ->
                        parseStudentId()?.let { id ->
                            if(repo.update(id, elem))
                                HttpStatusCode.OK
                            else
                                HttpStatusCode.NotFound
                        }
                    }?: HttpStatusCode.BadRequest
                )
            }
            delete {
                call.respond(
                        parseStudentId()?.let { i: Int ->
                            if (repo.delete(i))
                                HttpStatusCode.OK
                            else
                                HttpStatusCode.NotFound
                        }?: HttpStatusCode.BadRequest
                )
            }
        }
        route("$path/groups/{group}"){
            get {
                parseGroup()?.let { group ->
                    repo.read().let{ elem ->
                        call.respond(elem.filter { it.group == group })
                    }
                }?: call.respond(HttpStatusCode.BadRequest)
            }
        }
    }
}

fun PipelineContext<Unit, ApplicationCall>.parseGroup(group: String = "group") =
        call.parameters[group]

fun PipelineContext<Unit, ApplicationCall>.parseStudentId(id: String = "studentId") =
        call.parameters[id]?.toIntOrNull()

suspend fun  PipelineContext<Unit, ApplicationCall>.parseStudentBody(
    serializer: KSerializer<Student>
) =
    try {
        Json.decodeFromString(
            serializer,
            call.receive()
        )
    } catch (e: Throwable) {
        null
    }