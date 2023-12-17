package org.ifmo.isbdcurs.security

import org.ifmo.isbdcurs.persistence.UserRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException


@Configuration
@EnableWebSecurity
class SecurityConfig(private val userRepository: UserRepository) {
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean fun userDetailsService(): UserDetailsService {
        return UserDetailsService { username -> userRepository.findByUsername(username) ?: throw UsernameNotFoundException(username) }
    }

    @Bean
    fun authenticationProvider(): AuthenticationProvider {
        val provider = DaoAuthenticationProvider()
        provider.setUserDetailsService(userDetailsService())
        provider.setPasswordEncoder(BCryptPasswordEncoder())
        return provider
    }

    @Bean
    fun authenticationManager(
        config: AuthenticationConfiguration
    ): AuthenticationManager {
        return config.authenticationManager
    }

    @Bean
    @Throws(Exception::class)
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeRequests {
                authorize("/admin-page", hasRole("ADMIN"))
                authorize(anyRequest, authenticated)
            }
            formLogin {
                loginPage = "/login"
                loginProcessingUrl = "/login"
                failureUrl = "/login?error"
                defaultSuccessUrl("/index", true)
            }
            logout {
                logoutUrl = "/logout"
                logoutSuccessUrl = "/login"
            }
        }
        return http.build()
    }
}