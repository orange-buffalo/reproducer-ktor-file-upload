import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.cio.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Files

class FileUploadTest {

    private lateinit var wireMockServer: WireMockServer

    @BeforeEach
    fun setup() {
        wireMockServer = WireMockServer(WireMockConfiguration.options().dynamicPort())
        wireMockServer.start()
        WireMock.configureFor("localhost", wireMockServer.port())
    }

    @AfterEach
    fun teardown() {
        wireMockServer.stop()
    }

    @Test
    fun `should upload file and receive same content`() = runBlocking {
        val content = "This is a test content"
        val tempFile = Files.createTempFile("temp", ".txt").toFile()
        tempFile.writeText(content)

        val url = "http://localhost:${wireMockServer.port()}/upload"
        WireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/upload")).willReturn(WireMock.aResponse().withStatus(200)))

        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
            defaultRequest {
                contentType(ContentType.Application.Json)
            }
        }

        val response: HttpResponse = client.post {
            url(url)
            contentType(ContentType.Application.OctetStream)
            setBody(tempFile.readChannel())
        }

        assertEquals(HttpStatusCode.OK, response.status)

        WireMock.verify(
            WireMock.postRequestedFor(WireMock.urlEqualTo("/upload"))
                .withRequestBody(WireMock.equalTo(content))
                .withHeader("Content-Type", WireMock.equalTo("application/octet-stream"))
        )

        tempFile.delete()
        client.close()
    }
}
