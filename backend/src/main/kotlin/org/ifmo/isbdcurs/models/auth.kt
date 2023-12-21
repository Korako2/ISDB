package org.ifmo.isbdcurs.models

import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

@Entity
@Table(name = "users")
data class User(
    @Id val id: Long,
    @get:JvmName("getUsername1")
    val username: String,
    @get:JvmName("getPassword1")
    val password: String,
    val email: String,
    val phone: String,
    val isAdmin: Boolean
) : UserDetails {
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return if (isAdmin) mutableListOf(GrantedAuthority { "ROLE_ADMIN" }) else mutableListOf(GrantedAuthority { "ROLE_USER" })
    }

    override fun getPassword(): String {
        return password
    }

    override fun getUsername(): String {
        return username
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }
}
