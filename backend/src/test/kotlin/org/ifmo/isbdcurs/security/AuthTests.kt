package org.ifmo.isbdcurs.security

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext


@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [SecurityConfig::class])
class AuthTests {
    @Autowired
    private lateinit var context: WebApplicationContext

    private var mvc: MockMvc? = null

    @BeforeEach
    fun setup() {
        mvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .build()
    }

    @WithMockUser(authorities = ["ROLE_USER"])
    @Test
    fun endpointWhenUserAuthorityThenAuthorized() {
        this.mvc!!.perform(MockMvcRequestBuilders.get("/index"))
            .andExpect { result -> assertEquals(200, result.response.status) }
    }
}
