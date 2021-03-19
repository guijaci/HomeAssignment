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

    private val repo: MutableMap<Long, Long> = mutableMapOf()
    private val repoLock: ReadWriteLock by lazy { ReentrantReadWriteLock() }

    override fun reset() {
        lockWriting()
        repo.clear()
        unlockWriting()
    }

    override fun getBalance(accountId: Long): Optional<Long> {
        lockReading()
        val balance = repo[accountId]
        unlockReading()
        return Optional.ofNullable(balance)
    }

    override fun processEvent(event: EventDto): Optional<out EventResultDto> {
        return when (event) {
            is EventDto.Deposit -> deposit(event)
            is EventDto.Withdraw -> withdraw(event)
            is EventDto.Transfer -> transfer(event)
            else -> throw IllegalArgumentException("Illegal Event")
        }
    }

    override fun deposit(event: EventDto.Deposit): Optional<EventResultDto.DepositResult> {
        val (destination, amount) = event
        lockWriting()
        val balanceBefore = repo[destination] ?: 0
        val balanceAfter = balanceBefore + amount
        repo[destination] = balanceAfter
        unlockWriting()
        val destinationAccount = AccountDetailsDto(destination, balanceAfter)
        val depositResult = EventResultDto.DepositResult(destinationAccount)
        return Optional.of(depositResult)
    }

    override fun withdraw(event: EventDto.Withdraw): Optional<EventResultDto.WithdrawResult> {
        val (origin, amount) = event
        lockWriting()
        val balanceBefore = repo[origin] ?: return unlockWriting().run {
            Optional.empty<EventResultDto.WithdrawResult>()
        }
        val balanceAfter = balanceBefore - amount
        repo[amount] = balanceAfter
        unlockWriting()
        val originAccount = AccountDetailsDto(origin, balanceAfter)
        val withdrawResult = EventResultDto.WithdrawResult(originAccount)
        return Optional.of(withdrawResult)
    }

    override fun transfer(event: EventDto.Transfer): Optional<EventResultDto.TransferResult> {
        val (origin, destination, amount) = event
        lockWriting()
        val originBalanceBefore = repo[origin] ?: return unlockWriting().run {
            Optional.empty<EventResultDto.TransferResult>()
        }
        val destinationBalanceBefore = repo[destination] ?: 0
        val originBalanceAfter = originBalanceBefore - amount
        val destinationBalanceAfter = destinationBalanceBefore + amount
        repo[origin] = originBalanceAfter
        repo[destination] = destinationBalanceAfter
        unlockWriting()
        val originAccount = AccountDetailsDto(origin, originBalanceAfter)
        val destinationAccount = AccountDetailsDto(destination, destinationBalanceAfter)
        val transferResult = EventResultDto.TransferResult(originAccount, destinationAccount)
        return Optional.of(transferResult)
    }

    private fun lockReading() {
        repoLock.readLock().lock()
    }

    private fun unlockReading() {
        repoLock.readLock().unlock()
    }

    private fun lockWriting() {
        repoLock.writeLock().lock()
    }

    private fun unlockWriting() {
        repoLock.writeLock().unlock()
    }
}
