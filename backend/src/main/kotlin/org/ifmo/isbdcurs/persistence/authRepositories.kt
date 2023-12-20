package org.ifmo.isbdcurs.persistence

import org.ifmo.isbdcurs.models.User
import org.springframework.data.repository.CrudRepository
import java.util.*

interface UserRepository : CrudRepository<User, Long> {
    fun findByUsername(username: String): Optional<User>
    fun existsByUsername(username: String): Boolean
    fun existsByPhone(phone: String): Boolean
    fun existsByEmail(email: String): Boolean
}