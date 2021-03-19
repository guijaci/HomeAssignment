package me.guijaci.ebanx.homeassignment.banking.service

import me.guijaci.ebanx.homeassignment.banking.model.EventDto
import me.guijaci.ebanx.homeassignment.banking.model.EventResultDto
import java.util.*

interface IBankingService {

    fun reset()

    fun getBalance(accountId: Long): Optional<Long>

    fun processEvent(event: EventDto): Optional<String>

    fun deposit(event: EventDto.Deposit): Optional<String>

    fun withdraw(event: EventDto.Withdraw): Optional<String>

    fun transfer(event: EventDto.Transfer): Optional<String>

}