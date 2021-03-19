package me.guijaci.ebanx.homeassignment.banking.model

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
    defaultImpl = EventDto.Unknown::class)
sealed class EventDto(val type: String) {

    class Unknown: EventDto("")

    @JsonTypeName(TYPE_DEPOSIT)
    data class Deposit(
        val destination: Long,
        val amount: Long,
    ) : EventDto(TYPE_DEPOSIT)

    @JsonTypeName(TYPE_WITHDRAW)
    data class Withdraw(
        val origin: Long,
        val amount: Long,
    ) : EventDto(TYPE_WITHDRAW)

    @JsonTypeName(TYPE_TRANSFER)
    data class Transfer(
        val origin: Long,
        val destination: Long,
        val amount: Long,
    ) : EventDto(TYPE_TRANSFER)

    companion object{
        const val TYPE_DEPOSIT = "deposit"
        const val TYPE_WITHDRAW = "withdraw"
        const val TYPE_TRANSFER = "transfer"
    }

}
