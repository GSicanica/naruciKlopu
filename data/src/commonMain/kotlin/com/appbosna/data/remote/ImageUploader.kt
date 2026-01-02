import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable

// ✅ Response model koji vraća upload.php
@Serializable
data class UploadResponse(
    val url: String? = null,
    val error: String? = null
) {
    val success: Boolean get() = error == null && url != null
}

// ✅ Klasa za upload slike (radi sa tvojim PHP API-jem)
class ImageUploader(
    private val uploadUrl: String,
    private val tokenProvider: () -> String
) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }

    suspend fun uploadImage(bytes: ByteArray, fileName: String): UploadResponse {
        return try {
            val response: HttpResponse = client.submitFormWithBinaryData(
                url = uploadUrl,
                formData = formData {
                    append(
                        key = "file",
                        value = bytes,
                        headers = Headers.build {
                            append(HttpHeaders.ContentType, "image/jpeg")
                            append(HttpHeaders.ContentDisposition, "filename=$fileName")
                        }
                    )
                }
            ) {
                method = HttpMethod.Post
                val token = tokenProvider()
                if (token.isNotBlank()) headers.append(HttpHeaders.Authorization, "Bearer $token")
            }

            if (response.status.isSuccess()) {
                response.body()
            } else {
                UploadResponse(error = "HTTP ${response.status.value}")
            }
        } catch (e: Exception) {
            UploadResponse(error = e.message)
        }
    }
}