package me.guijaci.ebanx.homeassignment.banking.service

import com.fasterxml.jackson.databind.ObjectMapper
import me.guijaci.ebanx.homeassignment.banking.model.AccountDetailsDto
import me.guijaci.ebanx.homeassignment.banking.model.EventDto
import me.guijaci.ebanx.homeassignment.banking.model.EventResultDto
import me.guijaci.ebanx.homeassignment.read
import me.guijaci.ebanx.homeassignment.write
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.*
import kotlin.math.abs

@SpringBootTest
internal class BankingServiceTest {

    @Autowired
    lateinit var service: IBankingService

    @Autowired
    lateinit var mapper: ObjectMapper

    private val random: Random = Random(RANDOM_SEED)

    @BeforeEach
    fun reset() {
        service.reset()
    }

    @Test
    fun `getBalance() is empty before deposit`() {
        val id: Long = random.nextLong()
        val balanceOptional = service.getBalance(id)
        assertFalse(balanceOptional.isPresent) { "Found balance for id $id before deposit" }
    }

    @Test
    fun `reset() clears getBalance() response after deposit`() {
        val id: Long = random.nextLong()
        val amount: Long = abs(random.nextLong())
        service.processEvent(EventDto.Deposit(id, amount))
        val balanceOptional = service.getBalance(id)
        assert(balanceOptional.isPresent) { "No balance found for id $id" }
        assertEquals(balanceOptional.get(), amount)
        service.reset()
        val balanceOptionalAfterReset = service.getBalance(id)
        assertFalse(balanceOptionalAfterReset.isPresent) { "Balance did not reset for $id: returned ${balanceOptionalAfterReset.get()}" }
    }

    @Test
    fun `deposit() returns correct account info`() {
        val id: Long = random.nextLong()
        val amount: Long = abs(random.nextLong())
        val depositResult = service.processEvent(EventDto.Deposit(id, amount))
        assert(depositResult.isPresent) { "Deposit failed for $id" }
        assertEquals(
            mapper.read<EventResultDto.DepositResult>(depositResult.get()),
            EventResultDto.DepositResult(AccountDetailsDto(id, amount))
        )
    }

    @Test
    fun `withdraw() fails before deposit and succeeds after`() {
        service.reset()
        val id: Long = random.nextLong()
        val amountBefore: Long = abs(random.nextLong())
        val amountWithdrawn: Long = amountBefore % abs(random.nextInt())
        val withdrawBeforeFunds = service.processEvent(EventDto.Withdraw(id, amountWithdrawn))
        assertFalse(withdrawBeforeFunds.isPresent) { "Withdraw made before funds added: \nid: $id \nresult:${withdrawBeforeFunds.get()}" }
        val depositResult = service.processEvent(EventDto.Deposit(id, amountBefore))
        assert(depositResult.isPresent) { "Tried to deposit $amountBefore in account $id, but nothing returned as result" }
        val withdrawResult = service.processEvent(EventDto.Withdraw(id, amountWithdrawn))
        assert(withdrawResult.isPresent) { "Tried to withdraw from $id after deposit, but no funds found. \n Result from deposit: ${depositResult.get()}" }
        assertEquals(
            mapper.read<EventResultDto.WithdrawResult>(withdrawResult.get()),
            EventResultDto.WithdrawResult(AccountDetailsDto(id, amountBefore - amountWithdrawn))
        )
    }

    @Test
    fun `transfer() fails for no balance in origin and succeeds after`() {
        val origin = random.nextLong()
        val destination = random.nextLong()
        val amountBefore: Long = abs(random.nextLong())
        val amountTransferred: Long = amountBefore % abs(random.nextInt())
        val transferResultBeforeDeposit =
            service.processEvent(EventDto.Transfer(origin, destination, amountTransferred))
        assertFalse(transferResultBeforeDeposit.isPresent) { "Transferred from account $origin to $destination before even making a deposit. \nResult: ${transferResultBeforeDeposit.get()}" }
        val depositResult = service.processEvent(EventDto.Deposit(origin, amountBefore))
        assert(depositResult.isPresent) { "Deposit of $amountBefore failed for $origin" }
        val transferResult = service.processEvent(EventDto.Transfer(origin, destination, amountTransferred))
        assert(transferResult.isPresent) { "Transfer of $amountTransferred from $origin to $destination failed" }
        assertEquals(
            mapper.read<EventResultDto.TransferResult>(transferResult.get()),
            EventResultDto.TransferResult(
                AccountDetailsDto(origin, amountBefore - amountTransferred),
                AccountDetailsDto(destination, amountTransferred)
            )
        )
    }

    companion object {
        private const val RANDOM_SEED = 4906399541842410040L
    }
}