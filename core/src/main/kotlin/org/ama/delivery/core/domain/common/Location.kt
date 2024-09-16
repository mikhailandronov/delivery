package org.ama.delivery.core.domain.common

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.raise.ensure
import kotlin.math.abs
import kotlin.random.Random

data class Location internal constructor(
    private val x: Int,
    private val y: Int,
) : ValueObject {
    fun xToInt() = x
    fun yToInt() = y

    companion object {
        fun minLocation() = Location(1, 1)
        fun maxLocation() = Location(10, 10)

        fun from(x: Int, y: Int): Either<LocationError, Location> = either {
            ensure( // require() ?
                x >= minLocation().xToInt() &&
                        x <= maxLocation().xToInt()
            ) { LocationError.IncorrectCoordinates }

            ensure( // require() ?
                y >= minLocation().yToInt() &&
                        y <= maxLocation().yToInt()
            ) { LocationError.IncorrectCoordinates }

            Location(x, y)
        }

        fun random(): Location {
            val x = Random.nextInt(minLocation().xToInt(), maxLocation().xToInt())
            val y = Random.nextInt(minLocation().yToInt(), maxLocation().yToInt())
            return from(x, y).getOrElse { minLocation() }
        }
    }

    fun distanceTo(location: Location): Int =
        abs(location.x - x) + abs(location.y - y)
}

sealed class LocationError {
    data object IncorrectCoordinates : LocationError()
}