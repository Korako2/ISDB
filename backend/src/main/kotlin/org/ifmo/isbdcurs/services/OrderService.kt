package org.ifmo.isbdcurs.services

import org.ifmo.isbdcurs.internal.DriverWorker
import org.ifmo.isbdcurs.models.*
import org.ifmo.isbdcurs.persistence.*
import org.ifmo.isbdcurs.util.ExceptionHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.BindingResult
import java.time.Instant
import java.time.LocalTime
import java.util.*
import kotlin.jvm.optionals.getOrElse

@Service
class OrderService @Autowired constructor(
    private val orderRepo: OrderRepository,
    private val vehicleService: VehicleService,
    private val driverWorker: DriverWorker,
    private val vehicleOwnershipRepository: VehicleOwnershipRepository,
    private val personRepository: PersonRepository,
    private val loadingUnloadingAgreementRepository: LoadingUnloadingAgreementRepository,
    private val vehicleMovementHistoryRepository: VehicleMovementHistoryRepository,
    private val storagePointRepository: StoragePointRepository,
    private val addressRepository: AddressRepository,
) {
    private val logger: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(DriverWorker::class.java)

    private val availableCountries = arrayOf("Россия")

    private val exceptionHelper = ExceptionHelper(logger)

    fun getOrdersPaged(page: Int, size: Int): List<OrderResponse> {
        val minOrderId = page * size
        val maxOrderId = page * size + size
        return exceptionHelper.wrapWithBackendException("Error while getting orders") {
            orderRepo.getExtendedResults(minOrderId, maxOrderId).map { it.toOrderResponse() }
        }
    }

    // gets order from database. Raises exception if order not found or jpa error
    fun getOrdersByCustomerId(customerId: Long, page: Int, pageSize: Int): List<OrderResponse> {
        val minOrderId = page * pageSize
        val maxOrderId = page * pageSize + pageSize
        return exceptionHelper.wrapWithBackendException("Error while getting orders by customer id") {
            orderRepo.getExtendedResultsByCustomerId(customerId, minOrderId, maxOrderId).map { it.toOrderResponse() }
        }
    }

    @Transactional
    fun addOrder(orderDataRequest: OrderDataRequest): AddOrderResult {
        return exceptionHelper.wrapWithBackendException("Error while adding order") {
            addOrderOrThrow(orderDataRequest)
        }
    }

    private fun addOrderOrThrow(orderDataRequest: OrderDataRequest): AddOrderResult {
        val departureAddress: Address = getAddressOrAddNew(orderDataRequest.departureStoragePoint)
        val deliveryAddress: Address = getAddressOrAddNew(orderDataRequest.deliveryStoragePoint)

        val departureStoragePoint: StoragePoint = storagePointRepository.findById(departureAddress.id!!)
            .orElseThrow { BackendException("Departure storage point not found") }
        val deliveryStoragePoint: StoragePoint = storagePointRepository.findById(deliveryAddress.id!!)
            .orElseThrow { BackendException("Delivery storage point not found") }

        val orderCoordinates = Coordinates(departureStoragePoint.latitude.toDouble(), departureStoragePoint.longitude.toDouble())
        val deliveryCoordinates = Coordinates(deliveryStoragePoint.latitude.toDouble(), deliveryStoragePoint.longitude.toDouble())
        val orderParams = orderDataRequest.orderParameters
        val orderDataForVehicle = OrderDataForVehicle(
            weight = orderParams.weight,
            width = orderParams.width,
            height = orderParams.height,
            length = orderParams.length,
            // TODO: use enum in data class instead of string
            cargoType = CargoType.valueOf(orderParams.cargoType),
            latitude = orderCoordinates.latitude,
            longitude = orderCoordinates.longitude,
        )

        val vehicleId = vehicleService.findSuitableVehicle(orderDataForVehicle)
        if (vehicleId == -1L) {
            throw BackendException("No suitable vehicle found")
        }

        val vehicleCoordinates =
            vehicleMovementHistoryRepository.findByVehicleIdOrderByDateDesc(vehicleId).firstOrNull()?.let {
                Coordinates(it.latitude.toDouble(), it.longitude.toDouble())
            } ?: Coordinates(0.0, 0.0)

        logger.debug("Found nearest vehicle with id = $vehicleId")
        val driverId = vehicleOwnershipRepository.findByVehicleId(vehicleId).driverId
        val person = personRepository.findById(driverId).getOrElse { throw Exception("Driver not found") }
        val driverFullName = person.firstName + " " + person.lastName

        val driveToAddressDistance = vehicleCoordinates.calcDistanceKm(orderCoordinates)
        // TODO: get current customer id from session
        val customerId = 1L

        val distance = orderCoordinates.calcDistanceKm(deliveryCoordinates)

        val orderId = orderRepo.addOrder(
            customerId.toInt(),
            distance,
            vehicleId.toInt(),
            orderParams.weight,
            orderParams.width,
            orderParams.height,
            orderParams.length,
            orderParams.cargoType,
            Date.from(Instant.now()),
        )

        logger.debug("New Order id = $orderId")

        // TODO: take time from request
        //        val unloadingSeconds = orderDataRequest.unloadingTime * 60 * 60
        //        val loadingSeconds = orderDataRequest.loadingTime * 60 * 60
        val unloadingSeconds = 100L
        val loadingSeconds = 100L

        val senderId = customerId
        val receiverId = -1L // TODO: random number
        // create agreement
        val loadingUnloadingAgreement = LoadingUnloadingAgreement(
            orderId = orderId,
            driverId = driverId,
            unloadingTime = LocalTime.ofSecondOfDay(unloadingSeconds),
            loadingTime = LocalTime.ofSecondOfDay(loadingSeconds),
            departurePoint = departureAddress.id!!,
            deliveryPoint = deliveryAddress.id!!,
            senderId = senderId,
            receiverId = receiverId,
        )

        loadingUnloadingAgreementRepository.save(loadingUnloadingAgreement)

        println("===================== orderId = $orderId")
        val addOrderResult = AddOrderResult(
            orderId = orderId,
            averageDeliveryDate = Date.from(Instant.now().plusSeconds((driveToAddressDistance / 60).toLong())),
            driverFullName = driverFullName,
        )

        run {
            driverWorker.startWork(driverId = driverId, orderId = orderId)
        }
        return addOrderResult
    }

    fun isValidData(orderDataRequest: OrderDataRequest, result: BindingResult): Boolean {
        return isValidAddresses(orderDataRequest, result) && isValidCargoType(orderDataRequest.orderParameters.cargoType)
    }

    private fun isValidCountry(country: String): Boolean = country in availableCountries

    private fun rejectInvalidValue(result: BindingResult, field: String, errorCode: String, errorMessage: String) {
        result.rejectValue(field, errorCode, errorMessage)
    }

    private fun isValidAddresses(orderDataRequest: OrderDataRequest, result: BindingResult): Boolean {
        if (!isValidCountry(orderDataRequest.departureStoragePoint.country)) {
            logger.warn("[OrderService] isValidAddresses: departureCountry = ${orderDataRequest.departureStoragePoint.country}")
            rejectInvalidValue(result, "departureCountry", "error.departureCountry", "Страна не поддерживается")
            return false
        }

        if (!isValidCountry(orderDataRequest.deliveryStoragePoint.country)) {
            logger.warn("[OrderService] isValidAddresses: destinationCountry = ${orderDataRequest.deliveryStoragePoint.country}")
            rejectInvalidValue(result, "destinationCountry", "error.destinationCountry", "Страна не поддерживается")
            return false
        }

        if (orderDataRequest.deliveryStoragePoint.country != orderDataRequest.departureStoragePoint.country) {
            logger.warn("[OrderService] isValidAddresses: departureCountry = ${orderDataRequest.deliveryStoragePoint.country}, destinationCountry = ${orderDataRequest.deliveryStoragePoint.country}")
            rejectInvalidValue(result, "destinationCountry", "error.destinationCountry", "Страны отправления и назначения должны совпадать")
            return false
        }

        return true
    }
    private fun isValidCargoType(cargoType: String): Boolean {
        return cargoType in arrayOf("BULK", "TIPPER", "PALLETIZED")
    }

    private fun ExtendedOrder.toOrderResponse(): OrderResponse {
        return OrderResponse(
            id = this.id,
            customerName = this.customerName,
            driverName = this.driverName,
            departurePoint = this.departurePoint,
            deliveryPoint = this.deliveryPoint,
            status = this.status,
        )
    }

    private fun getAddressOrAddNew(addStoragePointRequest: StorageAddressRequest) : Address {
        val address = addressRepository.findByCountryAndCityAndStreetAndBuilding(
            addStoragePointRequest.country,
            addStoragePointRequest.city,
            addStoragePointRequest.street,
            addStoragePointRequest.building,
        ).getOrElse {
            val newAddress = Address(
                country = addStoragePointRequest.country,
                city = addStoragePointRequest.city,
                street = addStoragePointRequest.street,
                building = addStoragePointRequest.building,
                corpus = 1,
            )
            val newStoragePoint = StoragePoint(
                addressId = newAddress.id!!,
                // TODO: random coordinates
                latitude = 1.0f,
                longitude = 1.0f,
            )
            storagePointRepository.save(newStoragePoint)
            addressRepository.save(newAddress)
            newAddress
        }
        return address
    }
}
