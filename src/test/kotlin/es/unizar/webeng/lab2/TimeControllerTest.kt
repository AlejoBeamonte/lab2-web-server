package es.unizar.webeng.lab2

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
@Import(TimeControllerTest.FixedTimeProviderConfiguration::class)
class TimeControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun timeIsJsonInEnglish() {
        mockMvc
            .perform(
                get("/time")
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.ACCEPT_LANGUAGE, "en"),
            ).andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.label").value("Current server time"))
            .andExpect(jsonPath("$.time").exists())
            .andExpect(jsonPath("$.time").value(FIXED_TIME.toString()))
    }

    @Test
    fun timeIsJsonInSpanish() {
        mockMvc
            .perform(
                get("/time")
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.ACCEPT_LANGUAGE, "es"),
            ).andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.label").value("Hora actual del servidor"))
            .andExpect(jsonPath("$.time").value(FIXED_TIME.toString()))
    }

    @Test
    fun timeIsXmlInEnglish() {
        mockMvc
            .perform(
                get("/time")
                    .accept(MediaType.APPLICATION_XML)
                    .header(HttpHeaders.ACCEPT_LANGUAGE, "en"),
            ).andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
            .andExpect(xpath("/TimeDTO/label").string("Current server time"))
            .andExpect(xpath("/TimeDTO/time").string(FIXED_TIME.toString()))
    }

    @Test
    fun timeIsXmlInSpanish() {
        mockMvc
            .perform(
                get("/time")
                    .accept(MediaType.APPLICATION_XML)
                    .header(HttpHeaders.ACCEPT_LANGUAGE, "es"),
            ).andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
            .andExpect(xpath("/TimeDTO/label").string("Hora actual del servidor"))
            .andExpect(xpath("/TimeDTO/time").string(FIXED_TIME.toString()))
    }

    @Test
    fun timeIsHtmlInEnglish() {
        mockMvc
            .perform(
                get("/time")
                    .accept(MediaType.TEXT_HTML)
                    .header(HttpHeaders.ACCEPT_LANGUAGE, "en"),
            ).andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("lang=\"en\"")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Current server time")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString(FIXED_TIME.toString())))
    }

    @Test
    fun timeIsHtmlInSpanish() {
        mockMvc
            .perform(
                get("/time")
                    .accept(MediaType.TEXT_HTML)
                    .header(HttpHeaders.ACCEPT_LANGUAGE, "es"),
            ).andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("lang=\"es\"")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Hora actual del servidor")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString(FIXED_TIME.toString())))
    }

    @Test
    fun unsupportedTimeRepresentationIsNotAcceptable() {
        mockMvc
            .perform(get("/time").accept(MediaType.APPLICATION_PDF))
            .andExpect(status().isNotAcceptable)
    }

    @TestConfiguration(proxyBeanMethods = false)
    class FixedTimeProviderConfiguration {
        @Bean
        @Primary
        fun fixedTimeProvider(): TimeProvider =
            object : TimeProvider {
                override fun now(): LocalDateTime = FIXED_TIME
            }
    }

    companion object {
        private val FIXED_TIME: LocalDateTime = LocalDateTime.of(2026, 9, 30, 10, 25, 34, 123000000)
    }
}
