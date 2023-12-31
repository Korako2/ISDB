package org.ifmo.isbdcurs.security

import org.ifmo.isbdcurs.persistence.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.access.hierarchicalroles.RoleHierarchy
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher
import org.springframework.web.servlet.handler.HandlerMappingIntrospector


@Configuration
@EnableWebSecurity
class SecurityConfig(private val userRepository: UserRepository) {
    @Autowired
    private lateinit var customerUserDetailsService: CustomUserDetailsService
    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Bean
    fun authenticationProvider(): AuthenticationProvider {
        val provider = DaoAuthenticationProvider()
        provider.setUserDetailsService(customerUserDetailsService)
        provider.setPasswordEncoder(passwordEncoder)
        return provider
    }

    @Bean
    fun roleHierarchy(): RoleHierarchy {
        val roleHierarchy = RoleHierarchyImpl()
        val hierarchy = "ROLE_ADMIN > ROLE_USER"
        roleHierarchy.setHierarchy(hierarchy)
        return roleHierarchy
    }

    @Bean
    @Throws(Exception::class)
    fun filterChain(http: HttpSecurity, introspector: HandlerMappingIntrospector): SecurityFilterChain {
        val mvcMatcherBuilder = MvcRequestMatcher.Builder(introspector)
        http {
            csrf { disable() }
            authorizeHttpRequests {
                authorize(mvcMatcherBuilder.pattern("/error"), permitAll)
                authorize(mvcMatcherBuilder.pattern("/login"), permitAll)
                authorize(mvcMatcherBuilder.pattern("/logout"), permitAll)
                authorize(mvcMatcherBuilder.pattern("/register"), permitAll)
                authorize(mvcMatcherBuilder.pattern("/favicon.ico"), permitAll)
                authorize(mvcMatcherBuilder.pattern("/webjars/**"), permitAll)
                authorize(mvcMatcherBuilder.pattern("/static/**"), permitAll)
                authorize(mvcMatcherBuilder.pattern("/css/**"), permitAll)
                authorize(mvcMatcherBuilder.pattern("/js/**"), permitAll)
                authorize(mvcMatcherBuilder.pattern("/manager"), hasRole("ADMIN"))
                authorize(mvcMatcherBuilder.pattern("/manager/**"), hasRole("ADMIN"))
                authorize(mvcMatcherBuilder.pattern("/logs"), hasRole("ADMIN"))
                authorize(mvcMatcherBuilder.pattern("/customer/**"), hasAnyRole("USER", "ADMIN"))
                authorize(mvcMatcherBuilder.pattern("/add_order"), hasAnyRole("USER", "ADMIN"))
                authorize(mvcMatcherBuilder.pattern("/add_test"), hasAnyRole("USER"))
                authorize(mvcMatcherBuilder.pattern("/ws/**"), hasAnyRole("USER", "ADMIN"))

                authorize(mvcMatcherBuilder.pattern("/add_driver"), permitAll)

                // multiple roles USER and ADMIN
                authorize(mvcMatcherBuilder.pattern("/index"), hasAnyRole("USER", "ADMIN"))
                authorize(mvcMatcherBuilder.pattern("/"), hasAnyRole("USER", "ADMIN"))
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