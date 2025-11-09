package com.miassolutions.milkledger.data.remote


import java.time.LocalDate

object FirestoreDateConverter {

    fun convertLocalDatesToLongs(data: Any?): Any? {
        return when (data) {
            is Map<*, *> -> data.mapValues { convertLocalDatesToLongs(it.value) }
            is List<*> -> data.map { convertLocalDatesToLongs(it) }
            is LocalDate -> data.toEpochDay()
            else -> data
        }
    }

    fun convertLongsToLocalDates(data: Any?): Any? {
        return when (data) {
            is Map<*, *> -> data.mapValues { convertLongsToLocalDates(it.value) }
            is List<*> -> data.map { convertLongsToLocalDates(it) }
            is Number -> {
                val epochDay = data.toLong()
                // Convert only realistic epochDay ranges (~years 1900–2100)
                if (epochDay in 0..80000L) {
                    try {
                        LocalDate.ofEpochDay(epochDay)
                    } catch (_: Exception) {
                        data
                    }
                } else data
            }
            else -> data
        }
    }
}
