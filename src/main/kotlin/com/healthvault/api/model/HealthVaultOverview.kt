package com.healthvault.api.model

data class HealthVaultOverview(
    val name: String,
    val description: String,
    val status: String,
    val capabilities: List<String>,
)
