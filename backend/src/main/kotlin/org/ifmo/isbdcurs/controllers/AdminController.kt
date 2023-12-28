import org.ifmo.isbdcurs.services.CustomerService
import org.ifmo.isbdcurs.services.DriverService
import org.ifmo.isbdcurs.services.OrderService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class AdminController(
    private val orderService: OrderService,
    private val driverService: DriverService,
    private val customerService: CustomerService,
    ) {
    private val logger = org.slf4j.LoggerFactory.getLogger(AdminController::class.java)

    @GetMapping("/admin")
    fun showLogs(model: Model, @RequestParam(defaultValue = "0") pageNumber: Int,
                 @RequestParam(defaultValue = "10") pageSize: Int,
                 redirectAttributes: RedirectAttributes
    ): String {
        if (pageNumber < 0 || pageNumber > orderService.getTotalPagesForManager(pageSize) || pageSize != 10) {
            redirectAttributes.addAttribute("pageNumber", 0)
            redirectAttributes.addAttribute("pageSize", 10)
            return "redirect:/admin"
        }
        model.addAttribute("orders", orderService.getOrdersForManager(pageNumber, pageSize))
        model.addAttribute("currentPage", pageNumber)
        model.addAttribute("pageSize", pageSize)
        model.addAttribute("totalPages", orderService.getTotalPagesForManager(pageSize).toInt())
        model.addAttribute("ords", orderService.getFullOrdersInfo(pageNumber, pageSize))
        return "admin"
    }

    @GetMapping("/admin/orders")
    fun showOrdersListPage(model: Model, @RequestParam(defaultValue = "0") pageNumber: Int,
                           @RequestParam(defaultValue = "10") pageSize: Int,
                           redirectAttributes: RedirectAttributes
    ): String {
        if (pageNumber < 0 || pageNumber > orderService.getTotalPages(pageSize) || pageSize != 10) {
            redirectAttributes.addAttribute("pageNumber", 0)
            redirectAttributes.addAttribute("pageSize", 10)
            return "redirect:/admin/orders"
        }
        model.addAttribute("orders", orderService.getOrdersPaged(pageNumber, pageSize))
        model.addAttribute("currentPage", pageNumber)
        model.addAttribute("pageSize", pageSize)
        model.addAttribute("totalPages", 5)
        model.addAttribute("totalPages", orderService.getTotalPages(pageSize))
        return "tables/orders"
    }

    @GetMapping("/admin/drivers")
    fun showDriversListPage(model: ModelMap, @RequestParam(defaultValue = "0") pageNumber: Int,
                            @RequestParam(defaultValue = "10") pageSize: Int,
                            redirectAttributes: RedirectAttributes
    ): ModelAndView {
        if (pageNumber < 0 || pageNumber > driverService.getTotalPages(pageSize) || pageSize != 10) {
            redirectAttributes.addAttribute("pageNumber", 0)
            redirectAttributes.addAttribute("pageSize", 10)
            logger.info("Redirecting to /admin/drivers")
            return ModelAndView("redirect:/admin/drivers", model)
        }
        model.addAttribute("drivers", driverService.getDriversPaged(pageNumber, pageSize))
        model.addAttribute("currentPage", pageNumber)
        model.addAttribute("pageSize", pageSize)
        model.addAttribute("totalPages", 5)
        model.addAttribute("totalPages", driverService.getTotalPages(pageSize))
        return ModelAndView("tables/drivers", model)
    }

    @GetMapping("/admin/customers")
    fun showCustomersListPage(model: Model, @RequestParam(defaultValue = "0") pageNumber: Int,
                            @RequestParam(defaultValue = "10") pageSize: Int,
                            redirectAttributes: RedirectAttributes
    ): String {
        if (pageNumber < 0 || pageNumber > customerService.getTotalPages(pageSize) || pageSize != 10) {
            redirectAttributes.addAttribute("pageNumber", 0)
            redirectAttributes.addAttribute("pageSize", 10)
            return "redirect:/admin/customers"
        }
        model.addAttribute("customers", customerService.getCustomersPaged(pageNumber, pageSize))
        model.addAttribute("currentPage", pageNumber)
        model.addAttribute("pageSize", pageSize)
        model.addAttribute("totalPages", 5)
        model.addAttribute("totalPages", customerService.getTotalPages(pageSize))
        return "tables/customers"
    }

    @GetMapping("/admin/find_suitable_driver")
    fun showFindSuitableDriverPage(model: Model, @RequestParam orderId: Long): String {
        model.addAttribute("orderById", orderService.getFullOrderInfoById(orderId))
        return "find_suitable_driver"
    }

    @GetMapping("/admin/suitable_driver")
    fun showFindSuitableDriver(model: Model, @RequestParam orderId: Long): String {
        val driverId = orderService.findSuitableDriverAndUpdateOrder(orderId)
        model.addAttribute("orderById", orderService.getFullOrderInfoById(orderId))
        model.addAttribute("driver", driverService.getSuitableDriverResponseByDriverId(driverId))
        return "suitable_driver"
    }
}