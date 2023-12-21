package org.ifmo.isbdcurs.services

import org.ifmo.isbdcurs.models.AddCustomerRequest
import org.ifmo.isbdcurs.persistence.CustomerRepository
import org.ifmo.isbdcurs.util.ExceptionHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class CustomerService @Autowired constructor(private val customerRepo: CustomerRepository) {
    private val logger: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(DriverService::class.java)
    private val exceptionHelper = ExceptionHelper(logger)

    fun addCustomer(addCustomerRequest: AddCustomerRequest) : Long {
        // TODO: validation
        val customerId = customerRepo.addNewCustomer(addCustomerRequest)
        return customerId
    }

    fun getTotalPages(pageSize: Int): Long {
        return (customerRepo.count() + pageSize - 1) / pageSize
    }

    fun getCustomersPaged(page: Int, size: Int) {
        val minOrderId = page * size
        val maxOrderId = page * size + size
        return exceptionHelper.wrapWithBackendException("Error while getting orders") {
            customerRepo.getExtendedCustomersPaged(minOrderId, maxOrderId)
        }
    }
}