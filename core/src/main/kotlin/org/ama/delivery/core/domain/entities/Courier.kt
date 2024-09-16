package org.ama.delivery.core.domain.entities

import org.ama.delivery.core.domain.common.ValueObject
import java.util.*

class Courier {
}

enum class CourierStatus (val id: Int){
    Free (1), // Свободен
    Busy (2)  // Занят
}

data class CourierId
internal constructor (private val id: UUID) : ValueObject {
    fun toUUID() = id
    override fun toString() = id.toString()
    companion object {
        fun from(id: UUID) = CourierId(id)
        fun from(id: String) = CourierId(UUID.fromString(id))
        fun new(): CourierId = CourierId(UUID.randomUUID())
    }
}
