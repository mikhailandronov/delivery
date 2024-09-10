package org.ama.delivery.core.domain.common

import arrow.core.getOrElse
import kotlin.test.*

class LocationTest {

    @Test
    fun `Should create location with correct values`() {
        // Given
        val correctX = 5
        val correctY = 9
        // When
        val createdLocation = Location.from(correctX, correctY)
        // Then
        assertTrue(createdLocation.isRight(), "Should return Location as right")
        assertFalse(createdLocation.isLeft(), "Should not return anything as left")
        assertEquals(correctX, createdLocation.getOrNull()?.xToIntValue(), "Incorrect X coordinate")
        assertEquals(correctY, createdLocation.getOrNull()?.yToIntValue(), "Incorrect Y coordinate")
    }

    @Test
    fun `Should return an error for incorrect coordinates`() {
        // Given
        val valuesX = arrayOf(
            Location.maxLocation().xToIntValue() + 1,
            Location.minLocation().xToIntValue() - 1,
            Location.minLocation().xToIntValue() // to combine correct and incorrect coordinates
        )

        val valuesY = arrayOf(
            Location.maxLocation().yToIntValue() + 1,
            Location.minLocation().yToIntValue() - 1,
            Location.minLocation().yToIntValue() // to combine correct and incorrect coordinates
        )

        for (incorrectX in valuesX)
            for (incorrectY in valuesY)
                if (incorrectX != Location.minLocation().xToIntValue() ||
                    incorrectY != Location.minLocation().yToIntValue()
                ) {
                    // When
                    val createdLocation = Location.from(incorrectX, incorrectY)
                    // Then
                    assertTrue(createdLocation.isLeft(), "Should return an error as left")
                    assertFalse(createdLocation.isRight(), "Should not return anything as right")
                    assertIs<LocationError.IncorrectCoordinates>(
                        createdLocation.leftOrNull(), "Incorrect error type"
                    )
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
        assertEquals(loc1, loc2, "loc1 and loc2 should be equal")
        assertNotEquals(loc1, loc3, "loc1 and loc3 should not be equal")
        assertTrue(loc2 != loc3, "loc2 and loc3 should not be equal")
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
        assertEquals(5, distNonZero, "Distance should be 5")
        assertEquals(0, distZero, "Distance should be 0")
    }

    @Test
    fun `Should provide random location object`(){
        // When
        val location = Location.random()
        // Then
        assertIs<Location>(location, "Random location object should be Location")
    }
}