package org.ifmo.isbdcurs.util

import org.ifmo.isbdcurs.services.BackendException

class ExceptionHelper(private val logger: org.slf4j.Logger) {
    fun <T> wrapWithBackendException(message: String, f: () -> T): T {
        try {
            return f()
        } catch (e: BackendException) {
            logger.error(message, e)
            throw e
        } catch (e: Exception) {
            logger.error(message, e)
            throw BackendException(message, e)
        }
    }
}

