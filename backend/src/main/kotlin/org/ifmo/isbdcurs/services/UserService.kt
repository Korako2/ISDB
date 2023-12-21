package org.ifmo.isbdcurs.services

import org.ifmo.isbdcurs.models.AddNewCustomer
import org.ifmo.isbdcurs.models.User
import org.ifmo.isbdcurs.models.UserDto
import org.ifmo.isbdcurs.persistence.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.validation.BindingResult
import kotlin.jvm.optionals.getOrNull

@Service
class UserService (val userRepository: UserRepository, val passwordEncoder: PasswordEncoder) {
    private val logger = LoggerFactory.getLogger(UserService::class.java)

    fun isUniqueUserData(addNewCustomer: AddNewCustomer, result: BindingResult): Boolean {
        if (userRepository.existsByUsername(addNewCustomer.username))  {
            result.rejectValue("username", "error.username", "Логин уже занят")
            return false
        }
        if (userRepository.existsByEmail(addNewCustomer.email)) {
            result.rejectValue("email", "error.email", "Email уже занят")
            return false
        }
        if (userRepository.existsByPhone(addNewCustomer.phone)) {
            result.rejectValue("phone", "error.phone", "Телефон уже занят")
            return false
        }
        return true
    }

    fun addUser(userDto: UserDto, userId: Long): User {
        val password = passwordEncoder.encode(userDto.password)
        val user = User(
            id = userId,
            username = userDto.username,
            password = password,
            email = userDto.email,
            phone = userDto.phone,
            isAdmin = isAdmin(userDto.username)
        )
        logger.info("Adding user: $user")
        return userRepository.save(user)
    }

    fun isAdmin(username: String): Boolean {
        return userRepository.findByUsername(username).getOrNull()?.isAdmin ?: false
    }
}
