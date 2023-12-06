package org.ifmo.isbdcurs.services

import org.ifmo.isbdcurs.models.AddCustomerRequest
import org.ifmo.isbdcurs.persistence.CustomerRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class CustomerService @Autowired constructor(private val customerRepo: CustomerRepository) {
    fun addCustomer(addCustomerRequest: AddCustomerRequest) : Long {
        // TODO: validation
        val customerId = customerRepo.addNewCustomer(addCustomerRequest)
        return customerId
    }
}