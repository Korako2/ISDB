package org.ifmo.isbdcurs.security

import org.aspectj.lang.annotation.Before
import org.ifmo.isbdcurs.controllers.BusinessController
import org.ifmo.isbdcurs.controllers.LoginController
import org.ifmo.isbdcurs.persistence.UserRepository
import org.ifmo.isbdcurs.services.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin
import org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.util.logging.Filter


@WebMvcTest(value = [BusinessController::class, LoginController::class]) // inject all controllers
@ContextConfiguration(classes = [SecurityConfig::class])
@WebAppConfiguration
@Import(BusinessController::class, LoginController::class)
class AuthTests {
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

    @MockBean
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var wac: WebApplicationContext

    private lateinit var mvc: MockMvc

    @BeforeEach
    fun setup() {
        mvc = MockMvcBuilders
            .webAppContextSetup(wac)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .build()
    }

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

    @WithMockUser
    @Test
    fun endpointWhenAnonymousThenRedirectToLogin() {
        mvc.get("/admin-page").andExpect { status { isForbidden() } }

        mvc.get("/orders?pageNumber=0&pageSize=2").andExpect { status { isForbidden() } }
    }

    @WithMockUser
    @Test
    fun loginWhenIncorrectCredentialsThenRedirectToLogin() {
        mvc.perform {
            formLogin("/login")
                .user("admin")
                .password("wrong")
                .buildRequest(it)
        }.andExpect {
            status().is3xxRedirection()
            redirectedUrl("/login?error")
        }
    }

    @WithMockUser
    @Test
    fun loginWhenCorrectCredentialsThenRedirectToIndex() {
        mvc.perform {
            formLogin("/login")
                .user("admin")
                .password("admin")
                .buildRequest(it)
        }.andExpect {
            authenticated().withRoles("USER", "ADMIN")
            status().is3xxRedirection()
            redirectedUrl("/index")
        }
    }

    @Test
    @WithMockUser(username = "user", authorities = ["ROLE_USER"])
    fun userLoginWhenCorrectCredentialsThenRedirectToIndex() {
        // TODO: fix bug with passing test
        mvc.perform {
            formLogin("/login")
                .user("user")
                .password("user")
                .buildRequest(it)
        }.andExpect {
            authenticated().withRoles("ADMIN")
            status().is3xxRedirection()
            redirectedUrl("/index")
        }
    }
}
