package org.ifmo.isbdcurs.services

import org.ifmo.isbdcurs.models.User
import org.springframework.stereotype.Service
import org.springframework.validation.BindingResult

@Service
class UserService {
    private var users = listOf(
        User("user1", "password1", "email1@gmail.com", "88005553535", false),
        User("user2", "password2", "email2@gmail.com", "89506096906", true),
    )

    fun getUserByUsername(username: String): User? {
        return users.find { it.username == username }
    }

    fun isValidUser(username: String, password: String): Boolean {
        val user = getUserByUsername(username)
        return user?.password == password
    }

    fun isUniqueUserData(user: User, result: BindingResult): Boolean {
        val userWithSameUsername = users.any { it.username == user.username }
        if (userWithSameUsername) result.rejectValue("username", "error.username", "Логин уже занят")
        val userWithSameEmail = users.any { it.email == user.email }
        if (userWithSameEmail) result.rejectValue("email", "error.email", "Email уже занят")
        val userWithSamePhone = users.any { it.phone == user.phone }
        if (userWithSamePhone) result.rejectValue("phone", "error.phone", "Телефон уже занят")
        return !(userWithSameUsername || userWithSameEmail || userWithSamePhone)
    }

    fun addUser(user: User) {
        users.toMutableList().add(user)
    }

}
