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
                                HttpStatusCode.OK
                            else
                                HttpStatusCode.NotFound
                        } ?: HttpStatusCode.BadRequest
                )
            }
        }
        route("$path/{courseId}") {
            get {
                parseCourseId()?.let { id ->
                    repo.read(id)?.let { elem ->
                        call.respond(elem)
                    } ?: call.respond(HttpStatusCode.NotFound)
                } ?: call.respond(HttpStatusCode.BadRequest)
            }
            put {
                call.respond(
                        parseCourseBody(courseSerializer)?.let { elem ->
                            parseCourseId()?.let { id ->
                                if (repo.update(id, elem))
                                    HttpStatusCode.OK
                                else
                                    HttpStatusCode.NotFound
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
                parseCourseId()?.let { id: Int ->
                    repo.read(id)?.let { elem ->
                        call.respond(elem.getTutorsAtCourse())
                    } ?: call.respond(HttpStatusCode.NotFound)
                } ?: call.respond(HttpStatusCode.BadRequest)
            }
        }
        route("$path/{courseId}/tutor/{tutorId}") {
            post {
                parseCourseId()?.let { courseid ->
                    repo.read(courseid)?.let { elem ->
                        parseTutorId()?.let { tutorid ->
                            if (elem.addTutorById(tutorid))
                                HttpStatusCode.OK
                            else
                                HttpStatusCode.NotFound
                        }?: HttpStatusCode.BadRequest
                    }?: HttpStatusCode.NotFound
                }?: HttpStatusCode.BadRequest
            }
            delete {
                parseCourseId()?.let { courseid ->
                    repo.read(courseid)?.let { elem ->
                        parseTutorId()?.let { tutorid ->
                            if (elem.removeTutorFromCourse(tutorid))
                                HttpStatusCode.OK
                            else
                                HttpStatusCode.NotFound
                        } ?: HttpStatusCode.BadRequest
                    }?: HttpStatusCode.NotFound
                } ?: HttpStatusCode.BadRequest
            }
        }
        route("$path/{courseId}/student") {
            get {
                parseCourseId()?.let { id ->
                    repo.read(id)?.let { elem ->
                        call.respond(elem.getStudentsAtCourse())
                    } ?: call.respond(HttpStatusCode.NotFound)
                } ?: call.respond(HttpStatusCode.BadRequest)
            }
        }
        route("$path/{courseId}/student/{studentId}") {
            post {
                parseCourseId()?.let { courseid ->
                    repo.read(courseid)?.let { elem ->
                        parseStudentId()?.let { studentid ->
                            if (elem.addStudentById(studentid))
                                HttpStatusCode.OK
                            else
                                HttpStatusCode.NotFound
                        }?: HttpStatusCode.BadRequest
                    }?: HttpStatusCode.NotFound
                }?: HttpStatusCode.NotFound
            }
            delete {
                parseCourseId()?.let { courseid ->
                    repo.read(courseid)?.let { elem ->
                        parseStudentId()?.let { studentid ->
                            if (elem.removeStudentFromCourse(studentid))
                                HttpStatusCode.OK
                            else
                                HttpStatusCode.NotFound
                        } ?: HttpStatusCode.BadRequest
                    }?: HttpStatusCode.NotFound
                } ?: HttpStatusCode.BadRequest
            }
        }
        route("$path/{courseId}/toplist") {
            get {
                parseCourseId()?.let { id ->
                    repo.read(id)?.let { elem ->
                        call.respond(elem.getCourseToplist())
                    } ?: HttpStatusCode.NotFound
                } ?: HttpStatusCode.BadRequest
            }
        }
        route("$path/{courseId}/task") {
            get {
                parseCourseId()?.let { id ->
                    repo.read(id)?.let { elem ->
                        call.respond(elem.getTasks())
                    } ?: HttpStatusCode.NotFound
                } ?: HttpStatusCode.BadRequest
            }
            post {
                parseCourseId()?.let { id ->
                    repo.read(id)?.let { elem ->
                        parseTaskBody(taskSerializer)?.let {
                            if (elem.addTask(it))
                                HttpStatusCode.OK
                            else
                                HttpStatusCode.NotFound
                        } ?: HttpStatusCode.BadRequest
                    }?: HttpStatusCode.NotFound
                } ?: HttpStatusCode.BadRequest
            }
        }
        route("$path/{courseId}/task/{taskId}") {
            get {
                parseCourseId()?.let { courseid ->
                    repo.read(courseid)?.let { elem ->
                        parseTaskId()?.let { taskid ->
                            call.respond(elem.getTask(taskid) ?: "No such task")
                        } ?: HttpStatusCode.NotFound
                    } ?: HttpStatusCode.NotFound
                } ?: HttpStatusCode.BadRequest
            }
            delete {
                parseCourseId()?.let { courseid ->
                    repo.read(courseid)?.let { elem ->
                        parseTaskId()?.let { taskid ->
                            if (elem.removeTask(taskid))
                                HttpStatusCode.OK
                            else
                                HttpStatusCode.NotFound
                        } ?: HttpStatusCode.BadRequest
                    }?: HttpStatusCode.NotFound
                } ?: HttpStatusCode.BadRequest
            }
        }
        route("$path/{courseId}/task/{taskId}/grade"){
            get {
                parseCourseId()?.let { courseid ->
                    repo.read(courseid)?.let { elem ->
                        parseTaskId()?.let { taskid ->
                            call.respond(elem.getTask(taskid)?.getGrades()!!)
                        } ?: HttpStatusCode.NotFound
                    } ?: HttpStatusCode.BadRequest
                }?:HttpStatusCode.BadRequest
            }
            post {
                parseCourseId()?.let { courseid ->
                    repo.read(courseid)?.let { elem ->
                        parseTaskId()?.let { taskid ->
                            parseGradeBody(gradeSerializer)?.let {
                                if (elem.getTask(taskid)?.addGrade(it)!!)
                                    HttpStatusCode.OK
                                else
                                    HttpStatusCode.NotFound
                            }?: HttpStatusCode.BadRequest
                        }?: HttpStatusCode.BadRequest
                    }?: HttpStatusCode.NotFound
                }?: HttpStatusCode.BadRequest
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