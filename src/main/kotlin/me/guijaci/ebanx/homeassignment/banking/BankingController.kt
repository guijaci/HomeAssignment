package me.guijaci.ebanx.homeassignment.banking

import me.guijaci.ebanx.homeassignment.banking.model.EventDto
import me.guijaci.ebanx.homeassignment.banking.service.IBankingService
import me.guijaci.ebanx.homeassignment.response.badRequest
import me.guijaci.ebanx.homeassignment.response.created
import me.guijaci.ebanx.homeassignment.response.notFound
import me.guijaci.ebanx.homeassignment.response.ok
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import java.util.*

@Controller
class BankingController(
    @Autowired val service: IBankingService
) {

    @PostMapping("/reset")
    fun reset(): ResponseEntity<*> {
        service.reset()
        return ok()
    }

    @GetMapping("/balance")
    fun getBalance(@RequestParam("account_id") accountId: Long): ResponseEntity<*> {
        val optional: Optional<ResponseEntity<out Any>> = service.getBalance(accountId)
            .map { balance -> ok(balance) }
        return optional.orElse(notFound())
    }

    @PostMapping("/event")
    fun postEvent(@RequestBody event: EventDto): ResponseEntity<*> {
        if (event is EventDto.Unknown)
            return badRequest()
        val optional: Optional<ResponseEntity<out Any>> = service.processEvent(event)
            .map { balance -> created(balance) }
        return optional.orElse(notFound())
    }

}