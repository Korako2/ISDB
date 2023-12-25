package org.ifmo.isbdcurs.customer.ordering

import org.springframework.stereotype.Service

@Service
class AddressService {
    fun getAllAddresses(): List<AddressDto> {
        return listOf(
            AddressDto(1, "Moscow"),
            AddressDto(2, "New York"),
            AddressDto(3, "London"),
            AddressDto(4, "Paris"),
            AddressDto(5, "Berlin"),
            AddressDto(6, "Tokyo"),
            AddressDto(7, "Beijing"),
            AddressDto(8, "Seoul"),
            AddressDto(9, "Rome"),
            AddressDto(10, "Madrid"),
            AddressDto(11, "Barcelona"),
        )
    }
}