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

    override fun processEvent(event: EventDto): Optional<String> {
        return when (event) {
            is EventDto.Deposit -> deposit(event)
            is EventDto.Withdraw -> withdraw(event)
            is EventDto.Transfer -> transfer(event)
            else -> throw IllegalArgumentException("Illegal Event")
        }
    }

    override fun deposit(event: EventDto.Deposit): Optional<String> {
        val (destination, amount) = event
        lockWriting()
        val balanceBefore = repo[destination] ?: 0
        val balanceAfter = balanceBefore + amount
        repo[destination] = balanceAfter
        unlockWriting()
        val destinationAccount = AccountDetailsDto(destination, balanceAfter)
        val depositResult = EventResultDto.DepositResult(destinationAccount)
        return depositResult(depositResult)
    }

    override fun withdraw(event: EventDto.Withdraw): Optional<String> {
        val (origin, amount) = event
        lockWriting()
        val balanceBefore = repo[origin] ?: return unlockWriting().run { withdrawResult() }
        val balanceAfter = balanceBefore - amount
        repo[origin] = balanceAfter
        unlockWriting()
        val originAccount = AccountDetailsDto(origin, balanceAfter)
        val withdrawResult = EventResultDto.WithdrawResult(originAccount)
        return withdrawResult(withdrawResult)
    }

    override fun transfer(event: EventDto.Transfer): Optional<String> {
        val (origin, destination, amount) = event
        lockWriting()
        val originBalanceBefore = repo[origin] ?: return unlockWriting().run { transferResult() }
        val destinationBalanceBefore = repo[destination] ?: 0
        val originBalanceAfter = originBalanceBefore - amount
        val destinationBalanceAfter = destinationBalanceBefore + amount
        repo[origin] = originBalanceAfter
        repo[destination] = destinationBalanceAfter
        unlockWriting()
        val originAccount = AccountDetailsDto(origin, originBalanceAfter)
        val destinationAccount = AccountDetailsDto(destination, destinationBalanceAfter)
        val transferResult = EventResultDto.TransferResult(originAccount, destinationAccount)
        return transferResult(transferResult)
    }

    private fun depositResult(depositResult: EventResultDto.DepositResult? = null) =
        Optional.ofNullable(depositResult)
            .map { """{"destination": {"id":"${it.destination.id}", "balance":${it.destination.balance}}}""" }

    private fun withdrawResult(withdrawResult: EventResultDto.WithdrawResult? = null) =
        Optional.ofNullable(withdrawResult)
            .map { """{"origin": {"id":"${it.origin.id}", "balance":${it.origin.balance}}}""" }

    private fun transferResult(transferResult: EventResultDto.TransferResult? = null) =
        Optional.ofNullable(transferResult)
            .map { """{"origin": {"id":"${it.origin.id}", "balance":${it.origin.balance}}, "destination": {"id":"${it.destination.id}", "balance":${it.destination.balance}}}""" }

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
