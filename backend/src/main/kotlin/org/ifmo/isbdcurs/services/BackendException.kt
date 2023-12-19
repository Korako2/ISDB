package org.ifmo.isbdcurs.services

class BackendException : Exception {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}