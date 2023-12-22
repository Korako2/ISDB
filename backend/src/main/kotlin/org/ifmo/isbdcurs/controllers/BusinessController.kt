package org.ifmo.isbdcurs.controllers

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.ifmo.isbdcurs.models.*
import org.ifmo.isbdcurs.persistence.CustomerRepository
import org.ifmo.isbdcurs.persistence.UserRepository
import org.ifmo.isbdcurs.services.*
import org.ifmo.isbdcurs.util.ErrorHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.ModelMap
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.mvc.support.RedirectAttributes

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
    private val logger = org.slf4j.LoggerFactory.getLogger(BusinessController::class.java)

    @GetMapping("/index")
    fun showOrdersList(request: HttpServletRequest): String {
        // redirects to appropriate page depending on user role
        if (request.isUserInRole("ROLE_ADMIN")) {
            return "redirect:/admin"
        }
        return "redirect:/customer-orders"
    }

    @GetMapping("/customer-orders")
    fun showOrdersListPage(model: ModelMap, @RequestParam(defaultValue = "0") pageNumber: Int,
                           @RequestParam(defaultValue = "10") pageSize: Int,
                           redirectAttributes: RedirectAttributes,
                            @AuthenticationPrincipal userDetails: UserDetails
    ): ModelAndView {
        if (pageNumber < 0 || pageNumber > orderService.getTotalPages(pageSize) || pageSize != 10) {
            redirectAttributes.addAttribute("pageNumber", 0)
            redirectAttributes.addAttribute("pageSize", 10)
            return ModelAndView("redirect:/customer-orders", model)
        }
        val customerId = getCustomerId(userDetails)
        model.addAttribute("orders", orderService.getOrdersByCustomerId(customerId, pageNumber, pageSize))
        model.addAttribute("currentPage", pageNumber)
        model.addAttribute("pageSize", pageSize)
        model.addAttribute("totalPages", 5)
        model.addAttribute("totalPages", orderService.getTotalPages(pageSize))
        model.addAttribute("orderDataRequest",
            OrderDataRequest(
                departureCountry = "Россия",
                departureCity = "Москва",
                departureStreet = "Ленина",
                departureHouse = 1,
                destinationCountry = "Россия",
                destinationCity = "Москва",
                destinationStreet = "Ленина",
                destinationHouse = 2,
                length = 1.0,
                width = 1.0,
                height = 1.0,
                weight = 1.0,
                cargoType = "Тип груза",
                loadingTime = "01:00",
                unloadingTime = "01:30"
            )
        )
        return ModelAndView("index", model)
    }

    @PostMapping("/add_order")
    fun addOrder(@Valid @ModelAttribute("orderDataRequest") orderDataRequest: OrderDataRequest, result: BindingResult, model: ModelMap,
                 @AuthenticationPrincipal userDetails: UserDetails): String {
        println(123)
        if (result.hasErrors())
        logger.info("Order data request: $orderDataRequest")
        if (orderService.isValidData(orderDataRequest, result) && !result.hasErrors()) {
            errorHelper.addErrorIfFailed(model) {
                val customerId = getCustomerId(userDetails)
                orderService.addOrder(customerId, orderDataRequest)
            }
        }
        return "redirect:/customer-orders?pageNumber=0&pageSize=10"
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

    @GetMapping("/add_driver")
    fun showAddDriverForm(model: Model): String {
        model.addAttribute("driverRequest", DriverRequest("Иван", "Иванов", "Иванович", "Ж", "12.12.1980", "8888333444", "1234432112344321", 800, 124, "11.11.2021", "11.11.2031", "123456789", "1234432123", "Лукойл"))
        return "add_driver"
    }
    @PostMapping("/add_driver")
    fun addDriver(@Valid @ModelAttribute("driverRequest") driverRequest: DriverRequest, result: BindingResult, model: Model): String {
        //todo Распарсить driverRequest на AddDriverRequest и на AddDriverInfoRequest
        println(123124124)
        if (driverService.isValidData(driverRequest, result) && !result.hasErrors()) {
            errorHelper.addErrorIfFailed(model) {
                //driverService.addDriver(driverRequest)
                println("Все ок")
            }
        }
        return "add_driver"
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
        logger.info("User entity: $userEntity")
        // TODO: here we assume that customer ID is the same as user ID
        return customerRepository.findById(userEntity.id).orElseThrow().id!!
    }
}