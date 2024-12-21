package com.jomariabejo.ktor

import Task
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import model.TaskRepository
import tasksAsTable

fun Application.configureRouting() {
    routing {
        staticResources("/content", "mycontent")
        staticResources("/task-ui", "task-ui")

        get("/") {
            call.respondText("Hello World!")
        }
        route("/tasks") {
            get("/test1") {
                val text = "<h1>Hello From Ktor</h1>"
                val type = ContentType.parse("text/html")
                call.respondText(text, type)
            }

            get() {
                val tasks = TaskRepository.allTasks()

                val htmlContent = """
        <html>
            <head>
                <title>Task List</title>
            </head>
            <body>
                <h1>Task List</h1>
                ${tasks.tasksAsTable()}
                <br>
                <a href="/task-ui/task-form.html">
                    <button>Add new task</button>
                </a>
            </body>
        </html>
    """

                // Respond with the HTML content
                call.respondText(
                    contentType = ContentType.parse("text/html"),
                    text = htmlContent
                )
            }

            get("/byPriority/{priority}") {
                val priorityAsText = call.parameters["priority"]
                if (priorityAsText == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }

                try {
                    val priority = Priority.valueOf(priorityAsText)
                    val tasks = TaskRepository.tasksByPriority(priority)

                    if (tasks.isEmpty()) {
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }

                    call.respondText(
                        contentType = ContentType.parse("text/html"),
                        text = tasks.tasksAsTable()
                    )
                } catch (ex: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

            post {
                val formContent = call.receiveParameters()

                val params = Triple(
                    formContent["name"] ?: "",
                    formContent["description"] ?: "",
                    formContent["priority"] ?: ""
                )

                if (params.toList().any { it.isEmpty() }) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@post
                }

                try {
                    val priority = Priority.valueOf(params.third)
                    TaskRepository.addTask(
                        Task(
                            params.first,
                            params.second,
                            priority
                        )
                    )

                    call.respondRedirect("/tasks")
                } catch (ex: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest)
                } catch (ex: IllegalStateException) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

        }
    }
}
