package com.healthvault.api.service

import com.healthvault.api.model.HealthVaultOverview
import com.healthvault.api.repository.HealthVaultRepository
import org.springframework.stereotype.Service

@Service
class HealthVaultService(
    private val healthVaultRepository: HealthVaultRepository,
) {
    fun getOverview(): HealthVaultOverview {
        return healthVaultRepository.findOverview()
    }
}
