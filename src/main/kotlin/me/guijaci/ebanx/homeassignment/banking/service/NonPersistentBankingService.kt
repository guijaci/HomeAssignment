package me.guijaci.ebanx.homeassignment.banking.service

import me.guijaci.ebanx.homeassignment.banking.model.AccountDetailsDto
import me.guijaci.ebanx.homeassignment.banking.model.EventDto
import me.guijaci.ebanx.homeassignment.banking.model.EventResultDto
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException
import java.util.*
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

@Service
class NonPersistentBankingService : IBankingService {

    override fun reset() {
        throw NotImplementedError("Not implemented")
    }

    override fun getBalance(accountId: Long): Optional<Long> {
        throw NotImplementedError("Not implemented")

    }

    override fun processEvent(event: EventDto): Optional<out EventResultDto> {
        throw NotImplementedError("Not implemented")

    }

    override fun deposit(event: EventDto.Deposit): Optional<EventResultDto.DepositResult> {
        throw NotImplementedError("Not implemented")
    }

    override fun withdraw(event: EventDto.Withdraw): Optional<EventResultDto.WithdrawResult> {
        throw NotImplementedError("Not implemented")
    }

    override fun transfer(event: EventDto.Transfer): Optional<EventResultDto.TransferResult> {
        throw NotImplementedError("Not implemented")
   }
}
