package com.healthvault.api.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.healthvault.api.repository.ExamFolderRepository
import com.healthvault.api.repository.MedicalExamFileRepository
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
    @Autowired private val examFolderRepository: ExamFolderRepository,
    @Autowired private val medicalExamFileRepository: MedicalExamFileRepository,
    @Autowired private val medicalExamRepository: MedicalExamRepository,
    @Autowired private val userAccountRepository: UserAccountRepository,
) {
    @BeforeEach
    fun cleanDatabase() {
        medicalExamFileRepository.deleteAll()
        medicalExamRepository.deleteAll()
        examFolderRepository.deleteAll()
        userAccountRepository.deleteAll()
    }

    @Test
    fun `creates a medical exam for a user`() {
        val userId = createUser()
        val folderId = createFolder(userId)

        mockMvc.post(examsPath(userId)) {
            contentType = MediaType.APPLICATION_JSON
            content = examJson(
                performedAt = "2026-01-10",
                requestingDoctor = "Dr. Sarah Connor",
                examType = "Blood test",
                result = "Normal blood count.",
                folderId = folderId,
            )
        }.andExpect {
            status { isCreated() }
            jsonPath("$.data.id") { exists() }
            jsonPath("$.data.userId") { value(userId) }
            jsonPath("$.data.folderId") { value(folderId) }
            jsonPath("$.data.performedAt") { value("2026-01-10") }
            jsonPath("$.data.requestingDoctor") { value("Dr. Sarah Connor") }
            jsonPath("$.data.examType") { value("Blood test") }
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
        val folderId = createFolder(userId)
        createExam(userId, folderId, performedAt = "2025-01-10", result = "Older result.")
        createExam(userId, folderId, performedAt = "2026-01-10", result = "Newer result.")

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
        val folderId = createFolder(userId)
        val examId = createExam(userId, folderId)

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
        val folderId = createFolder(userId)
        val examId = createExam(userId, folderId)

        mockMvc.put("${examsPath(userId)}/$examId") {
            contentType = MediaType.APPLICATION_JSON
            content = examJson(
                performedAt = "2026-02-15",
                requestingDoctor = "Dr. John Watson",
                examType = "MRI",
                result = "Updated result.",
                folderId = folderId,
            )
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.id") { value(examId) }
            jsonPath("$.data.performedAt") { value("2026-02-15") }
            jsonPath("$.data.requestingDoctor") { value("Dr. John Watson") }
            jsonPath("$.data.examType") { value("MRI") }
            jsonPath("$.data.result") { value("Updated result.") }
        }
    }

    @Test
    fun `partially updates a medical exam with patch`() {
        val userId = createUser()
        val folderId = createFolder(userId)
        val examId = createExam(userId, folderId, result = "Initial result.")

        mockMvc.patch("${examsPath(userId)}/$examId") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                mapOf(
                    "examType" to "Ultrasound",
                    "result" to "Patched result.",
                ),
            )
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.id") { value(examId) }
            jsonPath("$.data.requestingDoctor") { value("Dr. Jane Foster") }
            jsonPath("$.data.examType") { value("Ultrasound") }
            jsonPath("$.data.result") { value("Patched result.") }
        }
    }

    @Test
    fun `deletes a medical exam`() {
        val userId = createUser()
        val folderId = createFolder(userId)
        val examId = createExam(userId, folderId)

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
    fun `deletes medical exam with file metadata`() {
        val userId = createUser()
        val folderId = createFolder(userId)
        val examId = createExam(userId, folderId)
        createExamFile(userId, examId)

        mockMvc.delete("${examsPath(userId)}/$examId")
            .andExpect {
                status { isNoContent() }
            }

        assert(medicalExamFileRepository.findAll().isEmpty())
    }

    @Test
    fun `rejects medical exam for missing user`() {
        mockMvc.post(examsPath("018fd6a9-0a58-7cc4-8f63-7a1aee35b8f2")) {
            contentType = MediaType.APPLICATION_JSON
            content = examJson(folderId = "018fd6a9-0a58-7cc4-8f63-7a1aee35b8f2")
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.errorMessage") { value("User account not found.") }
        }
    }

    @Test
    fun `rejects invalid medical exam request`() {
        val userId = createUser()
        val folderId = createFolder(userId)

        mockMvc.post(examsPath(userId)) {
            contentType = MediaType.APPLICATION_JSON
            content = examJson(
                performedAt = LocalDate.now().plusDays(1).toString(),
                requestingDoctor = "",
                examType = "",
                result = "",
                folderId = folderId,
            )
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errorMessage") { exists() }
        }
    }

    @Test
    fun `rejects blank medical exam field in partial update`() {
        val userId = createUser()
        val folderId = createFolder(userId)
        val examId = createExam(userId, folderId)

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
        val folderId = createFolder(firstUserId)
        val examId = createExam(firstUserId, folderId)

        mockMvc.get("${examsPath(secondUserId)}/$examId")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.errorMessage") { value("Medical exam not found.") }
            }
    }

    @Test
    fun `moves a medical exam between folders`() {
        val userId = createUser()
        val sourceFolderId = createFolder(userId, name = "Source")
        val targetFolderId = createFolder(userId, name = "Target")
        val examId = createExam(userId, sourceFolderId)

        mockMvc.patch("${examsPath(userId)}/$examId/folder") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(mapOf("folderId" to targetFolderId))
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.id") { value(examId) }
            jsonPath("$.data.folderId") { value(targetFolderId) }
        }
    }

    @Test
    fun `searches medical exams by date doctor and type`() {
        val userId = createUser()
        val folderId = createFolder(userId)
        createExam(
            userId = userId,
            folderId = folderId,
            performedAt = "2026-01-10",
            requestingDoctor = "Dr. Alice Smith",
            examType = "Blood test",
        )
        createExam(
            userId = userId,
            folderId = folderId,
            performedAt = "2026-02-20",
            requestingDoctor = "Dr. Bob Jones",
            examType = "MRI",
        )

        mockMvc.get(examsPath(userId)) {
            param("date", "2026-01")
            param("doctor", "alice")
            param("examType", "blood")
        }.andExpect {
            status { isOk() }
            jsonPath("$.data", hasSize<Any>(1))
            jsonPath("$.data[0].requestingDoctor") { value("Dr. Alice Smith") }
            jsonPath("$.data[0].examType") { value("Blood test") }
        }
    }

    @Test
    fun `returns folder tree with nested folders and exams`() {
        val userId = createUser()
        val rootFolderId = createFolder(userId, name = "Lab")
        val childFolderId = createFolder(userId, name = "Blood", parentId = rootFolderId)
        createExam(userId, childFolderId, examType = "Blood test")

        mockMvc.get("$FOLDERS_PATH_BASE/$userId/exam-folders/tree")
            .andExpect {
                status { isOk() }
                jsonPath("$.data", hasSize<Any>(1))
                jsonPath("$.data[0].id") { value(rootFolderId) }
                jsonPath("$.data[0].subfolders[0].id") { value(childFolderId) }
                jsonPath("$.data[0].subfolders[0].exams", hasSize<Any>(1))
                jsonPath("$.data[0].subfolders[0].exams[0].examType") { value("Blood test") }
            }
    }

    @Test
    fun `creates a presigned upload url for a medical exam file`() {
        val userId = createUser()
        val folderId = createFolder(userId)
        val examId = createExam(userId, folderId)

        mockMvc.post("${examsPath(userId)}/$examId/files/upload-url") {
            contentType = MediaType.APPLICATION_JSON
            content = examFileJson(
                fileName = "blood-result.pdf",
                contentType = "application/pdf",
                sizeBytes = 1024,
            )
        }.andExpect {
            status { isCreated() }
            jsonPath("$.data.file.id") { exists() }
            jsonPath("$.data.file.examId") { value(examId) }
            jsonPath("$.data.file.fileName") { value("blood-result.pdf") }
            jsonPath("$.data.file.contentType") { value("application/pdf") }
            jsonPath("$.data.file.sizeBytes") { value(1024) }
            jsonPath("$.data.method") { value("PUT") }
            jsonPath("$.data.url") { exists() }
            jsonPath("$.data.headers.Content-Type") { value("application/pdf") }
            jsonPath("$.data.expiresAt") { exists() }
        }

        val file = medicalExamFileRepository.findAll().single()
        assert(file.id.version() == 7)
    }

    @Test
    fun `lists medical exam file metadata and includes files in exam response`() {
        val userId = createUser()
        val folderId = createFolder(userId)
        val examId = createExam(userId, folderId)
        createExamFile(userId, examId, fileName = "xray.png", mimeType = "image/png")

        mockMvc.get("${examsPath(userId)}/$examId/files")
            .andExpect {
                status { isOk() }
                jsonPath("$.data", hasSize<Any>(1))
                jsonPath("$.data[0].fileName") { value("xray.png") }
                jsonPath("$.data[0].contentType") { value("image/png") }
            }

        mockMvc.get("${examsPath(userId)}/$examId")
            .andExpect {
                status { isOk() }
                jsonPath("$.data.files", hasSize<Any>(1))
                jsonPath("$.data.files[0].fileName") { value("xray.png") }
            }
    }

    @Test
    fun `creates a presigned download url for a medical exam file`() {
        val userId = createUser()
        val folderId = createFolder(userId)
        val examId = createExam(userId, folderId)
        val fileId = createExamFile(userId, examId)

        mockMvc.get("${examsPath(userId)}/$examId/files/$fileId/download-url")
            .andExpect {
                status { isOk() }
                jsonPath("$.data.file.id") { value(fileId) }
                jsonPath("$.data.method") { value("GET") }
                jsonPath("$.data.url") { exists() }
                jsonPath("$.data.headers") { exists() }
                jsonPath("$.data.expiresAt") { exists() }
            }
    }

    @Test
    fun `deletes medical exam file metadata`() {
        val userId = createUser()
        val folderId = createFolder(userId)
        val examId = createExam(userId, folderId)
        val fileId = createExamFile(userId, examId)

        mockMvc.delete("${examsPath(userId)}/$examId/files/$fileId")
            .andExpect {
                status { isNoContent() }
                content { string(blankOrNullString()) }
            }

        mockMvc.get("${examsPath(userId)}/$examId/files/$fileId/download-url")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.errorMessage") { value("Medical exam file not found.") }
            }
    }

    @Test
    fun `rejects invalid medical exam file upload request`() {
        val userId = createUser()
        val folderId = createFolder(userId)
        val examId = createExam(userId, folderId)

        mockMvc.post("${examsPath(userId)}/$examId/files/upload-url") {
            contentType = MediaType.APPLICATION_JSON
            content = examFileJson(fileName = "", contentType = "", sizeBytes = 0)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errorMessage") { exists() }
        }
    }

    @Test
    fun `rejects folders deeper than three levels`() {
        val userId = createUser()
        val firstId = createFolder(userId, name = "Level 1")
        val secondId = createFolder(userId, name = "Level 2", parentId = firstId)
        val thirdId = createFolder(userId, name = "Level 3", parentId = secondId)

        mockMvc.post("$FOLDERS_PATH_BASE/$userId/exam-folders") {
            contentType = MediaType.APPLICATION_JSON
            content = folderJson(name = "Level 4", parentId = thirdId)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errorMessage") { value("Folders can have at most 3 levels.") }
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

    private fun createFolder(
        userId: String,
        name: String = "General",
        parentId: String? = null,
    ): String {
        val response = mockMvc.post("$FOLDERS_PATH_BASE/$userId/exam-folders") {
            contentType = MediaType.APPLICATION_JSON
            content = folderJson(name = name, parentId = parentId)
        }.andExpect {
            status { isCreated() }
        }.andReturn()

        return objectMapper.readTree(response.response.contentAsString)
            .path("data")
            .path("id")
            .asText()
    }

    private fun createExam(
        userId: String,
        folderId: String,
        performedAt: String = "2026-01-10",
        requestingDoctor: String = "Dr. Jane Foster",
        examType: String = "Blood test",
        result: String = "Normal result.",
    ): String {
        val response = mockMvc.post(examsPath(userId)) {
            contentType = MediaType.APPLICATION_JSON
            content = examJson(performedAt, requestingDoctor, examType, result, folderId)
        }.andExpect {
            status { isCreated() }
        }.andReturn()

        return objectMapper.readTree(response.response.contentAsString)
            .path("data")
            .path("id")
            .asText()
    }

    private fun createExamFile(
        userId: String,
        examId: String,
        fileName: String = "blood-result.pdf",
        mimeType: String = "application/pdf",
        sizeBytes: Long = 1024,
    ): String {
        val response = mockMvc.post("${examsPath(userId)}/$examId/files/upload-url") {
            contentType = MediaType.APPLICATION_JSON
            content = examFileJson(fileName, mimeType, sizeBytes)
        }.andExpect {
            status { isCreated() }
        }.andReturn()

        return objectMapper.readTree(response.response.contentAsString)
            .path("data")
            .path("file")
            .path("id")
            .asText()
    }

    private fun examJson(
        performedAt: String = "2026-01-10",
        requestingDoctor: String = "Dr. Jane Foster",
        examType: String = "Blood test",
        result: String = "Normal result.",
        folderId: String,
    ): String {
        return objectMapper.writeValueAsString(
            mapOf(
                "performedAt" to performedAt,
                "requestingDoctor" to requestingDoctor,
                "examType" to examType,
                "result" to result,
                "folderId" to folderId,
            ),
        )
    }

    private fun folderJson(name: String, parentId: String? = null): String {
        return objectMapper.writeValueAsString(
            mapOf(
                "name" to name,
                "parentId" to parentId,
            ),
        )
    }

    private fun examFileJson(fileName: String, contentType: String, sizeBytes: Long): String {
        return objectMapper.writeValueAsString(
            mapOf(
                "fileName" to fileName,
                "contentType" to contentType,
                "sizeBytes" to sizeBytes,
            ),
        )
    }

    private fun examsPath(userId: String): String {
        return "$USERS_PATH/$userId/exams"
    }

    companion object {
        private const val USERS_PATH = "/api/v1/users"
        private const val FOLDERS_PATH_BASE = "/api/v1/users"
    }
}
