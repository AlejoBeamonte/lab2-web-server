package es.unizar.webeng.lab2

import org.springframework.stereotype.Service
import java.time.LocalDateTime

data class TimeDTO(
    val label: String,
    val time: LocalDateTime,
)

interface TimeProvider {
    fun now(): LocalDateTime
}

@Service
class TimeService : TimeProvider {
    override fun now(): LocalDateTime = LocalDateTime.now()
}

fun LocalDateTime.toDTO(label: String): TimeDTO = TimeDTO(label = label, time = this)
