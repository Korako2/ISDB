package org.ifmo.isbdcurs.services

import org.ifmo.isbdcurs.models.User
import org.ifmo.isbdcurs.models.UserDto
import org.ifmo.isbdcurs.persistence.UserRepository
import org.springframework.stereotype.Service
import org.springframework.validation.BindingResult

@Service
class UserService (val userRepository: UserRepository) {
    fun isPasswordCorrect(username: String, password: String): Boolean {
        return userRepository.findByUsername(username)?.password == password
    }

    fun isUniqueUserData(user: UserDto, result: BindingResult): Boolean {
        if (userRepository.existsByUsername(user.username))  {
            result.rejectValue("username", "error.username", "Логин уже занят")
            return false
        }
        if (userRepository.existsByEmail(user.email)) {
            result.rejectValue("email", "error.email", "Email уже занят")
            return false
        }
        if (userRepository.existsByPhone(user.phone)) {
            result.rejectValue("phone", "error.phone", "Телефон уже занят")
            return false
        }
        return true
    }

    fun addUser(userDto: UserDto) {
        val user = User(
            username = userDto.username,
            password = userDto.password,
            email = userDto.email,
            phone = userDto.phone,
            isAdmin = isAdmin(userDto.username)
        )
        userRepository.save(user)
    }

    fun isAdmin(username: String): Boolean {
        return userRepository.findByUsername(username)?.isAdmin ?: false
    }
}