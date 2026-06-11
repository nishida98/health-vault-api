package com.healthvault.api.controller

import com.healthvault.api.model.HealthVaultOverview
import com.healthvault.api.service.HealthVaultService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/healthvault")
class HealthVaultController(
    private val healthVaultService: HealthVaultService,
) {
    @GetMapping
    fun getOverview(): HealthVaultOverview {
        return healthVaultService.getOverview()
    }
}
