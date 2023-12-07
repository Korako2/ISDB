package org.ifmo.isbdcurs.controllers
import org.ifmo.isbdcurs.services.UserService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
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
    fun showRegisterForm(): String {
        return "/register"
    }

    @PostMapping("/register")
    fun register(@RequestParam username: String, @RequestParam password: String, model: Model): String {
        if (userService.isValidUser(username, password)) {
            model.addAttribute("loggedInUser", username)
            return "redirect:/index"
        }
        return "redirect:/register?error"
    }
}
