package org.ifmo.isbdcurs.controllers

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.ifmo.isbdcurs.models.*
import org.ifmo.isbdcurs.persistence.CustomerRepository
import org.ifmo.isbdcurs.persistence.UserRepository
import org.ifmo.isbdcurs.services.*
import org.ifmo.isbdcurs.util.ErrorHelper
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.util.*

@Controller
class BusinessController @Autowired constructor(
    private val orderService: OrderService,
    private val customerService: CustomerService,
    private val driverService: DriverService,
    private val storagePointService: StoragePointService,
    private val userRepository: UserRepository,
    private val customerRepository: CustomerRepository,
    adminLogService: AdminLogService,
) {
    private val errorHelper = ErrorHelper(adminLogService)

    @GetMapping("/index")
    fun showOrdersList(request: HttpServletRequest): String {
        // redirects to appropriate page depending on user role
        if (request.isUserInRole("ROLE_ADMIN")) {
            return "redirect:/admin"
        }
        return "redirect:/orders"
    }

    @GetMapping("/orders")
    fun showOrdersListPage(model: Model, @RequestParam(defaultValue = "0") pageNumber: Int,
                           @RequestParam(defaultValue = "10") pageSize: Int,
                           redirectAttributes: RedirectAttributes
    ): String {
        if (pageNumber < 0 || pageNumber > orderService.getTotalPages() || pageSize != 10) {
            redirectAttributes.addAttribute("pageNumber", 0)
            redirectAttributes.addAttribute("pageSize", 10)
            return "redirect:/orders"
        }
        model.addAttribute("orders", orderService.getOrdersPaged(pageNumber, pageSize))
        model.addAttribute("currentPage", pageNumber)
        model.addAttribute("pageSize", pageSize)
        model.addAttribute("totalPages", 5)
        //model.addAttribute("totalPages", orderService.getTotalPages) //todo
        model.addAttribute("orderDataRequest",
            OrderDataRequest(
                departureStoragePoint = StorageAddressRequest(
                    country = "Россия",
                    city = "Москва",
                    street = "Ленина",
                    building = 1,
                ),
                deliveryStoragePoint = StorageAddressRequest(
                    country = "Россия",
                    city = "Москва",
                    street = "Ленина",
                    building = 2,
                ),
                orderParameters = PhysicalParametersRequest(
                    length = 1.0,
                    width = 1.0,
                    height = 1.0,
                    weight = 1.0,
                    cargoType = "Тип груза",
                ),
                time = TimeParametersRequest(
                    // TODO: fix
                    loadingTime = Date(),
                    unloadingTime = Date()
                )
            )
        )
        return "index"
    }

    fun showCustomerOrders(
        model: Model,
        @RequestParam pageNumber: Int,
        @RequestParam pageSize: Int,
        @AuthenticationPrincipal userDetails: UserDetails
    ): String {
        errorHelper.addErrorIfFailed(model) {
            val customerId = getCustomerId(userDetails)
            val ordersPaged = orderService.getOrdersByCustomerId(customerId, pageNumber, pageSize)
            model.addAttribute("orders", ordersPaged)
        }
        return "index"
    }

    @PostMapping("/add_order")
    fun addOrder(model: Model, @Valid orderDataRequest: OrderDataRequest, result: BindingResult): String {
        if (orderService.isValidData(orderDataRequest, result) && !result.hasErrors()) {
            errorHelper.addErrorIfFailed(model) {
                orderService.addOrder(orderDataRequest)
            }
        }
        return "redirect:/orders?pageNumber=1&pageSize=10"
    }

    @PostMapping("/add_customer")
    fun addCustomer(
        @Valid @RequestBody addCustomerRequest: AddCustomerRequest,
        result: BindingResult,
        model: Model
    ): String {
        // TODO: может нам понадобится сохранять Id созданного заказчика?
        // TODO: обработка ошибок и вывод клиенту

        customerService.addCustomer(addCustomerRequest)
        return "redirect:/index"
    }

    @PostMapping("/add_driver")
    fun addDriver(@Valid @RequestBody addDriverRequest: AddDriverRequest, result: BindingResult, model: Model): String {
        driverService.addDriver(addDriverRequest)
        return "redirect:/index"
    }

    @PostMapping("/add_driver_info")
    fun addDriverInfo(
        @Valid @RequestBody addDriverInfoRequest: AddDriverInfoRequest,
        result: BindingResult,
        model: Model
    ): String {
        driverService.addDriverInfo(addDriverInfoRequest)
        return "redirect:/index"
    }

    @PostMapping("/add_storagepoint")
    fun addAddress(
        @Valid @RequestBody addAddressRequest: AddStoragePointRequest,
        result: BindingResult,
        model: Model
    ): String {
        storagePointService.addStoragePoint(addAddressRequest)
        return "redirect:/index"
    }

    private fun getCustomerId(userDetails: UserDetails): Long {
        val userEntity = userRepository.findByUsername(userDetails.username).orElseThrow()
        // TODO: here we assume that customer ID is the same as user ID
        return customerRepository.findById(userEntity.id!!).orElseThrow().id!!
    }


}