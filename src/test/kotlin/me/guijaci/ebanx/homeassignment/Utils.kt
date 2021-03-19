package me.guijaci.ebanx.homeassignment

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef

inline fun <reified T> ObjectMapper.write(obj: T) =
    writerFor(jacksonTypeRef<T>()).writeValueAsString(obj)

inline fun <reified T> ObjectMapper.read(serialized: String) =
    readerFor(jacksonTypeRef<T>()).readValue<T>(serialized)