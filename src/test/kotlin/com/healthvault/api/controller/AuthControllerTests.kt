package com.healthvault.api.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.healthvault.api.repository.ExamFolderRepository
import com.healthvault.api.repository.MedicalExamRepository
import com.healthvault.api.repository.UserAccountRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTests(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val examFolderRepository: ExamFolderRepository,
    @Autowired private val medicalExamRepository: MedicalExamRepository,
    @Autowired private val userAccountRepository: UserAccountRepository,
) {
    @BeforeEach
    fun cleanDatabase() {
        medicalExamRepository.deleteAll()
        examFolderRepository.deleteAll()
        userAccountRepository.deleteAll()
    }

    @Test
    fun `logs in and returns a jwt token`() {
        createUser(email = "login@example.com", password = "strong-password")

        mockMvc.post("$AUTH_PATH/login") {
            contentType = MediaType.APPLICATION_JSON
            content = loginJson("login@example.com", "strong-password")
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.token") { exists() }
            jsonPath("$.data.tokenType") { value("Bearer") }
            jsonPath("$.data.expiresAt") { exists() }
            jsonPath("$.data.user.email") { value("login@example.com") }
            jsonPath("$.data.user.passwordHash") { doesNotExist() }
            jsonPath("$.data.user.passwordSalt") { doesNotExist() }
        }
    }

    @Test
    fun `validates a jwt token`() {
        createUser(email = "validate@example.com", password = "strong-password")
        val token = login("validate@example.com", "strong-password")

        mockMvc.post("$AUTH_PATH/validate") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(mapOf("token" to token))
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.valid") { value(true) }
            jsonPath("$.data.email") { value("validate@example.com") }
            jsonPath("$.data.userId") { exists() }
            jsonPath("$.data.expiresAt") { exists() }
        }
    }

    @Test
    fun `rejects login with invalid password`() {
        createUser(email = "wrong-password@example.com", password = "strong-password")

        mockMvc.post("$AUTH_PATH/login") {
            contentType = MediaType.APPLICATION_JSON
            content = loginJson("wrong-password@example.com", "wrong-password")
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.errorMessage") { value("Invalid email or password.") }
        }
    }

    @Test
    fun `rejects login with unknown email`() {
        mockMvc.post("$AUTH_PATH/login") {
            contentType = MediaType.APPLICATION_JSON
            content = loginJson("missing@example.com", "strong-password")
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.errorMessage") { value("Invalid email or password.") }
        }
    }

    @Test
    fun `rejects invalid token`() {
        mockMvc.post("$AUTH_PATH/validate") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(mapOf("token" to "invalid-token"))
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.errorMessage") { value("Token is invalid or expired.") }
        }
    }

    @Test
    fun `rejects invalid login request`() {
        mockMvc.post("$AUTH_PATH/login") {
            contentType = MediaType.APPLICATION_JSON
            content = loginJson("not-an-email", "")
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errorMessage") { exists() }
        }
    }

    @Test
    fun `rejects blank token validation request`() {
        mockMvc.post("$AUTH_PATH/validate") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(mapOf("token" to " "))
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errorMessage") { exists() }
        }
    }

    private fun createUser(email: String, password: String) {
        mockMvc.post(USERS_PATH) {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                mapOf(
                    "name" to "Auth User",
                    "nickname" to "auth",
                    "email" to email,
                    "password" to password,
                ),
            )
        }.andExpect {
            status { isCreated() }
        }
    }

    private fun login(email: String, password: String): String {
        val result = mockMvc.post("$AUTH_PATH/login") {
            contentType = MediaType.APPLICATION_JSON
            content = loginJson(email, password)
        }.andExpect {
            status { isOk() }
        }.andReturn()

        return objectMapper.readTree(result.response.contentAsString)
            .path("data")
            .path("token")
            .asText()
    }

    private fun loginJson(email: String, password: String): String {
        return objectMapper.writeValueAsString(
            mapOf(
                "email" to email,
                "password" to password,
            ),
        )
    }

    companion object {
        private const val AUTH_PATH = "/api/v1/auth"
        private const val USERS_PATH = "/api/v1/users"
    }
}
