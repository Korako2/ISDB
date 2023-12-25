package org.ifmo.isbdcurs.customer.ordering

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping


const val BASE_PATH = "customer"

// thymleaf
// 1. fill in departure and delivery address
// 1.1 return all addresses in list
// 1.2 return addresses by prefix letters filter
// 2. submit departure and delivery address
@Controller()
class CustomerController @Autowired constructor(val addressService: AddressService) {
    private val logger = org.slf4j.LoggerFactory.getLogger(CustomerController::class.java)

    @GetMapping("customer/addressForm")
    fun showAddressList(model: Model): String {
        val addresses = addressService.getAllAddresses()
        model.addAttribute("allAddresses", addresses)
        model.addAttribute("selectedDepartureAddress", AddressDto(1, "Moscow"))
        model.addAttribute("selectedDeliveryAddress", AddressDto(2, "New York"))
        return "customer/ordering/address_form"
    }

//    @PostMapping("customer/submitAddressForm")
//    fun submitAddressForm(@ModelAttribute): String {
//
//        logger.info("[submitAddressForm] selectedDepartureAddress: $selectedDepartureAddress, selectedDeliveryAddress: $selectedDeliveryAddress")
//        return "redirect:/customer/ordering"
//    }

    @PostMapping("customer/submitAddressForm")
    fun submitAddressForm(): String {

        // You can perform additional processing or redirect to another page as needed

        return "redirect:/addressForm" // Redirect back to the form page
    }
}