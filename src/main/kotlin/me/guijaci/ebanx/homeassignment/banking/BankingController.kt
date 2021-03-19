package me.guijaci.ebanx.homeassignment.banking

import me.guijaci.ebanx.homeassignment.banking.model.EventDto
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import sun.reflect.generics.reflectiveObjects.NotImplementedException

@Controller
class BankingController {

    @PostMapping("/reset")
    fun reset(): ResponseEntity<*>{
        throw NotImplementedException()
    }

    @GetMapping("/balance")
    fun getBalance(@RequestParam("account_id") accountId: Long): ResponseEntity<*>{
        throw NotImplementedException()
    }

    @PostMapping("/event")
    fun postEvent(@RequestBody event: EventDto): ResponseEntity<*>{
        throw NotImplementedException()
    }

}