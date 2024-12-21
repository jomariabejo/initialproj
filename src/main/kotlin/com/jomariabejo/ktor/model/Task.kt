enum class Priority {
    Low, Medium, High, Vital
}

data class Task(
    val id: Int,          // Added ID field
    val name: String,
    val description: String,
    val priority: Priority
)

fun Task.taskAsRow() = """
    <tr>
        <td>$id</td>
        <td>$name</td>
        <td>$description</td>
        <td>$priority</td>
        <td>
            <a href="/tasks/edit/$id">
                <button class="edit-btn">
                    <i class="fa fa-pencil"></i> Edit
                </button>
            </a>
            <a href="/tasks/remove/$id">
                <button class="delete-btn">
                    <i class="fa fa-trash"></i> Delete
                </button>
            </a>
        </td>
    </tr>
""".trimIndent()



fun List<Task>.tasksAsTable() = this.joinToString(
    prefix = "<table rules=\"all\">",
    postfix = "</table>",
    separator = "\n",
    transform = Task::taskAsRow
)

