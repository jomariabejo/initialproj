package model

import Priority
import Task

object TaskRepository {
    // This list stores all tasks
    private val tasks = mutableListOf(
        Task(1, "cleaning", "Clean the house", Priority.Low),
        Task(2, "gardening", "Mow the lawn", Priority.Medium),
        Task(3, "shopping", "Buy the groceries", Priority.High),
        Task(4, "painting", "Paint the fence", Priority.Medium)
    )

    // Get all tasks
    fun allTasks(): List<Task> = tasks

    // Get tasks by priority
    fun tasksByPriority(priority: Priority): List<Task> = tasks.filter { it.priority == priority }

    // Find a task by its name
    fun taskByName(name: String): Task? = tasks.find { it.name.equals(name, ignoreCase = true) }

    // Find a task by its ID
    fun taskById(id: Int): Task? = tasks.find { it.id == id }

    // Add a task, ensuring unique names
    fun addTask(task: Task) {
        // Check if the task name already exists in the list
        if (taskByName(task.name) != null) {
            throw IllegalStateException("Cannot duplicate task names!")
        }

        // Ensure the ID is unique by getting the max ID and incrementing it for new tasks
        val newId = (tasks.maxOfOrNull { it.id } ?: 0) + 1
        tasks.add(task.copy(id = newId))
    }

    // Delete a task by its ID
    fun deleteTask(id: Int): Boolean {
        val task = taskById(id)
        return if (task != null) {
            tasks.remove(task)
            true
        } else {
            false
        }
    }

    // Update an existing task by its ID
    fun updateTask(id: Int, updatedTask: Task): Boolean {
        val index = tasks.indexOfFirst { it.id == id }
        return if (index != -1) {
            tasks[index] = updatedTask.copy(id = id)  // Keep the same ID while updating
            true
        } else {
            false
        }
    }
}
