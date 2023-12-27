package org.ifmo.isbdcurs.customer.ordering

import jakarta.validation.Valid
import org.ifmo.isbdcurs.customer.CustomerController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.SessionAttributes
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import org.springframework.web.servlet.view.RedirectView


data class CargoParamsDto(
    val type: String,
    val weight: Float,
    val height: Float,
    val width: Float,
    val length: Float,
)

data class AddressesDto(
    var departure: AddressDto = AddressDto(-1, ""),
    var delivery: AddressDto = AddressDto(-1, ""),
)

data class OrderDetails(
    var id: Long = -1,
    val addressesDto: AddressesDto,
    val cargo: CargoParamsDto,
    val cost: Float,
)

// 1. fill in departure and delivery address
// 1.1 return all addresses in list
// 1.2 return addresses by prefix letters filter
// 2. submit departure and delivery address
@SessionAttributes("selectedAddresses", "cargoParams", "cost")
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
        return AddressesDto(AddressDto(1, "Moscow"), AddressDto(2, "New York"))
    }

    @ModelAttribute("cargoParams")
    fun cargoParams(model: Model): CargoParamsDto {
        logger.info("[cargoParams] called defaults")
        return CargoParamsDto("OPEN", 1.0f, 1.0f, 1.0f, 1.0f)
    }

    @ModelAttribute("cost")
    fun cost(model: Model): Float {
        logger.info("[cost] called defaults")
        return 0.0f
    }

    @GetMapping("customer/addressForm")
    fun showAddressList(model: Model): String {
        val addresses = addressService.getAllAddresses()
        model.addAttribute("allAddresses", addresses)
        return "customer/ordering/address_form"
    }

    @PostMapping("customer/submitAddressForm")
    fun submitAddressForm(
        @RequestParam("delivery") deliveryId: Long,
        @RequestParam("departure") departureId: Long,
        model: Model,
        redirectAttributes: RedirectAttributes
    ): RedirectView {
        val delivery = addressService.getAddressById(deliveryId)
        val departure = addressService.getAddressById(departureId)
        val selectedAddresses = AddressesDto(departure, delivery)
        model.addAttribute("selectedAddresses", selectedAddresses)
        logger.info("[submitAddressForm] addresses: $selectedAddresses")
        // very important to use RedirectView, otherwise attributes will be lost
        redirectAttributes.addFlashAttribute("selectedAddresses", selectedAddresses)
        return RedirectView("/customer/cargoForm")
    }

    @GetMapping("customer/cargoForm")
    fun showCargoForm(model: Model): String {
        return "customer/ordering/cargo_form"
    }

    @PostMapping("customer/submitCargoForm")
    fun submitCargoForm(
        @Valid cargoParams: CargoParamsDto,
        model: Model,
        redirectAttributes: RedirectAttributes
    ): RedirectView {
        val costs = costsService.calculatePrice(cargoParams)
        model.addAttribute("cargoParams", cargoParams)
        model.addAttribute("cost", costs)
        redirectAttributes.addFlashAttribute("cargoParams", cargoParams)
        redirectAttributes.addFlashAttribute("cost", costs)
        return RedirectView("/customer/submitOrder")
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
        val order = OrderDetails(-1, selectedAddresses, cargoParams, cost)
        orderService.createOrder(order)

        logger.info("[submitOrder] order: $order")
        return RedirectView("/customer/index")
    }
}