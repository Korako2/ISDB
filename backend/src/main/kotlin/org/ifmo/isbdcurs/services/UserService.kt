package org.ifmo.isbdcurs.services
import org.ifmo.isbdcurs.models.User
import org.springframework.stereotype.Service

@Service
class UserService {
    private val users = listOf(
        User("user1", "password1"),
        User("user2", "password2")
    )

    fun getUserByUsername(username: String): User? {
        return users.find { it.username == username }
    }

    fun isValidUser(username: String, password: String): Boolean {
        val user = getUserByUsername(username)
        return user?.password == password
    }
}
