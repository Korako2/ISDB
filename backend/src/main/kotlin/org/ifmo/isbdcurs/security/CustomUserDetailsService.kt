package org.ifmo.isbdcurs.security

import org.ifmo.isbdcurs.persistence.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CustomUserDetailsService : UserDetailsService {

    @Autowired
    private lateinit var userRepository: UserRepository

    private val logger: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(CustomUserDetailsService::class.java)

    override fun loadUserByUsername(username: String?): UserDetails {
        logger.info("loadUserByUsername: $username")
        if (username == null) {
            throw UsernameNotFoundException("Username is null")
        }
        return userRepository.findByUsername(username).orElseThrow { throw UsernameNotFoundException(username) }
    }
}