package me.guijaci.ebanx.homeassignment.banking

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import me.guijaci.ebanx.homeassignment.banking.model.AccountDetailsDto
import me.guijaci.ebanx.homeassignment.banking.model.EventDto
import me.guijaci.ebanx.homeassignment.banking.model.EventResultDto
import me.guijaci.ebanx.homeassignment.banking.service.IBankingService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.util.*
import kotlin.math.abs

@WebMvcTest(BankingController::class)
internal class BankingControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var mapper: ObjectMapper

    @MockBean
    private lateinit var service: IBankingService

    private val random: Random = Random(RANDOM_SEED)

    @Test
    fun `'POST reset' always returns ok`() {
        mockMvc.perform(post("/reset"))
            .andDo(print())
            .andExpect(status().isOk)
    }

    @Test
    fun `'GET balance' not found`() {
        `when`(service.getBalance(anyLong())).thenReturn(Optional.empty())
        mockMvc.perform(get("/balance").param("account_id", random.nextLong().toString()))
            .andDo(print())
            .andExpect(status().isNotFound)
            .andExpect(content().json("0"))
    }

    @Test
    fun `'GET balance' returns balance`() {
        val balance = abs(random.nextLong())
        `when`(service.getBalance(anyLong())).thenReturn(Optional.of(balance))
        mockMvc.perform(get("/balance").param("account_id", random.nextLong().toString()))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().json("$balance"))
    }

    @Test
    fun `'POST event' unknown event`() {
        mockMvc.perform(
            post("/event")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"type":"unexpectedEvent","operation":"randomValue"}""")
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `'POST event' receive a deposit event for a existing account and returns deposit result`() {
        val depositEvent = EventDto.Deposit(random.nextLong(), abs(random.nextInt().toLong()))
        val destinationAccount =
            AccountDetailsDto(depositEvent.destination, random.nextInt().toLong() + depositEvent.amount)
        val depositResultDto = EventResultDto.DepositResult(destinationAccount)
        val requestBody = mapper.write(depositEvent)
        `when`(service.processEvent(depositEvent))
            .thenReturn(Optional.of(depositResultDto))
        mockMvc.perform(
            post("/event")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.origin").doesNotExist())
            .andExpect(jsonPath("$.type").doesNotExist())
            .andExpect(jsonPath("$.destination").exists())
            .andExpect(jsonPath("$.destination").isMap)
            .andExpect(jsonPath("$.destination.id").exists())
            .andExpect(jsonPath("$.destination.id").isNumber)
            .andExpect(jsonPath("$.destination.amount").exists())
            .andExpect(jsonPath("$.destination.amount").isNumber)
            .andReturn().response.contentAsString.let { response ->
                assertEquals(depositResultDto, mapper.read<EventResultDto.DepositResult>(response))
            }
    }

    @Test
    fun `'POST event' receive a deposit event for a non existing account`() {
        val depositEvent = EventDto.Deposit(random.nextLong(), abs(random.nextInt().toLong()))
        val requestBody = mapper.write(depositEvent)
        `when`(service.processEvent(depositEvent))
            .thenReturn(Optional.empty())
        mockMvc.perform(
            post("/event")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andDo(print())
            .andExpect(status().isNotFound)
            .andExpect(content().json("0"))
    }

    @Test
    fun `'POST event' receive a withdraw event for a existing account and returns withdraw result`() {
        val withdrawEvent = EventDto.Withdraw(random.nextLong(), abs(random.nextInt().toLong()))
        val originAccount = AccountDetailsDto(withdrawEvent.origin, random.nextLong() - withdrawEvent.amount)
        val withdrawResultDto = EventResultDto.WithdrawResult(originAccount)
        val requestBody = mapper.write(withdrawEvent)
        `when`(service.processEvent(withdrawEvent))
            .thenReturn(Optional.of(withdrawResultDto))
        mockMvc.perform(
            post("/event")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andDo(print())
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.destination").doesNotExist())
            .andExpect(jsonPath("$.type").doesNotExist())
            .andExpect(jsonPath("$.origin").exists())
            .andExpect(jsonPath("$.origin").isMap)
            .andExpect(jsonPath("$.origin.id").exists())
            .andExpect(jsonPath("$.origin.id").isNumber)
            .andExpect(jsonPath("$.origin.amount").exists())
            .andExpect(jsonPath("$.origin.amount").isNumber)
            .andReturn().response.contentAsString.let { response ->
                assertEquals(withdrawResultDto, mapper.read<EventResultDto.WithdrawResult>(response))
            }
    }

    @Test
    fun `'POST event' receive a withdraw event for a non existing account`() {
        val withdraw = EventDto.Withdraw(random.nextLong(), abs(random.nextInt().toLong()))
        val requestBody = mapper.write(withdraw)
        `when`(service.processEvent(withdraw))
            .thenReturn(Optional.empty())
        mockMvc.perform(
            post("/event")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andDo(print())
            .andExpect(status().isNotFound)
            .andExpect(content().json("0"))
    }


    @Test
    fun `'POST event' receive a transfer event for a existing account and returns transfer result`() {
        val transferEvent = EventDto.Transfer(random.nextLong(), random.nextLong(), abs(random.nextInt().toLong()))
        val originAccount = AccountDetailsDto(transferEvent.origin, random.nextLong() - transferEvent.amount)
        val destinationAccount =
            AccountDetailsDto(transferEvent.destination, random.nextInt().toLong() + transferEvent.amount)
        val transferResultDto = EventResultDto.TransferResult(originAccount, destinationAccount)
        val requestBody = mapper.write(transferEvent)
        `when`(service.processEvent(transferEvent))
            .thenReturn(Optional.of(transferResultDto))
        mockMvc.perform(
            post("/event")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andDo(print())
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.type").doesNotExist())
            .andExpect(jsonPath("$.destination").exists())
            .andExpect(jsonPath("$.destination").isMap)
            .andExpect(jsonPath("$.destination.id").exists())
            .andExpect(jsonPath("$.destination.id").isNumber)
            .andExpect(jsonPath("$.destination.amount").exists())
            .andExpect(jsonPath("$.destination.amount").isNumber)
            .andExpect(jsonPath("$.origin").exists())
            .andExpect(jsonPath("$.origin").isMap)
            .andExpect(jsonPath("$.origin.id").exists())
            .andExpect(jsonPath("$.origin.id").isNumber)
            .andExpect(jsonPath("$.origin.amount").exists())
            .andExpect(jsonPath("$.origin.amount").isNumber)
            .andReturn().response.contentAsString.let { response ->
                assertEquals(transferResultDto, mapper.read<EventResultDto.TransferResult>(response))
            }
    }

    @Test
    fun `'POST event' receive a transfer event for a non existing account`() {
        val transferEvent = EventDto.Transfer(random.nextLong(), random.nextLong(), abs(random.nextInt().toLong()))
        val requestBody = mapper.write(transferEvent)
        `when`(service.processEvent(transferEvent))
            .thenReturn(Optional.empty())
        mockMvc.perform(
            post("/event")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andDo(print())
            .andExpect(status().isNotFound)
            .andExpect(content().json("0"))
    }


    companion object {
        private const val RANDOM_SEED = 8615929364366092972L

        private inline fun <reified T> ObjectMapper.write(obj: T) =
            writerFor(jacksonTypeRef<T>()).writeValueAsString(obj)

        private inline fun <reified T> ObjectMapper.read(serialized: String) =
            readerFor(jacksonTypeRef<T>()).readValue<T>(serialized)
    }
}