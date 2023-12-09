package org.ifmo.isbdcurs.controllers
import jakarta.validation.Valid
import org.ifmo.isbdcurs.models.User
import org.ifmo.isbdcurs.services.UserService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.SessionAttributes

@Controller
@SessionAttributes("loggedInUser")
class LoginController(private val userService: UserService) {

    @GetMapping("/login")
    fun showLoginForm(): String {
        return "login"
    }

    @PostMapping("/login")
    fun login(@RequestParam username: String, @RequestParam password: String, model: Model): String {
        if (userService.isValidUser(username, password)) {
            model.addAttribute("loggedInUser", username)
            return "redirect:/index"
        }
        return "redirect:/login?error"
    }

    @GetMapping("/register")
    fun showRegisterForm(model: Model): String {
        model.addAttribute("user", User("", "", "", "", false))
        return "/register"
    }

    @PostMapping("/register")
    fun register(@Valid user: User, result: BindingResult, model: Model): String {
        if (userService.isUniqueUserData(user, result) && !result.hasErrors()) {
            userService.addUser(user);
            model.addAttribute("loggedInUser", user.username)
            return "redirect:/index"
        }
        return "/register"
    }
}
