package org.ifmo.isbdcurs.controllers

import jakarta.validation.Valid
import org.ifmo.isbdcurs.models.AddCustomerRequest
import org.ifmo.isbdcurs.models.AddNewCustomer
import org.ifmo.isbdcurs.models.UserDto
import org.ifmo.isbdcurs.services.CustomerService
import org.ifmo.isbdcurs.services.UserService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.ModelMap
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView
import java.text.SimpleDateFormat

@Controller
class LoginController(
    private val userService: UserService,
    private val customerService: CustomerService
) {
    private val logger = LoggerFactory.getLogger(LoginController::class.java)

    @GetMapping("/register")
    fun showRegisterForm(model: Model): String {
        val date = SimpleDateFormat("yyyy-MM-dd").parse("2000-01-01")
        model.addAttribute(
            "addNewCustomer",
            AddNewCustomer("Happy", "VeryHappy", "F", date, "happy", "password", "Happy@mail.ru", "88005553535", false)
        )
        return "register"
    }

    @PostMapping("/register")
    fun register(
        @ModelAttribute("addNewCustomer") @Valid addNewCustomer: AddNewCustomer,
        result: BindingResult,
        model: ModelMap
    ): ModelAndView {
        if (userService.isUniqueUserData(addNewCustomer, result) && !result.hasErrors()) {
            val userDto = UserDto(
                username = addNewCustomer.username,
                password = addNewCustomer.password,
                email = addNewCustomer.email,
                phone = addNewCustomer.phone,
            )
            val customerId = addCustomer(addNewCustomer)
            logger.info("Added customer with id $customerId")
            userService.addUser(userDto, customerId)
            return ModelAndView("redirect:/index", model)
        }
        return ModelAndView("register", model)
    }

    private fun addCustomer(customer: AddNewCustomer): Long {
        val date = customer.dateOfBirth!!
        val customerObj = AddCustomerRequest(
            firstName = customer.firstName,
            lastName = customer.lastName,
            gender = customer.gender,
            dateOfBirth = date,
            middleName = null,
            organization = null
        )
        return customerService.addCustomer(customerObj)
    }
}
