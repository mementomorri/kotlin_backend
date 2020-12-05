package rest

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import model.Tutor
import repo.Repo
import repo.RepoPerson

fun Application.tutorsRestRepo(
        repo: Repo<Tutor>,
        path: String= "/tutors",
        serializer: KSerializer<Tutor>
) {
    routing {
        route(path){
            get {
                call.respond(repo.read())
            }
            post{
                call.respond(
                        parseTutorBody(serializer)?.let { elem ->
                            if (repo.create(elem))
                                HttpStatusCode.OK
                            else
                                HttpStatusCode.NotFound
                        }?: HttpStatusCode.BadRequest
                )
            }
        }
        route("$path/posts/{post}"){
            get {
                parsePost()?.let { post ->
                    repo.read().let{ elem ->
                        call.respond(elem.filter { it.post == post })
                    }
                }?: call.respond(HttpStatusCode.BadRequest)
            }
        }
        route("$path/{tutorId}"){
            get{
                parseTutorId()?.let {id ->
                    repo.read(id)?.let { elem ->
                        call.respond(elem)
                    }?: call.respond(HttpStatusCode.NotFound)
                }?: call.respond(HttpStatusCode.BadRequest)
            }
            put {
                call.respond(
                        parseTutorBody(serializer)?.let { elem ->
                            parseTutorId()?.let { id ->
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
                        parseTutorId()?.let { i: Int ->
                            if (repo.delete(i))
                                HttpStatusCode.OK
                            else
                                HttpStatusCode.NotFound
                        }?: HttpStatusCode.BadRequest
                )
            }
        }
    }
}

fun PipelineContext<Unit, ApplicationCall>.parsePost(post: String = "post") =
        call.parameters[post]

fun PipelineContext<Unit, ApplicationCall>.parseTutorId(id: String = "tutorId") =
        call.parameters[id]?.toIntOrNull()

suspend fun  PipelineContext<Unit, ApplicationCall>.parseTutorBody(
        serializer: KSerializer<Tutor>
) =
        try {
            Json.decodeFromString(
                    serializer,
                    call.receive()
            )
        } catch (e: Throwable) {
            null
        }