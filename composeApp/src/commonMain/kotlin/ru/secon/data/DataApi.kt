package ru.secon.data

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import ru.secon.core.network.NetworkEndpointScope

object TaskApi : NetworkEndpointScope() {
    fun getTasks(): HttpEndpoint<List<Task>> = get("/tasks")
    fun createTask(): HttpEndpoint<String> = post("/tasks")
}

@Serializable
data class Task(
    val city: String,
    val street: String,
    val room: String,
    val apartment: String,
    val type: String,
    val date: LocalDateTime
)

object ProfilesApi : NetworkEndpointScope() {
    fun getProfiles(): HttpEndpoint<String> = get("/tasks")
    fun createProfile(): HttpEndpoint<String> = post("/tasks")
}

object RApi : NetworkEndpointScope() {
    fun getTasks(): HttpEndpoint<String> = get("/tasks")
    fun createTask(): HttpEndpoint<String> = post("/tasks")
}
