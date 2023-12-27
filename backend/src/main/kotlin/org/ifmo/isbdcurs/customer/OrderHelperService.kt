package org.ifmo.isbdcurs.customer

import org.ifmo.isbdcurs.customer.data.CustomerOrderDto
import org.ifmo.isbdcurs.customer.ordering.AddressDto
import org.ifmo.isbdcurs.customer.ordering.AddressService
import org.ifmo.isbdcurs.customer.ordering.AddressesDto
import org.ifmo.isbdcurs.customer.ordering.CargoParamsDto
import org.ifmo.isbdcurs.models.*
import org.ifmo.isbdcurs.persistence.CargoRepository
import org.ifmo.isbdcurs.persistence.LoadingUnloadingAgreementRepository
import org.ifmo.isbdcurs.persistence.OrderRepository
import org.ifmo.isbdcurs.persistence.StoragePointRepository
import org.ifmo.isbdcurs.util.calculateCargoCost
import org.ifmo.isbdcurs.util.calculateDeliveryCost
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalTime

@Service
class OrderHelperService @Autowired constructor(
    private val addressService: AddressService,
    private val loadingUnloadingAgreementRepository: LoadingUnloadingAgreementRepository,
    private val cargoRepository: CargoRepository,
    private val storagePointRepository: StoragePointRepository,
    private val orderRepository: OrderRepository,
) {
    fun getDeliveryAddressByOrderId(orderId: Long): AddressDto {
        val agreement = loadingUnloadingAgreementRepository.findById(orderId).orElseThrow()
        return addressService.getAddressById(agreement.deliveryPoint)
    }

    fun getDepartureAddressByOrderId(orderId: Long): AddressDto {
        val agreement = loadingUnloadingAgreementRepository.findById(orderId).orElseThrow()
        return addressService.getAddressById(agreement.departurePoint)
    }

    fun getCargoParamsByOrderId(orderId: Long): CargoParamsDto {
        val cargo = cargoRepository.findByOrderId(orderId).orElseThrow()
        return CargoParamsDto(
            cargo.cargoType.translate(),
            cargo.weight,
            cargo.height,
            cargo.width,
            cargo.length,
        )
    }

    fun getCostByOrderId(orderId: Long): Float {
        return calculateDeliveryCost(
            calculateCargoCost(
                getCargoParamsByOrderId(orderId).type,
                getCargoParamsByOrderId(orderId).weight,
                getCargoParamsByOrderId(orderId).height,
                getCargoParamsByOrderId(orderId).width,
                getCargoParamsByOrderId(orderId).length,
            ),
            orderRepository.findById(orderId).orElseThrow().distance,
        )
    }

    fun calculateDistanceBetweenAddresses(departureId: Long, deliveryId: Long): Double {
        val departurePoint = storagePointRepository.findById(departureId).orElseThrow()
        val deliveryPoint = storagePointRepository.findById(deliveryId).orElseThrow()
        return departurePoint.toCoordinates().calcDistanceKm(deliveryPoint.toCoordinates())
    }

    fun createAgreement(customerId: Long, orderId: Long, addressesDto: AddressesDto) : LoadingUnloadingAgreement {
        // create agreement
        val loadingSeconds = 60 * 60L
        return LoadingUnloadingAgreement(
            orderId = orderId,
            driverId = null,
            unloadingTime = LocalTime.ofSecondOfDay(loadingSeconds),
            loadingTime = LocalTime.ofSecondOfDay(loadingSeconds),
            departurePoint = addressesDto.departure.id,
            deliveryPoint = addressesDto.delivery.id,
            senderId = customerId,
            receiverId = customerId,
        )
    }
}

fun StoragePoint.toCoordinates(): Coordinates {
    return Coordinates(latitude.toDouble(), longitude.toDouble())
}

fun convertCustomerOrderToDto(customerOrder: CustomerOrder): CustomerOrderDto {
    val departureAddressString = customerOrder.departureAddress.toString()
    val deliveryAddressString = customerOrder.deliveryAddress.toString()
    return CustomerOrderDto(
        id = customerOrder.id,
        statusChangedTime = customerOrder.statusChangedTime,
        driverName = customerOrder.driverName ?: "не назначен",
        departureAddress = departureAddressString,
        deliveryAddress = deliveryAddressString,
        status = customerOrder.status.translate(),

    )
}
