package es.unizar.webeng.lab2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class ErrorPageTest {
    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var client: TestRestTemplate

    @Test
    fun unknownPathRendersCustomErrorPage() {
        val headers = HttpHeaders()
        headers.accept = listOf(MediaType.TEXT_HTML)

        val response =
            client.exchange(
                "http://127.0.0.1:$port/missing",
                HttpMethod.GET,
                HttpEntity<Void>(
                    headers,
                ),
                String::class.java,
            )

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertTrue(response.body.orEmpty().contains("Custom error page marker"))
        assertTrue(response.body.orEmpty().contains("Status: <span>404</span>"))
        assertTrue(response.body.orEmpty().contains("Path: <span>/missing</span>"))
    }
}
