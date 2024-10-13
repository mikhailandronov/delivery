package org.ama.delivery.core.domain.entities

import org.ama.delivery.core.domain.common.Location
import org.ama.delivery.core.domain.common.ValueObject
import java.util.*

class DeliveryOrderAssignment private constructor(
    val id: DeliveryOrderAssignmentId,
    val courierId: CourierId,
    val orderId: DeliveryOrderId,
    val orderDestination: Location,
    private var status: AssignmentStatus
) {
    companion object {
        fun create(
            order: DeliveryOrder, courier: Courier
        ) = DeliveryOrderAssignment(
            id = DeliveryOrderAssignmentId.new(),
            courierId = courier.id,
            orderId = order.id,
            orderDestination = order.destination,
            status = AssignmentStatus.Created
        )
    }

    fun status() = status

    fun setStatus(newStatus: AssignmentStatus) {
        if (status() == AssignmentStatus.Delivered || status() == AssignmentStatus.Cancelled)
            throw Exception("Assignment has been completed and can't be changed")
        if (newStatus == AssignmentStatus.Delivered && status() != AssignmentStatus.OnTheWay)
            throw Exception("Assignment was not on the way to be delivered")
        status = newStatus
    }
}

enum class AssignmentStatus(val id: Int) {
    Created(1),   // Создано
    OnTheWay(2),  // Выполняется
    Delivered(3), // Выполнено
    Cancelled(4)  // Отменено
}

data class DeliveryOrderAssignmentId
internal constructor(private val id: UUID) : ValueObject {
    fun toUUID() = id
    override fun toString() = id.toString()

    companion object {
        fun from(id: UUID) = DeliveryOrderAssignmentId(id)
        fun from(id: String) = DeliveryOrderAssignmentId(UUID.fromString(id))
        fun new(): DeliveryOrderAssignmentId = DeliveryOrderAssignmentId(UUID.randomUUID())
    }
}

sealed class DeliveryProcessingError {
    data object CourierIsBusy : DeliveryProcessingError()
    data object NoAssignmentsToDeliver : DeliveryProcessingError()
    data object NoCurrentDelivery : DeliveryProcessingError()
    data object DeliveryOrderNotFoundInAssignments : DeliveryProcessingError()
    data object CantCancelDeliveredOrder : DeliveryProcessingError()
}