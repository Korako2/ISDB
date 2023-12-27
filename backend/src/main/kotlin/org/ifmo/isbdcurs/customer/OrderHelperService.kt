package org.ifmo.isbdcurs.customer

import org.ifmo.isbdcurs.customer.data.CustomerOrderDto
import org.ifmo.isbdcurs.customer.ordering.AddressDto
import org.ifmo.isbdcurs.customer.ordering.AddressService
import org.ifmo.isbdcurs.customer.ordering.CargoParamsDto
import org.ifmo.isbdcurs.models.Coordinates
import org.ifmo.isbdcurs.models.CustomerOrder
import org.ifmo.isbdcurs.models.StoragePoint
import org.ifmo.isbdcurs.models.translate
import org.ifmo.isbdcurs.persistence.CargoRepository
import org.ifmo.isbdcurs.persistence.LoadingUnloadingAgreementRepository
import org.ifmo.isbdcurs.persistence.OrderRepository
import org.ifmo.isbdcurs.persistence.StoragePointRepository
import org.ifmo.isbdcurs.util.calculateCargoCost
import org.ifmo.isbdcurs.util.calculateDeliveryCost
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

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

}

fun StoragePoint.toCoordinates(): Coordinates {
    return Coordinates(latitude.toDouble(), longitude.toDouble())
}

fun convertCustomerOrderToDto(customerOrder: CustomerOrder): CustomerOrderDto {
    return CustomerOrderDto(
        customerOrder.id!!,
        departurePoint.toCoordinates(),
        deliveryPoint.toCoordinates(),
        customerOrder.distance,
        customerOrder.status,
        customerOrder.cost,
        customerOrder.cargoType.translate(),
        customerOrder.weight,
        customerOrder.height,
        customerOrder.width,
        customerOrder.length,
    )
}
