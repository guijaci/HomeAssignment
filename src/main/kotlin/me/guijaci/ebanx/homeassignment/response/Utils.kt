package me.guijaci.ebanx.homeassignment.response

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity


fun ok() = ok("OK")

fun <T> ok(body: T) =
    ResponseEntity.ok(body)

fun <T> created(body: T) =
    ResponseEntity.status(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body(body)

fun notFound() =
    ResponseEntity.status(HttpStatus.NOT_FOUND).body(0)

fun badRequest() =
    ResponseEntity.status(HttpStatus.BAD_REQUEST).build<Nothing>()
