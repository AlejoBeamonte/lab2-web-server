package es.unizar.webeng.lab2

import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.MessageSource
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.context.request.WebRequest
import java.util.Locale

@Controller
class TimeController(
    private val snapshots: TimeSnapshotService,
    private val messageSource: MessageSource,
) {
    @GetMapping("/time")
    fun time(
        model: Model,
        locale: Locale,
        webRequest: WebRequest,
        response: HttpServletResponse,
    ): String? {
        val snapshot = snapshots.current()
        val label = messageSource.getMessage("time.label", null, locale)
        response.setHeader(
            HttpHeaders.VARY,
            "${HttpHeaders.ACCEPT}, ${HttpHeaders.ACCEPT_LANGUAGE}, ${HttpHeaders.ACCEPT_ENCODING}",
        )
        if (webRequest.checkNotModified(snapshot.etag(locale), snapshot.lastModified)) {
            return null
        }
        model.addAttribute(TIME_MODEL_ATTRIBUTE, snapshot.toDTO(label))
        return "time"
    }
}
