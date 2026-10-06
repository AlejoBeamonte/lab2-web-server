package es.unizar.webeng.lab2

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.server.LocalServerPort
import java.net.Socket
import java.nio.charset.StandardCharsets

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class CompressionTest {
    @LocalServerPort
    private var port: Int = 0

    @Test
    fun timeResponseIsGzippedWhenRequested() {
        val response = rawTimeRequest()

        assertTrue(response.startsWith("HTTP/1.1 200"))
        assertTrue(
            response.lowercase().contains("\r\ncontent-encoding: gzip\r\n"),
            response.substringBefore("\r\n\r\n"),
        )
    }

    private fun rawTimeRequest(): String =
        Socket("127.0.0.1", port).use { socket ->
            val request =
                "GET /time HTTP/1.1\r\n" +
                    "Host: 127.0.0.1:$port\r\n" +
                    "Accept: application/json\r\n" +
                    "Accept-Encoding: gzip\r\n" +
                    "Connection: close\r\n\r\n"
            socket.getOutputStream().write(request.toByteArray(StandardCharsets.US_ASCII))
            socket.getOutputStream().flush()
            socket.getInputStream().readBytes().toString(StandardCharsets.ISO_8859_1)
        }
}
