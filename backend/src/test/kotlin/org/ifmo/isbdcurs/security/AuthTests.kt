package org.ifmo.isbdcurs.security

import org.ifmo.isbdcurs.controllers.BusinessController
import org.ifmo.isbdcurs.controllers.LoginController
import org.ifmo.isbdcurs.services.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get


@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [SecurityConfig::class])
@WebAppConfiguration
@WebMvcTest(value = [BusinessController::class, LoginController::class]) // inject all controllers
@Import(BusinessController::class, LoginController::class)
class AuthTests {
    @Autowired
    private lateinit var mvc: MockMvc

    @MockBean
    private lateinit var orderService: OrderService

    @MockBean
    private lateinit var customerService: CustomerService

    @MockBean
    private lateinit var driverService: DriverService

    @MockBean
    private lateinit var storagePointService: StoragePointService

    @MockBean
    private lateinit var userService: UserService

    @WithMockUser(authorities = ["ROLE_USER"])
    @Test
    fun endpointWhenUserAuthorityThenAuthorized() {
        mvc.get("/orders?pageNumber=0&pageSize=2").andExpect { status { isOk() } }

        mvc.get("/admin-page").andExpect { status { isForbidden() } }
    }

    @WithMockUser(authorities = ["ROLE_ADMIN"])
    @Test
    fun endpointWhenAdminAuthorityThenAuthorized() {
        mvc.get("/admin-page").andExpect { status { isOk() } }
    }
}
