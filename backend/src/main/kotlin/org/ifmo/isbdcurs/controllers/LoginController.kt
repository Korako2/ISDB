package org.ifmo.isbdcurs.controllers

import jakarta.validation.Valid
import org.ifmo.isbdcurs.models.AddCustomerRequest
import org.ifmo.isbdcurs.models.User
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
import java.util.*

@Controller
class LoginController(
    private val userService: UserService,
    private val customerService: CustomerService
) {
    private val logger = LoggerFactory.getLogger(LoginController::class.java)

    @GetMapping("/register")
    fun showRegisterForm(model: Model): String {
        model.addAttribute("user", UserDto("username", "password", "happy@mail.ru", "88005553535"))
        return "register"
    }

    @PostMapping("/register")
    fun register(@ModelAttribute("user") @Valid user: UserDto, result: BindingResult, model: ModelMap): ModelAndView {
        if (userService.isUniqueUserData(user, result) && !result.hasErrors()) {
            val customerId = addCustomer(user)
            logger.info("Added customer with id $customerId")
            userService.addUser(user, customerId)
            return ModelAndView("redirect:/index", model)
        }
        return ModelAndView("register", model)
    }

    private fun addCustomer(user: UserDto): Long {
        // TODO: remove hardcode when user add form is implemented
        val customer = AddCustomerRequest(
            firstName = user.username,
            lastName = "Пушкин",
            gender = "M",
            dateOfBirth = Date.from(Calendar.getInstance().apply {
                set(Calendar.YEAR, 1981)
                set(Calendar.MONTH, 9)
                set(Calendar.DAY_OF_MONTH, 22)
            }.toInstant()),
            middleName = "Олегович",
            organization = null
        )
        return customerService.addCustomer(customer)
    }
}
