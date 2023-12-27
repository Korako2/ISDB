package org.ifmo.isbdcurs.customer

import org.ifmo.isbdcurs.customer.ordering.AddressDto
import org.ifmo.isbdcurs.customer.ordering.AddressService
import org.ifmo.isbdcurs.persistence.LoadingUnloadingAgreementRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class OrderJoinerHelperService @Autowired constructor(
    private val addressService: AddressService,
    private val loadingUnloadingAgreementRepository: LoadingUnloadingAgreementRepository,
) {
    fun getDeliveryAddressByOrderId(orderId: Long): AddressDto {
        val agreement = loadingUnloadingAgreementRepository.findById(orderId).orElseThrow()
        return addressService.getAddressById(orderId)
    }
}