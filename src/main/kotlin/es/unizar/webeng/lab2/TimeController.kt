package es.unizar.webeng.lab2

import org.springframework.context.MessageSource
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import java.util.Locale

@Controller
class TimeController(
    private val service: TimeProvider,
    private val messageSource: MessageSource,
) {
    @GetMapping("/time")
    fun time(
        model: Model,
        locale: Locale,
    ): String {
        val label = messageSource.getMessage("time.label", null, locale)
        model.addAttribute(TIME_MODEL_ATTRIBUTE, service.now().toDTO(label))
        return "time"
    }
}
