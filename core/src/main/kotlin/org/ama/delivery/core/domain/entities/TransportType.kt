package org.ama.delivery.core.domain.entities

import arrow.core.Either
import arrow.core.raise.either

class TransportType
internal constructor(
    val id: Int,
    val name: String,
    val speed: Int
) {
    companion object {
        private val pedestrian = TransportType(id = 1, name = "Pedestrian", speed = 1)
        private val bicycle = TransportType(id = 2, name = "Bicycle", speed = 2)
        private val car = TransportType(id = 3, name = "Car", speed = 3)

        fun fromName(name: String): Either<TransportTypeError, TransportType> = either {
            when (name) {
                "Pedestrian" -> pedestrian
                "Bicycle" -> bicycle
                "Car" -> car
                else -> raise(TransportTypeError.IncorrectName)
            }
        }
        fun fromId(id: Int): Either<TransportTypeError, TransportType> = either {
            when (id) {
                1 -> pedestrian
                2 -> bicycle
                3 -> car
                else -> raise(TransportTypeError.IncorrectId)
            }
        }
    }
}

sealed class TransportTypeError {
    data object IncorrectName : TransportTypeError()
    data object IncorrectId : TransportTypeError()
}