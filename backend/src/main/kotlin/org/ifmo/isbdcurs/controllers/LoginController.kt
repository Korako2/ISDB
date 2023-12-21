package org.ifmo.isbdcurs.controllers
import jakarta.validation.Valid
import org.ifmo.isbdcurs.models.UserDto
import org.ifmo.isbdcurs.services.UserService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.ModelMap
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView

@Controller
@SessionAttributes("loggedInUser")
class LoginController(private val userService: UserService) {
    @GetMapping("/register")
    fun showRegisterForm(model: Model): String {
        model.addAttribute("user", UserDto("username", "password", "happy@mail.ru", "88005553535"))
        return "/register"
    }

    @PostMapping("/register")
    fun register(@ModelAttribute("user") @Valid user: UserDto, result: BindingResult, model: ModelMap): ModelAndView  {
        if (userService.isUniqueUserData(user, result) && !result.hasErrors()) {
            userService.addUser(user)
            return ModelAndView("redirect:/index", model)
        }
        return ModelAndView("/register", model)
    }
}
