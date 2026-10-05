package es.unizar.webeng.lab2

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class TimeController(
    private val service: TimeProvider,
) {
    @GetMapping("/time")
    fun time(model: Model): String {
        model.addAttribute(TIME_MODEL_ATTRIBUTE, service.now().toDTO())
        return "time"
    }
}
