import com.jomariabejo.ktor.module
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import junit.framework.TestCase.assertEquals
import kotlin.test.Test
import kotlin.test.assertNotEquals

class ApplicationTest {
    @Test
    fun testRoot(): Unit = testApplication {
        application {
            module()
        }

        // Test Helloworld
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Hello World!", response.bodyAsText())
    }
}