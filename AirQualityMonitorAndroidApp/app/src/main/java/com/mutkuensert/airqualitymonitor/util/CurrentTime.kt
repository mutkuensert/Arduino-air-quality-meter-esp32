package com.mutkuensert.airqualitymonitor.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object CurrentTime {

    fun now(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return LocalDateTime.now().format(formatter)
    }
}