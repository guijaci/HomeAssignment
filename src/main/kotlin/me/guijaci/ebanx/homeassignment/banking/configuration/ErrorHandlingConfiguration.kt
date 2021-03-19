package me.guijaci.ebanx.homeassignment.banking.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.HandlerExceptionResolver
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver
import java.util.*

@Configuration
class ErrorHandlingConfiguration {
    val simpleMappingExceptionResolver: HandlerExceptionResolver
        @Bean
        get() = SimpleMappingExceptionResolver().apply {

            val mappings = Properties()
            setExceptionMappings(mappings)
            setDefaultErrorView("error")
            setExceptionAttribute("ex")
            setWarnLogCategory("example.MvcLogger")
        }
}