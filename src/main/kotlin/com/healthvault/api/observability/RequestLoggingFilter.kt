package com.healthvault.api.observability

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
class RequestLoggingFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val requestId = request.getHeader(REQUEST_ID_HEADER)?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()
        val startedAt = System.nanoTime()

        MDC.put(REQUEST_ID_KEY, requestId)
        response.setHeader(REQUEST_ID_HEADER, requestId)

        try {
            filterChain.doFilter(request, response)
        } finally {
            val durationMs = (System.nanoTime() - startedAt) / NANOS_PER_MILLI
                requestLogger.info(
                "http_request method={} path={} status={} durationMs={} requestId={}",
                request.method,
                request.requestURI,
                response.status,
                durationMs,
                requestId,
            )
            MDC.remove(REQUEST_ID_KEY)
        }
    }

    private companion object {
        private const val REQUEST_ID_HEADER = "X-Request-Id"
        private const val REQUEST_ID_KEY = "requestId"
        private const val NANOS_PER_MILLI = 1_000_000
            private val requestLogger = LoggerFactory.getLogger(RequestLoggingFilter::class.java)
        }
    }
