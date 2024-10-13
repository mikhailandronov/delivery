package org.ama.delivery.core.domain.entities

import arrow.core.Either
import arrow.core.raise.either
import org.ama.delivery.core.domain.common.Location
import org.ama.delivery.core.domain.common.ValueObject
import java.util.*

class DeliveryOrder
private constructor(
    val id: DeliveryOrderId,
    val destination: Location,
    private var status: DeliveryOrderStatus
): AggregateRoot {

    companion object {
        fun create(
            id: DeliveryOrderId, destination: Location
        ) = DeliveryOrder(id, destination, DeliveryOrderStatus.Created)
    }

    fun status(): DeliveryOrderStatus = status

    fun deriveStatusFromCourier(courier: Courier): Either<DeliveryOrderError, Unit> = either {
        val activeOrderAssignmentStatus = courier.getAssignments()
            .find { it.orderId == id &&
                    it.status() != AssignmentStatus.Cancelled}
            ?.status()
        val cancelledOrderAssignments = courier.getAssignments()
            .filter { it.orderId == id &&
                    it.status() == AssignmentStatus.Cancelled
            }

        if (activeOrderAssignmentStatus == null) {
            if (cancelledOrderAssignments.isNotEmpty()){
                status = DeliveryOrderStatus.Created
                return@either
            }
            else
                raise(DeliveryOrderError.IncorrectStatusChange("Courier doesn't have assignments for this delivery order"))
        }

        if (activeOrderAssignmentStatus == AssignmentStatus.Delivered && status() != DeliveryOrderStatus.Assigned)
            raise(DeliveryOrderError.IncorrectStatusChange("Order hasn't been assigned, so it can't be completed"))

        if (status() == DeliveryOrderStatus.Completed && activeOrderAssignmentStatus != AssignmentStatus.Delivered)
            raise(DeliveryOrderError.IncorrectStatusChange("The order has been completed already"))

        status = when (activeOrderAssignmentStatus){
            AssignmentStatus.Created -> DeliveryOrderStatus.Assigned
            AssignmentStatus.OnTheWay -> DeliveryOrderStatus.Assigned
            AssignmentStatus.Delivered -> DeliveryOrderStatus.Completed
            else -> raise(DeliveryOrderError.IncorrectStatusChange())
        }
    }

    fun returnToSender(): Either<DeliveryOrderError, Unit> = either {
        if (status() == DeliveryOrderStatus.Completed)
            raise(DeliveryOrderError.IncorrectStatusChange("The order has been completed already"))
        status = DeliveryOrderStatus.Returned
    }
}

enum class DeliveryOrderStatus(val id: Int) {
    Created(1),  // Ждёт назначения
    Assigned(2), // Назначена
    Completed(3), // Доставлена
    Returned(4) // Возвращена отправителю
}

data class DeliveryOrderId
internal constructor(private val id: UUID) : ValueObject {
    fun toUUID() = id
    override fun toString() = id.toString()

    companion object {
        fun from(id: UUID) = DeliveryOrderId(id)
        fun from(id: String) = DeliveryOrderId(UUID.fromString(id))
        fun new(): DeliveryOrderId = DeliveryOrderId(UUID.randomUUID())
    }
}

sealed class DeliveryOrderError {
    data class IncorrectStatusChange(val message: String = "") : DeliveryOrderError()
}
