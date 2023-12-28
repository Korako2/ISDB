package org.ifmo.isbdcurs

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

private val CLASSPATH_RESOURCE_LOCATIONS = arrayOf(
    "classpath:/META-INF/resources/", "classpath:/resources/",
    "classpath:/static/", "classpath:/public/"
)

@EnableWebMvc
@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        super.addResourceHandlers(registry)
        registry
            .addResourceHandler("/webjars/**")
            .addResourceLocations("/webjars/")
        if (!registry.hasMappingForPattern("/**")) {
            registry.addResourceHandler("/**").addResourceLocations(
                    *CLASSPATH_RESOURCE_LOCATIONS
                );
        }
    }

    override fun addViewControllers(registry: ViewControllerRegistry) {
        registry.addViewController("/login").setViewName("login")
        registry.addViewController("/admin").setViewName("admin")
        registry.addViewController("/customer/user-agreement").setViewName("customer/user-agreement")
        registry.addViewController("/customer/confidential-policy").setViewName("customer/confidential-policy")
        registry.addRedirectViewController("/", "/index")
        registry.addRedirectViewController("/customer", "customer/index")
    }
}