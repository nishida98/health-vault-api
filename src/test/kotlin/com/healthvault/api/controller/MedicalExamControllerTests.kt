package com.healthvault.api.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.healthvault.api.repository.MedicalExamRepository
import com.healthvault.api.repository.UserAccountRepository
import org.hamcrest.Matchers.blankOrNullString
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.time.LocalDate

@SpringBootTest
@AutoConfigureMockMvc
class MedicalExamControllerTests(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val medicalExamRepository: MedicalExamRepository,
    @Autowired private val userAccountRepository: UserAccountRepository,
) {
    @BeforeEach
    fun cleanDatabase() {
        medicalExamRepository.deleteAll()
        userAccountRepository.deleteAll()
    }

    @Test
    fun `creates a medical exam for a user`() {
        val userId = createUser()

        mockMvc.post(examsPath(userId)) {
            contentType = MediaType.APPLICATION_JSON
            content = examJson(
                performedAt = "2026-01-10",
                requestingDoctor = "Dr. Sarah Connor",
                result = "Normal blood count.",
            )
        }.andExpect {
            status { isCreated() }
            jsonPath("$.data.id") { exists() }
            jsonPath("$.data.userId") { value(userId) }
            jsonPath("$.data.performedAt") { value("2026-01-10") }
            jsonPath("$.data.requestingDoctor") { value("Dr. Sarah Connor") }
            jsonPath("$.data.result") { value("Normal blood count.") }
            jsonPath("$.data.createdAt") { exists() }
            jsonPath("$.data.updatedAt") { exists() }
        }

        val exam = medicalExamRepository.findAll().single()
        assert(exam.id.version() == 7)
    }

    @Test
    fun `finds all medical exams for a user ordered by performed date desc`() {
        val userId = createUser()
        createExam(userId, performedAt = "2025-01-10", result = "Older result.")
        createExam(userId, performedAt = "2026-01-10", result = "Newer result.")

        mockMvc.get(examsPath(userId))
            .andExpect {
                status { isOk() }
                jsonPath("$.data", hasSize<Any>(2))
                jsonPath("$.data[0].performedAt") { value("2026-01-10") }
                jsonPath("$.data[1].performedAt") { value("2025-01-10") }
            }
    }

    @Test
    fun `finds a medical exam by id`() {
        val userId = createUser()
        val examId = createExam(userId)

        mockMvc.get("${examsPath(userId)}/$examId")
            .andExpect {
                status { isOk() }
                jsonPath("$.data.id") { value(examId) }
                jsonPath("$.data.userId") { value(userId) }
            }
    }

    @Test
    fun `replaces a medical exam with put`() {
        val userId = createUser()
        val examId = createExam(userId)

        mockMvc.put("${examsPath(userId)}/$examId") {
            contentType = MediaType.APPLICATION_JSON
            content = examJson(
                performedAt = "2026-02-15",
                requestingDoctor = "Dr. John Watson",
                result = "Updated result.",
            )
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.id") { value(examId) }
            jsonPath("$.data.performedAt") { value("2026-02-15") }
            jsonPath("$.data.requestingDoctor") { value("Dr. John Watson") }
            jsonPath("$.data.result") { value("Updated result.") }
        }
    }

    @Test
    fun `partially updates a medical exam with patch`() {
        val userId = createUser()
        val examId = createExam(userId, result = "Initial result.")

        mockMvc.patch("${examsPath(userId)}/$examId") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(mapOf("result" to "Patched result."))
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.id") { value(examId) }
            jsonPath("$.data.requestingDoctor") { value("Dr. Jane Foster") }
            jsonPath("$.data.result") { value("Patched result.") }
        }
    }

    @Test
    fun `deletes a medical exam`() {
        val userId = createUser()
        val examId = createExam(userId)

        mockMvc.delete("${examsPath(userId)}/$examId")
            .andExpect {
                status { isNoContent() }
                content { string(blankOrNullString()) }
            }

        mockMvc.get("${examsPath(userId)}/$examId")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.errorMessage") { value("Medical exam not found.") }
            }
    }

    @Test
    fun `rejects medical exam for missing user`() {
        mockMvc.post(examsPath("018fd6a9-0a58-7cc4-8f63-7a1aee35b8f2")) {
            contentType = MediaType.APPLICATION_JSON
            content = examJson()
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.errorMessage") { value("User account not found.") }
        }
    }

    @Test
    fun `rejects invalid medical exam request`() {
        val userId = createUser()

        mockMvc.post(examsPath(userId)) {
            contentType = MediaType.APPLICATION_JSON
            content = examJson(
                performedAt = LocalDate.now().plusDays(1).toString(),
                requestingDoctor = "",
                result = "",
            )
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errorMessage") { exists() }
        }
    }

    @Test
    fun `rejects blank medical exam field in partial update`() {
        val userId = createUser()
        val examId = createExam(userId)

        mockMvc.patch("${examsPath(userId)}/$examId") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(mapOf("result" to " "))
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errorMessage") { value("result must not be blank.") }
        }
    }

    @Test
    fun `does not expose another users medical exam`() {
        val firstUserId = createUser(email = "first-exam@example.com")
        val secondUserId = createUser(email = "second-exam@example.com")
        val examId = createExam(firstUserId)

        mockMvc.get("${examsPath(secondUserId)}/$examId")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.errorMessage") { value("Medical exam not found.") }
            }
    }

    private fun createUser(email: String = "exam-user@example.com"): String {
        val result = mockMvc.post(USERS_PATH) {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                mapOf(
                    "name" to "Exam User",
                    "nickname" to email.substringBefore("@"),
                    "email" to email,
                    "password" to "strong-password",
                ),
            )
        }.andExpect {
            status { isCreated() }
        }.andReturn()

        return objectMapper.readTree(result.response.contentAsString)
            .path("data")
            .path("id")
            .asText()
    }

    private fun createExam(
        userId: String,
        performedAt: String = "2026-01-10",
        requestingDoctor: String = "Dr. Jane Foster",
        result: String = "Normal result.",
    ): String {
        val response = mockMvc.post(examsPath(userId)) {
            contentType = MediaType.APPLICATION_JSON
            content = examJson(performedAt, requestingDoctor, result)
        }.andExpect {
            status { isCreated() }
        }.andReturn()

        return objectMapper.readTree(response.response.contentAsString)
            .path("data")
            .path("id")
            .asText()
    }

    private fun examJson(
        performedAt: String = "2026-01-10",
        requestingDoctor: String = "Dr. Jane Foster",
        result: String = "Normal result.",
    ): String {
        return objectMapper.writeValueAsString(
            mapOf(
                "performedAt" to performedAt,
                "requestingDoctor" to requestingDoctor,
                "result" to result,
            ),
        )
    }

    private fun examsPath(userId: String): String {
        return "$USERS_PATH/$userId/exams"
    }

    companion object {
        private const val USERS_PATH = "/api/v1/users"
    }
}
