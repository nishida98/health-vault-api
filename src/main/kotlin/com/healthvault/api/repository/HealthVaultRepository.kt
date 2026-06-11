package com.healthvault.api.repository

import com.healthvault.api.model.HealthVaultOverview
import org.springframework.stereotype.Repository

@Repository
class HealthVaultRepository {
    fun findOverview(): HealthVaultOverview {
        return HealthVaultOverview(
            name = "HealthVault API",
            description = "Backend API for centralized and organized patient medical records.",
            status = "INITIAL_SETUP",
            capabilities = listOf(
                "Patient account management",
                "Structured medical record storage",
                "Medical record organization by category and date",
                "Secure access to sensitive health information",
            ),
        )
    }
}
