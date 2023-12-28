package org.ifmo.isbdcurs.customer.ordering

import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import org.ifmo.isbdcurs.customer.CustomerController
import org.ifmo.isbdcurs.models.CargoType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.SessionAttributes
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import org.springframework.web.servlet.view.RedirectView


data class CargoParamsDto(
    val type: CargoType,
    @field:DecimalMin(value = "0.5", message = "Вес должен быть не менее 0.5")
    @field:DecimalMax(value = "150", message = "Вес должен быть не более 150")
    var weight: Float,
    @field:DecimalMin(value = "0.5", message = "Высота должна быть не менее 0.5")
    @field:DecimalMax(value = "4", message = "Высота должна быть не более 4")
    var height: Float,
    @field:DecimalMin(value = "0.5", message = "Ширина должна быть не менее 0.5")
    @field:DecimalMax(value = "2.5", message = "Ширина должна быть не более 2.5")
    var width: Float,
    @field:DecimalMin(value = "1", message = "Длина должна быть не менее 1")
    @field:DecimalMax(value = "15", message = "Длина должна быть не более 15")
    var length: Float,
)

data class AddressesDto(
    var departure: AddressDto = AddressDto(-1, ""),
    var delivery: AddressDto = AddressDto(-1, ""),
)

data class AddressesDtoRequest(
    var departure: Long = -1,
    var delivery: Long = -1,
)

data class OrderUserInput(
    var id: Long = -1,
    val addressesDto: AddressesDto,
    val cargo: CargoParamsDto,
    val cost: Float,
)

// 1. fill in departure and delivery address
// 1.1 return all addresses in list
// 1.2 return addresses by prefix letters filter
// 2. submit departure and delivery address
@SessionAttributes("selectedAddresses", "selectedAddressesInput", "allAddresses", "cargoParams", "cost")
@Controller
class OrderingController @Autowired constructor(
    val addressService: AddressService,
    val costsService: CostsService,
    private val orderService: CustomerOrderService
) {
    private val logger = org.slf4j.LoggerFactory.getLogger(CustomerController::class.java)

    // invoked once
    @ModelAttribute("selectedAddresses")
    fun selectedAddresses(model: Model): AddressesDto {
        logger.info("[selectedAddresses] called defaults")
        val delivery = addressService.getAddressById(1)
        val departure = addressService.getAddressById(2)
        return AddressesDto(departure, delivery)
    }

    @ModelAttribute("selectedAddressesInput")
    fun selectedAddressesInput(model: Model): AddressesDtoRequest {
        logger.info("[selectedAddressesInput] called defaults")
        return AddressesDtoRequest(1, 2)
    }

    @ModelAttribute("allAddresses")
    fun allAddresses(model: Model): List<AddressDto> {
        logger.info("[allAddresses] called defaults")
        return addressService.getAllAddresses()
    }

    @ModelAttribute("cargoParams")
    fun cargoParams(model: Model): CargoParamsDto {
        logger.info("[cargoParams] called defaults")
        return CargoParamsDto(CargoType.BULK, 1F, 1.5F, 0.7F, 9F)
    }

    @ModelAttribute("cost")
    fun cost(model: Model): Float {
        logger.info("[cost] called defaults")
        return 0.0f
    }

    @GetMapping("customer/addressForm")
    fun showAddressList(model: Model): String {
        return "customer/ordering/address_form"
    }

    @PostMapping("customer/addressForm")
    fun submitAddressForm(
        @ModelAttribute("selectedAddressesInput") selectedAddressesInput: AddressesDtoRequest,
        result: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        val selectedAddresses = AddressesDto(
            addressService.getAddressById(selectedAddressesInput.departure),
            addressService.getAddressById(selectedAddressesInput.delivery),
        )
        if (selectedAddresses.delivery.id == selectedAddresses.departure.id) {
            result.rejectValue("delivery", "delivery", "Адреса отправления и получения должны быть разными")
            result.rejectValue("departure", "departure", "Адреса отправления и получения должны быть разными")
        }
        if (result.hasErrors()) {
            logger.info("[submitAddressForm] errors: ${result.allErrors}")
            return "customer/ordering/address_form"
        }

        model.addAttribute("selectedAddresses", selectedAddresses)
        logger.info("[submitAddressForm] addresses: $selectedAddresses")
        // very important to use RedirectView, otherwise attributes will be lost
        redirectAttributes.addFlashAttribute("selectedAddresses", selectedAddresses)
        return "redirect:/customer/cargoForm"
    }

    @GetMapping("customer/cargoForm")
    fun showCargoForm(model: Model): String {
        return "customer/ordering/cargo_form"
    }

    @PostMapping("customer/cargoForm")
    fun submitCargoForm(
        @Valid @ModelAttribute("cargoParams") cargoParams: CargoParamsDto,
        result: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes
    ): ModelAndView {
        if (result.hasErrors()) {
            logger.info("[submitCargoForm] errors: ${result.allErrors}")
            return ModelAndView("/customer/ordering/cargo_form")
        }
        val costs = costsService.calculatePrice(cargoParams)
        model.addAttribute("cargoParams", cargoParams)
        model.addAttribute("cost", costs)
        redirectAttributes.addFlashAttribute("cargoParams", cargoParams)
        redirectAttributes.addFlashAttribute("cost", costs)
        return ModelAndView(RedirectView("/customer/submitOrder"))
    }

    @GetMapping("customer/submitOrder")
    fun submitOrder(model: Model): String {
        return "customer/ordering/submit_order"
    }

    @PostMapping("customer/submitOrder")
    fun submitOrder(
        @Valid @ModelAttribute("selectedAddresses") selectedAddresses: AddressesDto,
        @Valid @ModelAttribute("cargoParams") cargoParams: CargoParamsDto,
        @Valid @ModelAttribute("cost") cost: Float,
        model: Model,
        redirectAttributes: RedirectAttributes
    ): RedirectView {
        // TODO: remove hardcode
        val order = OrderUserInput(-1, selectedAddresses, cargoParams, cost)
        orderService.createOrder(order)

        logger.info("[submitOrder] order: $order")
        return RedirectView("/customer/index")
    }
}