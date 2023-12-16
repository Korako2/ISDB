package org.ifmo.isbdcurs.security

import org.ifmo.isbdcurs.models.User
import org.ifmo.isbdcurs.persistence.UserRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.web.servlet.config.annotation.EnableWebMvc

@Configuration
@EnableWebSecurity
class SecurityConfig {
    @Bean
    fun userDetailsService(passwordEncoder: PasswordEncoder, userRepo: UserRepository): InMemoryUserDetailsManager {
        val customUser = User(
            username = "admin",
            password = passwordEncoder.encode("admin"),
            email = "admin@admin.ru",
            isAdmin = true,
            phone = "12345678901"
        )
        userRepo.save(customUser)
        return InMemoryUserDetailsManager(customUser)
    }

    @Bean
    @Throws(Exception::class)
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http.authorizeHttpRequests {
            it.requestMatchers(AntPathRequestMatcher.antMatcher("/admin-page")).hasRole("ADMIN")
            it.anyRequest().authenticated()
        }.formLogin {
            it.loginPage("/login")
            it.permitAll()
        }.build()
    }
}