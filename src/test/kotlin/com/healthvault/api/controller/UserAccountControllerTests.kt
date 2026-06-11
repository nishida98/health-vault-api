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

@SpringBootTest
@AutoConfigureMockMvc
class UserAccountControllerTests(
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
    fun `creates a user account with hashed password and random salt`() {
        mockMvc.post(USERS_PATH) {
            contentType = MediaType.APPLICATION_JSON
            content = createUserJson(
                name = "Jane Doe",
                nickname = "jane",
                email = "Jane.Doe@Example.com",
                password = "strong-password",
            )
        }.andExpect {
            status { isCreated() }
            jsonPath("$.data.id") { exists() }
            jsonPath("$.data.name") { value("Jane Doe") }
            jsonPath("$.data.nickname") { value("jane") }
            jsonPath("$.data.email") { value("jane.doe@example.com") }
            jsonPath("$.data.createdAt") { exists() }
            jsonPath("$.data.updatedAt") { exists() }
            jsonPath("$.data.password") { doesNotExist() }
            jsonPath("$.data.passwordHash") { doesNotExist() }
            jsonPath("$.data.passwordSalt") { doesNotExist() }
        }

        val user = userAccountRepository.findAll().single()
        assert(user.id.version() == 7)
        assert(user.passwordHash != "strong-password")
        assert(user.passwordHash.isNotBlank())
        assert(user.passwordSalt.isNotBlank())
    }

    @Test
    fun `finds all user accounts`() {
        createUser(email = "first@example.com", nickname = "first")
        createUser(email = "second@example.com", nickname = "second")

        mockMvc.get(USERS_PATH)
            .andExpect {
                status { isOk() }
                jsonPath("$.data") { isArray() }
                jsonPath("$.data", hasSize<Any>(2))
            }
    }

    @Test
    fun `finds a user account by id`() {
        val id = createUser(email = "find@example.com", nickname = "finder")

        mockMvc.get("$USERS_PATH/$id")
            .andExpect {
                status { isOk() }
                jsonPath("$.data.id") { value(id) }
                jsonPath("$.data.email") { value("find@example.com") }
            }
    }

    @Test
    fun `replaces a user account with put`() {
        val id = createUser(email = "old@example.com", nickname = "old")

        mockMvc.put("$USERS_PATH/$id") {
            contentType = MediaType.APPLICATION_JSON
            content = createUserJson(
                name = "New Name",
                nickname = "new",
                email = "new@example.com",
                password = "new-password",
            )
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.id") { value(id) }
            jsonPath("$.data.name") { value("New Name") }
            jsonPath("$.data.nickname") { value("new") }
            jsonPath("$.data.email") { value("new@example.com") }
        }
    }

    @Test
    fun `partially updates a user account with patch`() {
        val id = createUser(email = "patch@example.com", nickname = "before")

        mockMvc.patch("$USERS_PATH/$id") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                mapOf(
                    "name" to "Patched Name",
                    "nickname" to "after",
                ),
            )
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.id") { value(id) }
            jsonPath("$.data.name") { value("Patched Name") }
            jsonPath("$.data.nickname") { value("after") }
            jsonPath("$.data.email") { value("patch@example.com") }
        }
    }

    @Test
    fun `deletes a user account`() {
        val id = createUser(email = "delete@example.com", nickname = "delete")

        mockMvc.delete("$USERS_PATH/$id")
            .andExpect {
                status { isNoContent() }
                content { string(blankOrNullString()) }
            }

        mockMvc.get("$USERS_PATH/$id")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.errorMessage") { value("User account not found.") }
            }
    }

    @Test
    fun `rejects invalid create request`() {
        mockMvc.post(USERS_PATH) {
            contentType = MediaType.APPLICATION_JSON
            content = createUserJson(
                name = "",
                nickname = "invalid",
                email = "not-an-email",
                password = "short",
            )
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errorMessage") { exists() }
        }
    }

    @Test
    fun `rejects duplicate email`() {
        createUser(email = "duplicate@example.com", nickname = "first")

        mockMvc.post(USERS_PATH) {
            contentType = MediaType.APPLICATION_JSON
            content = createUserJson(
                name = "Second User",
                nickname = "second",
                email = "DUPLICATE@example.com",
                password = "strong-password",
            )
        }.andExpect {
            status { isConflict() }
            jsonPath("$.errorMessage") { value("Email is already in use.") }
        }
    }

    @Test
    fun `returns not found for missing user account`() {
        mockMvc.get("$USERS_PATH/018fd6a9-0a58-7cc4-8f63-7a1aee35b8f2")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.errorMessage") { value("User account not found.") }
            }
    }

    @Test
    fun `rejects invalid id format`() {
        mockMvc.get("$USERS_PATH/not-a-uuid")
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.errorMessage") { value("id is invalid.") }
            }
    }

    @Test
    fun `rejects blank field in partial update`() {
        val id = createUser(email = "blank@example.com", nickname = "blank")

        mockMvc.patch("$USERS_PATH/$id") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(mapOf("name" to " "))
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errorMessage") { value("name must not be blank.") }
        }
    }

    @Test
    fun `rejects malformed request body`() {
        mockMvc.post(USERS_PATH) {
            contentType = MediaType.APPLICATION_JSON
            content = "{"
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errorMessage") { value("Request body is invalid.") }
        }
    }

    private fun createUser(
        name: String = "Test User",
        nickname: String,
        email: String,
        password: String = "strong-password",
    ): String {
        val result = mockMvc.post(USERS_PATH) {
            contentType = MediaType.APPLICATION_JSON
            content = createUserJson(name, nickname, email, password)
        }.andExpect {
            status { isCreated() }
        }.andReturn()

        return objectMapper.readTree(result.response.contentAsString)
            .path("data")
            .path("id")
            .asText()
    }

    private fun createUserJson(
        name: String,
        nickname: String,
        email: String,
        password: String,
    ): String {
        return objectMapper.writeValueAsString(
            mapOf(
                "name" to name,
                "nickname" to nickname,
                "email" to email,
                "password" to password,
            ),
        )
    }

    companion object {
        private const val USERS_PATH = "/api/v1/users"
    }
}
