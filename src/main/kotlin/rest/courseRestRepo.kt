package rest

import courses
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import model.Course
import model.Grade
import model.Task
import repo.Repo

fun Application.courseRestRepo(
        repo: Repo<Course> = courses,
        path:String= "/course",
        courseSerializer: KSerializer<Course> = Course.serializer(),
        taskSerializer: KSerializer<Task> = Task.serializer(),
        gradeSerializer: KSerializer<Grade> = Grade.serializer()
) {
    routing {
        route(path) {
            get {
                call.respond(repo.read())
            }
            post {
                call.respond(
                        parseCourseBody(courseSerializer)?.let { elem ->
                            if (repo.create(elem))
                                HttpStatusCode.Created
                            else
                                HttpStatusCode.NotFound
                        } ?: HttpStatusCode.BadRequest
                )
            }
        }
        route("$path/{courseId}") {
            get {
                call.respond(
                        parseCourseId()?.let { id ->
                            repo.read(id)?.let { elem ->
                                elem
                            } ?: HttpStatusCode.NotFound
                        } ?: HttpStatusCode.BadRequest
                )
            }
            put {
                call.respond(
                        parseCourseBody(courseSerializer)?.let { elem ->
                            parseCourseId()?.let { id ->
                                if (repo.update(id, elem))
                                    HttpStatusCode.Accepted
                                else
                                    HttpStatusCode.NotAcceptable
                            }
                        } ?: HttpStatusCode.BadRequest
                )
            }
            delete {
                call.respond(
                        parseCourseId()?.let { id: Int ->
                            if (repo.delete(id))
                                HttpStatusCode.OK
                            else
                                HttpStatusCode.NotFound
                        } ?: HttpStatusCode.BadRequest
                )
            }
        }
        route("$path/{courseId}/tutor") {
            get {
                call.respond(
                        parseCourseId()?.let { id: Int ->
                            repo.read(id)?.let { elem ->
                                elem.getTutorsAtCourse()
                            } ?:HttpStatusCode.NotFound
                        } ?: HttpStatusCode.BadRequest
                )
            }
        }
        route("$path/{courseId}/tutor/{tutorId}") {
            post {
                call.respond(
                        parseCourseId()?.let { courseid ->
                            repo.read(courseid)?.let { elem ->
                                parseTutorId()?.let { tutorid ->
                                    if (elem.addTutorById(tutorid))
                                        HttpStatusCode.Created
                                    else
                                        HttpStatusCode.NotFound
                                } ?: HttpStatusCode.BadRequest
                            } ?: HttpStatusCode.NotFound
                        } ?: HttpStatusCode.BadRequest
                )
            }
            delete {
                call.respond(
                        parseCourseId()?.let { courseid ->
                            repo.read(courseid)?.let { elem ->
                                parseTutorId()?.let { tutorid ->
                                    if (elem.removeTutorFromCourse(tutorid))
                                        HttpStatusCode.OK
                                    else
                                        HttpStatusCode.NotFound
                                } ?: HttpStatusCode.BadRequest
                            } ?: HttpStatusCode.NotFound
                        } ?: HttpStatusCode.BadRequest
                )
            }
        }
        route("$path/{courseId}/student") {
            get {
                call.respond(
                        parseCourseId()?.let { id ->
                            repo.read(id)?.let { elem ->
                                elem.getStudentsAtCourse()
                            } ?: HttpStatusCode.NotFound
                        } ?: HttpStatusCode.BadRequest
                )
            }
        }
        route("$path/{courseId}/student/{studentId}") {
            post {
                call.respond(
                        parseCourseId()?.let { courseid ->
                            repo.read(courseid)?.let { elem ->
                                parseStudentId()?.let { studentid ->
                                    if (elem.addStudentById(studentid))
                                        HttpStatusCode.Created
                                    else
                                        HttpStatusCode.NotFound
                                } ?: HttpStatusCode.BadRequest
                            } ?: HttpStatusCode.NotFound
                        } ?: HttpStatusCode.NotFound
                )
            }
            delete {
                call.respond(
                        parseCourseId()?.let { courseid ->
                            repo.read(courseid)?.let { elem ->
                                parseStudentId()?.let { studentid ->
                                    if (elem.removeStudentFromCourse(studentid))
                                        HttpStatusCode.OK
                                    else
                                        HttpStatusCode.NotFound
                                } ?: HttpStatusCode.BadRequest
                            } ?: HttpStatusCode.NotFound
                        } ?: HttpStatusCode.BadRequest
                )
            }
        }
        route("$path/{courseId}/toplist") {
            get {
                call.respond(
                        parseCourseId()?.let { id ->
                            repo.read(id)?.let { elem ->
                                elem.getCourseToplist()
                            } ?: HttpStatusCode.NotFound
                        } ?: HttpStatusCode.BadRequest
                )
            }
        }
        route("$path/{courseId}/task") {
            get {
                call.respond(
                        parseCourseId()?.let { id ->
                            repo.read(id)?.let { elem ->
                                elem.getTasks()
                            } ?: HttpStatusCode.NotFound
                        } ?: HttpStatusCode.BadRequest
                )
            }
            post {
                call.respond(
                        parseCourseId()?.let { id ->
                            repo.read(id)?.let { elem ->
                                parseTaskBody(taskSerializer)?.let {
                                    if (elem.addTask(it))
                                        HttpStatusCode.Created
                                    else
                                        HttpStatusCode.NotFound
                                } ?: HttpStatusCode.BadRequest
                            } ?: HttpStatusCode.NotFound
                        } ?: HttpStatusCode.BadRequest
                )
            }
        }
        route("$path/{courseId}/task/{taskId}") {
            get {
                call.respond(
                        parseCourseId()?.let { courseid ->
                            repo.read(courseid)?.let { elem ->
                                parseTaskId()?.let { taskid ->
                                    elem.getTask(taskid) ?: "No such task"
                                } ?: HttpStatusCode.NotFound
                            } ?: HttpStatusCode.NotFound
                        } ?: HttpStatusCode.BadRequest
                )
            }
            delete {
                call.respond(
                        parseCourseId()?.let { courseid ->
                            repo.read(courseid)?.let { elem ->
                                parseTaskId()?.let { taskid ->
                                    if (elem.removeTask(taskid))
                                        HttpStatusCode.OK
                                    else
                                        HttpStatusCode.NotFound
                                } ?: HttpStatusCode.BadRequest
                            } ?: HttpStatusCode.NotFound
                        } ?: HttpStatusCode.BadRequest
                )
            }
        }
        route("$path/{courseId}/task/{taskId}/grade"){
            get {
                call.respond(
                        parseCourseId()?.let { courseid ->
                            repo.read(courseid)?.let { elem ->
                                parseTaskId()?.let { taskid ->
                                    elem.getTask(taskid)?.getGrades()!!
                                } ?: HttpStatusCode.NotFound
                            } ?: HttpStatusCode.BadRequest
                        } ?: HttpStatusCode.BadRequest
                )
            }
            post {
                call.respond(
                        parseCourseId()?.let { courseid ->
                            repo.read(courseid)?.let { elem ->
                                parseTaskId()?.let { taskid ->
                                    parseGradeBody(gradeSerializer)?.let {
                                        if (elem.getTask(taskid)?.addGrade(it)!!)
                                            HttpStatusCode.Created
                                        else
                                            HttpStatusCode.NotFound
                                    } ?: HttpStatusCode.BadRequest
                                } ?: HttpStatusCode.BadRequest
                            } ?: HttpStatusCode.NotFound
                        } ?: HttpStatusCode.BadRequest
                )
            }
        }
    }
}

fun PipelineContext<Unit, ApplicationCall>.parseCourseId(id: String = "courseId"): Int? =
        call.parameters[id]?.toIntOrNull()

fun PipelineContext<Unit, ApplicationCall>.parseTaskId(id: String = "taskId"): Int? =
        call.parameters[id]?.toIntOrNull()

suspend fun  PipelineContext<Unit, ApplicationCall>.parseCourseBody(
        serializer: KSerializer<Course>
) =
        try {
            Json.decodeFromString(
                    serializer,
                    call.receive()
            )
        } catch (e: Throwable) {
            null
        }

suspend fun PipelineContext<Unit, ApplicationCall>.parseTaskBody(
        serializer: KSerializer<Task>
) =
        try {
            Json.decodeFromString(
                    serializer,
                    call.receive()
            )
        } catch (e: Throwable){
            null
        }

suspend fun PipelineContext<Unit, ApplicationCall>.parseGradeBody(
        serializer: KSerializer<Grade>
) =
        try {
            Json.decodeFromString(
                    serializer,
                    call.receive()
            )
        } catch (e: Throwable){
            null
        }