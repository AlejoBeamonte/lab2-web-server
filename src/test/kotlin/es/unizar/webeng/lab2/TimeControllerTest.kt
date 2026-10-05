package es.unizar.webeng.lab2

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
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
    fun timeIsJson() {
        mockMvc
            .perform(get("/time").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.time").exists())
            .andExpect(jsonPath("$.time").value(FIXED_TIME.toString()))
    }

    @Test
    fun timeIsXml() {
        mockMvc
            .perform(get("/time").accept(MediaType.APPLICATION_XML))
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
            .andExpect(xpath("/TimeDTO/time").string(FIXED_TIME.toString()))
    }

    @Test
    fun timeIsHtml() {
        mockMvc
            .perform(get("/time").accept(MediaType.TEXT_HTML))
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Current server time")))
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
