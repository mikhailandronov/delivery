package org.ama.delivery.core.domain.common

import arrow.core.getOrElse
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.ints.shouldBeInRange
import kotlin.test.Test
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf

class LocationTest2 {
    @Test
    fun `Should create location with correct values`() {
        // Given
        val correctX = 5
        val correctY = 9

        // When
        val result = Location.from(correctX, correctY)

        // Then
        val createdLocation = result.shouldBeRight()
        createdLocation.xToInt() shouldBe correctX
        createdLocation.yToInt() shouldBe correctY
    }

    @Test
    fun `Should return an error for incorrect coordinates`() {
        // Given
        val valuesX = arrayOf(
            Location.maxLocation().xToInt() + 1,
            Location.minLocation().xToInt() - 1,
            Location.minLocation().xToInt() // to combine correct and incorrect coordinates
        )

        val valuesY = arrayOf(
            Location.maxLocation().yToInt() + 1,
            Location.minLocation().yToInt() - 1,
            Location.minLocation().yToInt() // to combine correct and incorrect coordinates
        )

        for (incorrectX in valuesX)
            for (incorrectY in valuesY)
                if (incorrectX != Location.minLocation().xToInt() ||
                    incorrectY != Location.minLocation().yToInt()
                ) {
                    // When
                    val createdLocation = Location.from(incorrectX, incorrectY)
                    // Then
                    createdLocation.shouldBeLeft(LocationError.IncorrectCoordinates)
                }
    }

    @Test
    fun `Should verify equality`() {
        // Given
        val loc1 = Location.from(1, 1).getOrNull()
        val loc2 = Location.from(1, 1).getOrNull()
        val loc3 = Location.from(2, 1).getOrNull()
        // When
        // Then
        loc1 shouldBe loc2
        loc1 shouldNotBe loc3
    }

    @Test
    fun `Should calculate distance`(){
        // Given
        val loc1 = Location.from(2, 3).getOrElse { Location.minLocation() }
        val loc2 = Location.from(5, 5).getOrElse { Location.minLocation() }
        val loc3 = Location.from(5, 5).getOrElse { Location.minLocation() }
        // When
        val distNonZero = loc1.distanceTo(loc2)
        val distZero = loc2.distanceTo(loc3)
        // Then
        distZero shouldBe 0
        distNonZero shouldBe 5
    }

    @Test
    fun `Should provide random location object`(){
        // When
        val location = Location.random()
        // Then
        location.shouldBeInstanceOf<Location>()
        location.xToInt() shouldBeInRange (IntRange(
            Location.minLocation().xToInt(),
            Location.maxLocation().xToInt())
        )
        location.yToInt() shouldBeInRange (IntRange(
            Location.minLocation().yToInt(),
            Location.maxLocation().yToInt())
        )
    }
}
