package me.guijaci.ebanx.homeassignment.banking.model

import com.fasterxml.jackson.annotation.JsonIgnore

sealed class EventResultDto(@JsonIgnore val type: String) {

    data class DepositResult(
        val destination: AccountDetailsDto,
    ) : EventResultDto(TYPE_DEPOSIT)

    data class WithdrawResult(
        val origin: AccountDetailsDto,
    ) : EventResultDto(TYPE_WITHDRAW)

    data class TransferResult(
        val origin: AccountDetailsDto,
        val destination: AccountDetailsDto,
    ) : EventResultDto(TYPE_TRANSFER)

    companion object {
        const val TYPE_DEPOSIT = "deposit"
        const val TYPE_WITHDRAW = "withdraw"
        const val TYPE_TRANSFER = "transfer"
    }

}
