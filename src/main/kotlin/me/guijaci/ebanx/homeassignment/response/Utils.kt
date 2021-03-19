package me.guijaci.ebanx.homeassignment.response

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity


fun ok() =
    ResponseEntity.ok().build<Nothing>()

fun <T> ok(body: T) =
    ResponseEntity.ok(body)

fun <T> created(body: T) =
    ResponseEntity.status(HttpStatus.CREATED).body(body)

fun notFound() =
    ResponseEntity.status(HttpStatus.NOT_FOUND).body(0)

fun badRequest() =
    ResponseEntity.status(HttpStatus.BAD_REQUEST).build<Nothing>()
