package com.healthvault.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HealthVaultApiApplication

fun main(args: Array<String>) {
    runApplication<HealthVaultApiApplication>(*args)
}
