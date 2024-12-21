package com.jomariabejo.ktor

import Task
import io.ktor.http.*
import io.ktor.http.ContentDisposition.Companion.File
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.io.files.FileNotFoundException
import model.TaskRepository
import tasksAsTable
import java.io.File

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

                // Extract parameters from the form
                val name = formContent["name"] ?: ""
                val description = formContent["description"] ?: ""
                val priority = formContent["priority"] ?: ""

                // Validate parameters
                if (name.isEmpty() || description.isEmpty() || priority.isEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, "Missing or invalid parameters")
                    return@post
                }

                try {
                    // Parse the priority
                    val taskPriority = Priority.valueOf(priority)

                    // Create the new Task (assuming id is auto-generated or handled differently)
                    val newTask = Task(
                        id = TaskRepository.allTasks().size + 1,  // Use a simple incremented id (or auto-generate based on your logic)
                        name = name,
                        description = description,
                        priority = taskPriority
                    )

                    // Add the task to the repository
                    TaskRepository.addTask(newTask)

                    // Respond with success
                    call.respondRedirect("/tasks")
                } catch (e: IllegalArgumentException) {
                    // Handle invalid priority
                    call.respond(HttpStatusCode.BadRequest, "Invalid priority value")
                } catch (e: Exception) {
                    // Handle other errors (e.g., database issues, etc.)
                    call.respond(HttpStatusCode.InternalServerError, "An error occurred while adding the task")
                }
            }

            // Get task edit form
            get("/edit/{id}") {
                val taskId = call.parameters["id"]?.toIntOrNull()
                if (taskId == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid task ID")
                    return@get
                }

                val task = TaskRepository.taskById(taskId)
                if (task == null) {
                    call.respond(HttpStatusCode.NotFound, "Task not found")
                    return@get
                }

                // Read the HTML template from the resources
                val htmlTemplate = javaClass.classLoader.getResource("task-ui/edit-task.html")?.readText()
                    ?: throw FileNotFoundException("HTML template not found")


                // Replace placeholders in the template with actual task data
                val htmlForm = htmlTemplate.replace("{taskId}", task.id.toString())
                    .replace("{taskName}", task.name)
                    .replace("{taskDescription}", task.description)
                    .replace("{taskPriorityLow}", if (task.priority == Priority.Low) "selected" else "")
                    .replace("{taskPriorityMedium}", if (task.priority == Priority.Medium) "selected" else "")
                    .replace("{taskPriorityHigh}", if (task.priority == Priority.High) "selected" else "")

                // Respond with the populated HTML
                call.respondText(htmlForm, contentType = ContentType.parse("text/html"))
            }


            // Post updated task data
            post("/edit/{id}") {
                val taskId = call.parameters["id"]?.toIntOrNull()
                if (taskId == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid task ID")
                    return@post
                }

                val formContent = call.receiveParameters()

                val name = formContent["name"] ?: ""
                val description = formContent["description"] ?: ""
                val priority = formContent["priority"] ?: ""

                if (name.isEmpty() || description.isEmpty() || priority.isEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, "Missing or invalid parameters")
                    return@post
                }

                try {
                    val taskPriority = Priority.valueOf(priority)

                    val updatedTask = Task(
                        id = taskId,
                        name = name,
                        description = description,
                        priority = taskPriority
                    )

                    val updated = TaskRepository.updateTask(taskId, updatedTask)

                    if (updated) {
                        call.respondRedirect("/tasks")
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Task not found")
                    }

                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid priority value")
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "An error occurred while updating the task")
                }
            }

            // Handle delete request
            get("/remove/{id}") {
                val taskId = call.parameters["id"]?.toIntOrNull()
                if (taskId == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid task ID")
                    return@get
                }

                val deleted = TaskRepository.deleteTask(taskId)

                if (deleted) {
                    call.respondRedirect("/tasks")
                } else {
                    call.respond(HttpStatusCode.NotFound, "Task not found")
                }
            }
        }
    }
}
