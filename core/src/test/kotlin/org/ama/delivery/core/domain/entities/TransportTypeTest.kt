package org.ama.delivery.core.domain.entities

import kotlin.test.Test
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe

class TransportTypeTest {
    @Test
    fun `Should be equal when ids are equal`() {
        // Given
        val id = 2

        // When
        val trType1 = TransportType.fromId(id).shouldBeRight()
        val trType2 = TransportType.fromId(id).shouldBeRight()

        // Then
        trType1.shouldBeEqual(trType2)
    }

    @Test
    fun `Should return correct TransportType by correct name or id`() {
        // Given
        val correctIds = arrayOf(1, 2, 3)
        val correctNames = arrayOf("Pedestrian", "Bicycle", "Car")

        for (id in correctIds) {
            // When
            val trType = TransportType.fromId(id).shouldBeRight()
            // Then
            trType.id shouldBe id
            when (trType.id){
                1 -> trType.name shouldBe "Pedestrian"
                2 -> trType.name shouldBe "Bicycle"
                3 -> trType.name shouldBe "Car"
            }
        }

        for (name in correctNames) {
            // When
            val trType = TransportType.fromName(name).shouldBeRight()
            // Then
            trType.name shouldBe name
            when (trType.name){
                "Pedestrian" -> {
                    trType.id shouldBe 1
                    trType.speed shouldBe 1
                }
                "Bicycle" -> {
                    trType.id shouldBe 2
                    trType.speed shouldBe 2
                }
                "Car" -> {
                    trType.id shouldBe 3
                    trType.speed shouldBe 3
                }
            }
        }
    }

    @Test
    fun `Should return an error if name or id is incorrect`() {
        // Given
        val incorrectId = 4
        val incorrectName = "Велосипедист"
        // Then
        TransportType.fromId(incorrectId).shouldBeLeft(TransportTypeError.IncorrectId)
        TransportType.fromName(incorrectName).shouldBeLeft(TransportTypeError.IncorrectName)
    }
}