package me.guijaci.ebanx.homeassignment.banking.service

import me.guijaci.ebanx.homeassignment.banking.model.EventDto
import me.guijaci.ebanx.homeassignment.banking.model.EventResultDto
import org.springframework.stereotype.Service
import java.util.*

@Service
interface IBankingService {

    fun reset()

    fun getBalance(accountId: Long): Optional<Long>

    fun processEvent(event: EventDto): Optional<EventResultDto>

}