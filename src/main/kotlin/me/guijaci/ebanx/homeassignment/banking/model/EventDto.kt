package me.guijaci.ebanx.homeassignment.banking.model

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type")
sealed class EventDto(val type: String) {

    @JsonTypeName(TYPE_DEPOSIT)
    data class DepositDto(
        val destination: Long,
        val amount: Long,
    ) : EventDto(TYPE_DEPOSIT)

    @JsonTypeName(TYPE_WITHDRAW)
    data class WithdrawDto(
        val origin: Long,
        val amount: Long,
    ) : EventDto(TYPE_WITHDRAW)

    @JsonTypeName(TYPE_TRANSFER)
    data class TransferDto(
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
