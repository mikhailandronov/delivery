package org.ama.delivery.core.domain.entities

import io.kotest.assertions.arrow.core.shouldBeLeft
import kotlin.test.Test
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.ama.delivery.core.domain.common.Location

class DeliveryOrderTest {
    @Test
    fun `Should create DeliveryOrder with correct properties`() {
        // Given
        val correctId = DeliveryOrderId.new()
        val correctDestination = Location.from(7, 7).shouldBeRight()
        val statusOnCreation = DeliveryOrderStatus.Created

        // When
        val order = DeliveryOrder.create(correctId, correctDestination)

        // Then
        order.id shouldBe correctId
        order.destination shouldBe correctDestination
        order.status() shouldBe statusOnCreation
    }

    @Test
    fun `Should derive status from Courier assignments`() {
        // Given
        val correctId = DeliveryOrderId.new()
        val correctDestination = Location.from(7, 7).shouldBeRight()

        val trType = TransportType.fromName("Car").shouldBeRight()
        val location = Location.from(1, 1).shouldBeRight()
        val courier = Courier.create("John", trType, location).shouldBeRight()

        // When
        val order = DeliveryOrder.create(correctId, correctDestination)

        // Then
        courier.assignOrder(order).shouldBeRight()
        order.deriveStatusFromCourier(courier).shouldBeRight()
        order.status() shouldBe  DeliveryOrderStatus.Assigned

        courier.startNextDelivery().shouldBeRight()
        order.deriveStatusFromCourier(courier).shouldBeRight()
        order.status() shouldBe DeliveryOrderStatus.Assigned

        courier.cancelDeliveryOrderAssignment(order.id).shouldBeRight()
        order.deriveStatusFromCourier(courier).shouldBeRight()
        order.status() shouldBe DeliveryOrderStatus.Created

        courier.assignOrder(order).shouldBeRight()
        courier.startNextDelivery().shouldBeRight()
        order.deriveStatusFromCourier(courier).shouldBeRight()
        order.status() shouldBe DeliveryOrderStatus.Assigned

        courier.completeCurrentDelivery().shouldBeRight()
        order.deriveStatusFromCourier(courier).shouldBeRight()
        order.status() shouldBe DeliveryOrderStatus.Completed

        order.returnToSender().shouldBeLeft().shouldBeInstanceOf<DeliveryOrderError.IncorrectStatusChange>()
    }

    @Test
    fun `Should process return to sender`(){
        // Given
        val correctId = DeliveryOrderId.new()
        val correctDestination = Location.from(7, 7).shouldBeRight()
        val order = DeliveryOrder.create(correctId, correctDestination)

        // When
        order.returnToSender().shouldBeRight()

        // Then
        order.status() shouldBe DeliveryOrderStatus.Returned
    }
}