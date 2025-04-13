package ru.secon.data

import cafe.adriel.voyager.core.lifecycle.JavaSerializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okio.ByteString.Companion.toByteString
import ru.secon.core.network.NetworkEndpointScope

object TaskApi : NetworkEndpointScope() {
    fun getTasks(): HttpEndpoint<List<Task>> = get("/requests")
    fun createTask(data: CreateTask): HttpEndpoint<String> = post("/requests", body = data)
    fun formAct(data: Unit): HttpEndpoint<String> = post("/act", body = data)
    fun loadExel(file: ByteArray): HttpEndpoint<Unit> =
        post("/requests/by-excel", body = Excel(file.toByteString().base64()))
}

@Serializable
data class Excel(
    @SerialName("FileBytes") val bytes: String
)

@Serializable
data class CreateTask(
    val device: String,
    val type: Int,
    val city: String,
    val street: String,
    val room: String,
    val flat: String,
    val house: String,
)


@Serializable
enum class Type {
    @SerialName("0")
    STOP,

    @SerialName("1")
    RESUME,

    @SerialName("2")
    CONTROL;

    fun toInt() = when (this) {
        STOP -> 0
        RESUME -> 1
        CONTROL -> 2
    }
}

@Serializable
data class Task(
    val id: String,
    val city: String,
    val street: String,
    val room: String,
    val flat: String,
    val house: String,
    val type: Type,
    val device: String,
    // val creationTime: LocalDateTime?,
    val status: TaskStatus,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val beforeImage: String? = null,
    val afterImage: String? = null
) : JavaSerializable {
    fun getTitle() = "г.$city, ул.$street, дом $house, кв $flat"

    @Serializable
    enum class TaskStatus {
        @SerialName("0")
        IN_WORK,

        @SerialName("1")
        CLOSED
    }
}

object PhotoApi : NetworkEndpointScope() {
    fun setFirstPhoto(file: ByteArray, requestId: String): HttpEndpoint<Unit> =
        post("/requests/${requestId}/before-photo", body = Excel(file.toByteString().base64()))

    fun setSecondPhoto(file: ByteArray, requestId: String): HttpEndpoint<String> =
        post("/requests/${requestId}/after-photo", body = Excel(file.toByteString().base64()))
}

object ReportApi : NetworkEndpointScope() {
    fun controlAct(body: Control): HttpEndpoint<Unit> = post("/report/control-act", body = body)
    fun resumeAct(body: Resume): HttpEndpoint<Unit> = post("/report/stop-resume-act", body = body)
}

@Serializable
data class Control(
    val requestId: String,
    val hasViolation: Boolean,
    val hasCommutingDevice: Boolean,
    val meteringDeviceLocationType: Int,
    val meteringDeviceLocation: String,
    val deviceReadings: String
)

@Serializable
data class Resume(
    val requestId: String,
    val hasCommutingDevice: Boolean,
    val result: Int,
    val workMethod: String,
    val meteringDeviceLocation: Int,
    val deviceReadings: String,
    val workMethodType: Int,
    val clientFullName: String
)

