package com.miassolutions.milkledger.utils.extensions

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

private val zone: ZoneId = ZoneId.systemDefault()

/* ---------- Long → Date ---------- */

fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this)
        .atZone(zone)
        .toLocalDate()

fun Long.toLocalDateTime(): LocalDateTime =
    Instant.ofEpochMilli(this)
        .atZone(zone)
        .toLocalDateTime()

/* ---------- Date → Long ---------- */

fun LocalDate.toMillis(): Long =
    atStartOfDay(zone)
        .toInstant()
        .toEpochMilli()

fun LocalDateTime.toMillis(): Long =
    atZone(zone)
        .toInstant()
        .toEpochMilli()
