package org.ifmo.isbdcurs.security

import org.ifmo.isbdcurs.persistence.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher
import org.springframework.web.servlet.handler.HandlerMappingIntrospector


@Configuration
@EnableWebSecurity
class SecurityConfig(private val userRepository: UserRepository) {
    @Autowired
    private lateinit var customerUserDetailsService: CustomUserDetailsService
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun authenticationProvider(): AuthenticationProvider {
        val provider = DaoAuthenticationProvider()
        provider.setUserDetailsService(customerUserDetailsService)
        provider.setPasswordEncoder(BCryptPasswordEncoder())
        return provider
    }

    @Bean
    @Throws(Exception::class)
    fun filterChain(http: HttpSecurity, introspector: HandlerMappingIntrospector): SecurityFilterChain {
        val mvcMatcherBuilder = MvcRequestMatcher.Builder(introspector)
        http {
            csrf { disable() }
            authorizeHttpRequests {
                authorize(mvcMatcherBuilder.pattern("/css"), permitAll)
                authorize(mvcMatcherBuilder.pattern("/error"), permitAll)
                authorize(mvcMatcherBuilder.pattern("/login"), permitAll)
                authorize(mvcMatcherBuilder.pattern("/register"), permitAll)
                authorize(mvcMatcherBuilder.pattern("/admin-page"), hasRole("ADMIN"))
                authorize(mvcMatcherBuilder.pattern("/orders"), hasRole("USER"))

                // multiple roles USER and ADMIN
                authorize(mvcMatcherBuilder.pattern("/index"), hasAnyRole("USER", "ADMIN"))
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