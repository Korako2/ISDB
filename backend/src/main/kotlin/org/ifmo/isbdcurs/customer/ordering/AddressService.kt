package org.ifmo.isbdcurs.customer.ordering

import org.ifmo.isbdcurs.models.Address
import org.ifmo.isbdcurs.persistence.AddressRepository
import org.ifmo.isbdcurs.services.BackendException
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
class AddressService(private val addressRepository: AddressRepository) {
    fun getAllAddresses(): List<AddressDto> {
        return addressRepository.findAll().map { it.mapAddressToDto() }
    }

    fun getAddressById(id: Long): AddressDto {
        return addressRepository.findById(id).getOrNull()?.mapAddressToDto() ?: throw BackendException("Address not found")
    }

    private fun Address.mapAddressToDto(): AddressDto {
        val address = this
        val name = "${address.country}, ${address.city}, ${address.street}, ${address.building}"
        return AddressDto(address.id!!, name)
    }
}