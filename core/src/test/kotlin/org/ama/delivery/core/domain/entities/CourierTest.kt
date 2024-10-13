package org.ama.delivery.core.domain.entities

import kotlin.test.Test
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldMatch
import org.ama.delivery.core.domain.common.Location

class CourierTest{
    @Test
    fun `Should create Courier with correct properties`(){
        // Given
        val correctName = "John"
        val correctTrType = TransportType.fromName("Pedestrian").shouldBeRight()
        val correctLocation = Location.from(3, 3).shouldBeRight()
        val uuidRegex = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$".toRegex()

        // When
        val result = Courier.create(correctName, correctTrType, correctLocation)

        // Then
        val courier = result.shouldBeRight()
        courier.id.toString() shouldMatch uuidRegex
        courier.name shouldBe correctName
        courier.transportType shouldBe correctTrType
        courier.location() shouldBe correctLocation
    }

    @Test
    fun `Should return error if name is incorrect`(){
        // Given
        val incorrectName = "    "
        val correctTrType = TransportType.fromName("Pedestrian").shouldBeRight()
        val correctLocation = Location.from(3, 3).shouldBeRight()

        // When
        val result = Courier.create(incorrectName, correctTrType, correctLocation)

        // Then
        result.shouldBeLeft(CourierError.IncorrectName)
    }

    @Test
    fun `Should assign order and return assignments`(){
        // Given
        val correctName = "Jane"
        val correctTrType = TransportType.fromName("Car").shouldBeRight()
        val correctLocation = Location.from(3, 3).shouldBeRight()
        val orderDestination = Location.from(5, 5).shouldBeRight()
        val orderId = DeliveryOrderId.new()

        // When
        val courier = Courier.create(correctName, correctTrType, correctLocation).shouldBeRight()

        // Then
        var assignments = courier.getAssignments()
        assignments.size shouldBe 0

        courier.assignOrder(DeliveryOrder.create(orderId, orderDestination))
        assignments = courier.getAssignments()
        assignments.size shouldBe 1
        assignments[0].orderId shouldBe orderId
    }

    @Test
    fun `Should change status when processing delivery`(){
        // Given
        val correctName = "Jane"
        val correctTrType = TransportType.fromName("Bicycle").shouldBeRight()
        val correctLocation = Location.from(3, 3).shouldBeRight()
        val courier = Courier.create(correctName, correctTrType, correctLocation).shouldBeRight()

        val orderDestination = Location.from(5, 5).shouldBeRight()
        val orderId = DeliveryOrderId.new()

        // When
        // Then
        courier.startNextDelivery().shouldBeLeft(DeliveryProcessingError.NoAssignmentsToDeliver)
        courier.completeCurrentDelivery().shouldBeLeft(DeliveryProcessingError.NoCurrentDelivery)
        courier.getCurrentDeliveryOrderId().shouldBeLeft(DeliveryProcessingError.NoCurrentDelivery)

        courier.assignOrder(DeliveryOrder.create(orderId, orderDestination)).shouldBeRight()
        courier.status() shouldBe CourierStatus.Free

        courier.startNextDelivery().shouldBeRight()
        courier.status() shouldBe CourierStatus.Busy
        courier.getCurrentDeliveryOrderId().shouldBeRight(orderId)

        courier.completeCurrentDelivery().shouldBeRight()
        courier.status() shouldBe CourierStatus.Free
        courier.getCurrentDeliveryOrderId().shouldBeLeft(DeliveryProcessingError.NoCurrentDelivery)

        courier.cancelDeliveryOrderAssignment(orderId).shouldBeLeft(DeliveryProcessingError.CantCancelDeliveredOrder)
        courier.status() shouldBe CourierStatus.Free

        val newOrderId = DeliveryOrderId.new()
        courier.assignOrder(DeliveryOrder.create(newOrderId, orderDestination)).shouldBeRight()
        courier.status() shouldBe CourierStatus.Free

        courier.startNextDelivery().shouldBeRight()
        courier.status() shouldBe CourierStatus.Busy

        courier.cancelDeliveryOrderAssignment(newOrderId).shouldBeRight()
        courier.status() shouldBe CourierStatus.Free
    }

    @Test
    fun `Should return correct status`(){
        // Given
        val correctName = "Jane"
        val correctTrType = TransportType.fromName("Bicycle").shouldBeRight()
        val correctLocation = Location.from(3, 3).shouldBeRight()
        val orderDestination = Location.from(5, 5).shouldBeRight()
        val orderId = DeliveryOrderId.new()

        // When
        val courier = Courier.create(correctName, correctTrType, correctLocation).shouldBeRight()

        // Then
        courier.status() shouldBe CourierStatus.Free
        courier.assignOrder(DeliveryOrder.create(orderId, orderDestination))
        courier.status() shouldBe CourierStatus.Free
    }

    @Test
    fun `Should return duration of delivery to given location`(){
        // Given
        val correctName = "Jane"
        val correctTrType = TransportType.fromName("Bicycle").shouldBeRight()
        val currentLocation = Location.from(3, 3).shouldBeRight()
        val targetLocation1 = Location.from(5, 6).shouldBeRight()
        val targetLocation2 = Location.from(5, 7).shouldBeRight()

        val courier = Courier.create(correctName, correctTrType, currentLocation).shouldBeRight()

        // When
        val duration1 = courier.estimateTimeToReachLocation(targetLocation1)
        val duration2 = courier.estimateTimeToReachLocation(targetLocation2)

        // Then
        duration1 shouldBe 3
        duration2 shouldBe 3
    }

    @Test
    fun `Should move to order destination`(){
        // Given
        val correctName = "Jane"
        val correctTrType = TransportType.fromName("Pedestrian").shouldBeRight()
        val startLocation = Location.from(3, 3).shouldBeRight()
        val courier = Courier.create(correctName, correctTrType, startLocation).shouldBeRight()

        val orderId = DeliveryOrderId.new()
        val orderDestination = Location.from(5, 5).shouldBeRight()
        val order = DeliveryOrder.create(orderId, orderDestination)
        courier.assignOrder(order).shouldBeRight()

        // When
        // Then
        courier.stepToCurrentDeliveryDestination().shouldBeLeft(DeliveryProcessingError.NoCurrentDelivery)
        courier.startNextDelivery().shouldBeRight()

        val numOfSteps = courier.estimateTimeToReachLocation(orderDestination)
        for (step in 1 .. numOfSteps){
            courier.stepToCurrentDeliveryDestination().shouldBeRight()
            if (step != numOfSteps)
                courier.location() shouldNotBe orderDestination
        }

        courier.location() shouldBe orderDestination
    }
}