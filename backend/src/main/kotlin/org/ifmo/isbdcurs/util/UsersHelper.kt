package org.ifmo.isbdcurs.util

import org.ifmo.isbdcurs.persistence.CustomerRepository
import org.ifmo.isbdcurs.persistence.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service

@Service
class UsersHelper @Autowired constructor(
    private val userRepository: UserRepository,
    private val customerRepository: CustomerRepository,
) {
    fun getCustomerId(userDetails: UserDetails): Long {
        val userEntity = userRepository.findByUsername(userDetails.username).orElseThrow()
        return customerRepository.findById(userEntity.id).orElseThrow().id!!
    }
}