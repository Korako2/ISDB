package org.ifmo.isbdcurs.services

import org.ifmo.isbdcurs.models.AddCustomerRequest
import org.ifmo.isbdcurs.models.ContactInfo
import org.ifmo.isbdcurs.models.CustomerResponse
import org.ifmo.isbdcurs.persistence.ContactInfoRepository
import org.ifmo.isbdcurs.persistence.CustomerRepository
import org.ifmo.isbdcurs.persistence.PersonRepository
import org.ifmo.isbdcurs.util.ExceptionHelper
import org.ifmo.isbdcurs.util.pageToIdRangeNormal
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class CustomerService @Autowired constructor(
    private val customerRepo: CustomerRepository,
    private val contactInfoRepo: ContactInfoRepository,
    private val personRepository: PersonRepository
) {
    private val logger: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(DriverService::class.java)
    private val exceptionHelper = ExceptionHelper(logger)

    fun addCustomer(phone: String, addCustomerRequest: AddCustomerRequest) : Long {
        val customerId = customerRepo.addNewCustomer(addCustomerRequest)
        val personId = customerRepo.findById(customerId).get().personId
        contactInfoRepo.addContactInfo(
            personId = personId,
            phone = phone,
            email = "customer@mail.ru"
        )
        return customerId
    }

    fun getTotalPages(pageSize: Int): Long {
        return (customerRepo.count() + pageSize - 1) / pageSize
    }

    fun getCustomersPaged(page: Int, size: Int): List<CustomerResponse> {
        val (minOrderId, maxOrderId) = pageToIdRangeNormal(page, size)
        return exceptionHelper.wrapWithBackendException("Error while getting orders") {
            customerRepo.getExtendedCustomersPaged(minOrderId, maxOrderId)
        }
    }
}