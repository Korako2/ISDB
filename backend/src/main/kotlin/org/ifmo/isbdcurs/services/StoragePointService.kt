package org.ifmo.isbdcurs.services

import org.ifmo.isbdcurs.models.AddStoragePointRequest
import org.ifmo.isbdcurs.models.Address
import org.ifmo.isbdcurs.models.StoragePoint
import org.ifmo.isbdcurs.persistence.AddressRepository
import org.ifmo.isbdcurs.persistence.StoragePointRepository
import org.springframework.stereotype.Service

@Service
class StoragePointService(
    private val storagePointRepository: StoragePointRepository,
    private val addressRepository: AddressRepository
) {
    fun addStoragePoint(addStoragePointRequest: AddStoragePointRequest) {
        val newAddress = Address(
            country = "Thai",
            city = addStoragePointRequest.city,
            street = addStoragePointRequest.street,
            building = addStoragePointRequest.building,
            corpus = addStoragePointRequest.corpus,
        )
        addressRepository.save(newAddress)

        val storagePoint = StoragePoint(
            addressId = newAddress.id!!,
            latitude = addStoragePointRequest.latitude.toFloat(),
            longitude = addStoragePointRequest.longitude.toFloat(),
        )
        storagePointRepository.save(storagePoint)
    }

    fun getAll(): List<StoragePoint> = storagePointRepository.findAll().toList()

    fun getById(id: Long): StoragePoint = storagePointRepository.findById(id).get()
}