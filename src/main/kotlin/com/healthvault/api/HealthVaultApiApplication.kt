package com.healthvault.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class HealthVaultApiApplication

fun main(args: Array<String>) {
    runApplication<HealthVaultApiApplication>(*args)
}
