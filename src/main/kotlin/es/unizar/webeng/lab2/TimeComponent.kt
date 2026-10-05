package es.unizar.webeng.lab2

import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Locale

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

data class TimeSnapshot(
    val time: LocalDateTime,
    private val version: LocalDateTime,
) {
    val lastModified: Long =
        time
            .withNano(0)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

    fun etag(locale: Locale): String = "W/\"time-${lastModified / 60_000}-${locale.toLanguageTag()}\""

    fun hasVersion(candidate: LocalDateTime): Boolean = version == candidate
}

@Service
class TimeSnapshotService(
    private val timeProvider: TimeProvider,
) {
    private var snapshot: TimeSnapshot? = null

    @Synchronized
    fun current(): TimeSnapshot {
        val now = timeProvider.now()
        val version = now.truncatedTo(ChronoUnit.MINUTES)
        if (snapshot?.hasVersion(version) != true) {
            snapshot = TimeSnapshot(now, version)
        }
        return checkNotNull(snapshot)
    }
}

fun TimeSnapshot.toDTO(label: String): TimeDTO = TimeDTO(label = label, time = time)
