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

//    @GetMapping("/login")
//    fun showLoginForm(): String {
//        return "login"
//    }

    @GetMapping("/admin-page")
    fun showAdminPage(): String {
        return "admin-page"
    }

//    @PostMapping("/login")
//    fun login(@RequestParam username: String, @RequestParam password: String, model: Model): String {
//        if (userService.isPasswordCorrect(username, password)) {
//            model.addAttribute("loggedInUser", username)
//            if (userService.isAdmin(username)) return "redirect:/admin-page"
//            return "redirect:/index"
//        }
//        return "redirect:/login?error"
//    }

    @GetMapping("/register")
    fun showRegisterForm(model: Model): String {
        model.addAttribute("user", UserDto("username", "password", "happy@mail.ru", "123456578901"))
        return "/register"
    }

    @PostMapping("/register")
    fun register(@ModelAttribute("user") @Valid user: UserDto, result: BindingResult, model: ModelMap): ModelAndView  {
        if (userService.isUniqueUserData(user, result) && !result.hasErrors()) {
            userService.addUser(user)
            model.addAttribute("loggedInUser", user.username)
            return ModelAndView("redirect:/index", model)
        }
        return ModelAndView("/register")
    }
}
