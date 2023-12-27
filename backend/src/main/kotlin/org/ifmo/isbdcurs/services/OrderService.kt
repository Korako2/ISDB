package org.ifmo.isbdcurs.services

import org.ifmo.isbdcurs.internal.DriverWorker
import org.ifmo.isbdcurs.models.*
import org.ifmo.isbdcurs.persistence.*
import org.ifmo.isbdcurs.util.ExceptionHelper
import org.ifmo.isbdcurs.util.pageToIdRangeReversed
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
    private val orderStatusesRepository: OrderStatusesRepository
) {
    private val logger: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(OrderService::class.java)

    private val availableCountries = arrayOf("Россия")

    private val exceptionHelper = ExceptionHelper(logger)

    fun getOrdersPaged(page: Int, pageSize: Int): List<OrderResponse> {
        val totalPages = getTotalPages(pageSize).toInt()
        val (startOrderId, endOrderId) = pageToIdRangeReversed(totalPages, page, pageSize)
        return exceptionHelper.wrapWithBackendException("Error while getting orders") {
            orderRepo.getExtendedResults(startOrderId, endOrderId).map { it.toOrderResponse() }
        }
    }

    fun getTotalPages(pageSize: Int): Long {
        return (orderRepo.count() + pageSize - 1) / pageSize
    }

    fun getTotalPagesForManager(pageSize: Int): Long {
        return (orderStatusesRepository.countByOrderStatus() + pageSize - 1) / pageSize
    }

    fun countCustomerPages(customerId: Long, pageSize: Int): Int {
        return (orderRepo.countByCustomerId(customerId) + pageSize - 1) / pageSize
    }

    // gets order from database in reverse order. Raises exception if order not found or jpa error
    fun getOrdersByCustomerId(customerId: Long, page: Int, pageSize: Int): List<CustomerOrderResponse> {
        val offset = page * pageSize
        return exceptionHelper.wrapWithBackendException("Error while getting orders by customer id") {
            val orders = orderRepo.getExtendedResultsByCustomerId(customerId, pageSize, offset).map {
                it.toCustomerOrderResponse()
            }
            logger.info("Getting orders for customer with id = $customerId. Page = $page, pageSize = $pageSize. " +
                    "Orders size = ${orders.size}. Offset = $offset")
            orders
        }
    }

    fun getOrdersForManager(page: Int, pageSize: Int): List<ManagerOrderResponse> {
        val offset = page * pageSize
        return exceptionHelper.wrapWithBackendException("Error while getting orders by customer id") {
            val orders = orderRepo.getResultsForManager(pageSize, offset).map {
                it.toManagerOrderResponse()
            }
            logger.info("Page = $page, pageSize = $pageSize. " +
                    "Orders size = ${orders.size}. Offset = $offset")
            orders
        }
    }

    fun getFullOrderInfo(page: Int, pageSize: Int): List<FullOrderInfoResponse> {
        val offset = page * pageSize
        return exceptionHelper.wrapWithBackendException("Error while getting orders by customer id") {
            val orders = orderRepo.getFullOrdersInfoForManager(pageSize, offset).map {
                it.toFullOrderInfoResponse()
            }
            logger.info("Page = $page, pageSize = $pageSize. " +
                    "Orders size = ${orders.size}. Offset = $offset")
            orders
        }
    }
    @Transactional
    fun addOrder(customerId: Long, orderDataRequest: OrderDataRequest): AddOrderResult {
        return exceptionHelper.wrapWithBackendException("Error while adding order") {
            addOrderOrThrow(customerId, orderDataRequest)
        }
    }

    private fun addOrderOrThrow(customerId: Long, orderDataRequest: OrderDataRequest): AddOrderResult {
        val departureAddressDto = StorageAddressDto(
            country = orderDataRequest.departureCountry,
            city = orderDataRequest.departureCity,
            street = orderDataRequest.departureStreet,
            building = orderDataRequest.departureHouse,
        )
        val deliveryAddressDto = StorageAddressDto(
            country = orderDataRequest.destinationCountry,
            city = orderDataRequest.destinationCity,
            street = orderDataRequest.destinationStreet,
            building = orderDataRequest.destinationHouse,
        )
        val departureAddress: Address = getAddressOrAddNew(departureAddressDto)
        val deliveryAddress: Address = getAddressOrAddNew(deliveryAddressDto)

        val departureStoragePoint: StoragePoint = storagePointRepository.findById(departureAddress.id!!)
            .orElseThrow { BackendException("Departure storage point not found") }
        val deliveryStoragePoint: StoragePoint = storagePointRepository.findById(deliveryAddress.id!!)
            .orElseThrow { BackendException("Delivery storage point not found") }

        val orderCoordinates =
            Coordinates(departureStoragePoint.latitude.toDouble(), departureStoragePoint.longitude.toDouble())
        val deliveryCoordinates =
            Coordinates(deliveryStoragePoint.latitude.toDouble(), deliveryStoragePoint.longitude.toDouble())
        val orderDataForVehicle = OrderDataForVehicle(
            weight = orderDataRequest.weight,
            width = orderDataRequest.width,
            height = orderDataRequest.height,
            length = orderDataRequest.length,
            cargoType = orderDataRequest.cargoType,
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

        val distance = orderCoordinates.calcDistanceKm(deliveryCoordinates)

        val orderId = orderRepo.addOrder(
            customerId.toInt(),
            distance,
            vehicleId.toInt(),
            orderDataRequest.weight,
            orderDataRequest.width,
            orderDataRequest.height,
            orderDataRequest.length,
            orderDataRequest.cargoType,
            Instant.now(),
        )

        logger.debug("New Order id = $orderId")

        // TODO: take time from request
        //        val unloadingSeconds = orderDataRequest.unloadingTime * 60 * 60
        //        val loadingSeconds = orderDataRequest.loadingTime * 60 * 60
        // parse unloadingTime  to LocalTime
        // unloadinTime = "01:00"
        logger.debug("unloadingTime = ${orderDataRequest.unloadingTime}")
        val unloadingSeconds = orderDataRequest.unloadingTime.split(":").let {
            it[0].toInt() * 60 * 60 + it[1].toInt() * 60
        }
        val loadingSeconds = orderDataRequest.loadingTime.split(":").let {
            it[0].toInt() * 60 * 60 + it[1].toInt() * 60
        }

        val senderId = customerId
        val receiverId = 1L
        // create agreement
        val loadingUnloadingAgreement = LoadingUnloadingAgreement(
            orderId = orderId,
            driverId = driverId,
            unloadingTime = LocalTime.ofSecondOfDay(unloadingSeconds.toLong()),
            loadingTime = LocalTime.ofSecondOfDay(loadingSeconds.toLong()),
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
        return isValidAddresses(
            orderDataRequest, result
        ) && isValidCargoType(orderDataRequest.cargoType)
    }

    private fun isValidCountry(country: String): Boolean = country in availableCountries

    private fun rejectInvalidValue(result: BindingResult, field: String, errorCode: String, errorMessage: String) {
        result.rejectValue(field, errorCode, errorMessage)
    }

    private fun isValidAddresses(orderDataRequest: OrderDataRequest, result: BindingResult): Boolean {
        if (!isValidCountry(orderDataRequest.departureCountry)) {
            logger.warn("[OrderService] isValidAddresses: departureCountry = ${orderDataRequest.departureCountry}")
            rejectInvalidValue(result, "departureCountry", "error.departureCountry", "Страна не поддерживается")
            return false
        }

        if (!isValidCountry(orderDataRequest.destinationCountry)) {
            logger.warn("[OrderService] isValidAddresses: destinationCountry = ${orderDataRequest.destinationCountry}")
            rejectInvalidValue(result, "destinationCountry", "error.destinationCountry", "Страна не поддерживается")
            return false
        }

        if (orderDataRequest.destinationCountry != orderDataRequest.departureCountry) {
            logger.warn("[OrderService] isValidAddresses: departureCountry = ${orderDataRequest.destinationCountry}, destinationCountry = ${orderDataRequest.destinationCountry}")
            rejectInvalidValue(
                result,
                "destinationCountry",
                "error.destinationCountry",
                "Страны отправления и назначения должны совпадать"
            )
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
            status = this.status.translate(),
        )
    }

    private fun CustomerOrder.toCustomerOrderResponse(): CustomerOrderResponse {
        return CustomerOrderResponse(
            statusChangedTime = this.statusChangedTime,
            driverName = this.driverName,
            departureAddress = this.departureAddress,
            deliveryAddress = this.deliveryAddress,
            status = this.status.translate(),
        )
    }

    private fun ManagerOrder.toManagerOrderResponse(): ManagerOrderResponse {
        return ManagerOrderResponse(
            id = this.id,
            statusChangedTime = this.statusChangedTime,
            phoneNumber = this.value,
            departureAddress = this.departureAddress,
            deliveryAddress = this.deliveryAddress,
            status = this.status.translate(),
        )
    }
    private fun FullOrdersInfo.toFullOrderInfoResponse(): FullOrderInfoResponse {
        return FullOrderInfoResponse(
            id = this.id,
            statusChangedTime = this.statusChangedTime,
            phoneNumber = this.value,
            customerFirstName = this.customerFirstName,
            customerLastName = this.customerLastName,
            cargo = this.cargo,
            cargoType = this.cargo.cargoType.translate(),
            departureAddress = this.departureAddress,
            deliveryAddress = this.deliveryAddress,
            loadingTime = this.loadingTime,
            unloadingTime = this.unloadingTime,
            status = this.status.translate()
        )
    }


    private fun getAddressOrAddNew(addStoragePointRequest: StorageAddressDto): Address {
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
            addressRepository.save(newAddress)
            val newStoragePoint = StoragePoint(
                addressId = newAddress.id!!,
                latitude = (Random().nextDouble() * 2 + 44.0).toFloat(),
                longitude = (Random().nextDouble() * 2 + 44.0).toFloat()
            )
            storagePointRepository.save(newStoragePoint)
            newAddress
        }
        return address
    }
}
