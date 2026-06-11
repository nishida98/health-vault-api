package com.healthvault.api.observability

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component

@Component
class ApplicationMetrics(
    private val meterRegistry: MeterRegistry,
) {
    fun userCreated() = increment("healthvault.users.created")

    fun userDeleted() = increment("healthvault.users.deleted")

    fun loginSucceeded() = increment("healthvault.auth.login", "result", "success")

    fun loginFailed() = increment("healthvault.auth.login", "result", "failure")

    fun tokenValidated() = increment("healthvault.auth.token.validated")

    fun examCreated() = increment("healthvault.medical.exams.created")

    fun examMoved() = increment("healthvault.medical.exams.moved")

    fun examDeleted() = increment("healthvault.medical.exams.deleted")

    fun examFileUploadUrlCreated() = increment("healthvault.medical.exam_files.upload_urls.created")

    fun examFileDownloadUrlCreated() = increment("healthvault.medical.exam_files.download_urls.created")

    fun examFileDeleted() = increment("healthvault.medical.exam_files.deleted")

    fun folderCreated() = increment("healthvault.exam.folders.created")

    private fun increment(name: String, vararg tags: String) {
        meterRegistry.counter(name, *tags).increment()
    }
}
