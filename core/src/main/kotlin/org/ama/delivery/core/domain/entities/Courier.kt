package org.ama.delivery.core.domain.entities

import arrow.core.Either
import arrow.core.raise.either
import org.ama.delivery.core.domain.common.Location
import org.ama.delivery.core.domain.common.ValueObject
import java.util.*
import kotlin.math.abs
import kotlin.math.ceil


class Courier private constructor(
    val id: CourierId,
    val name: String,
    val transportType: TransportType,
    private var location: Location,
    private val assignments: MutableList<DeliveryOrderAssignment> = mutableListOf()
): AggregateRoot {

    companion object {
        fun create(
            name: String, transportType: TransportType, location: Location
        ): Either<CourierError, Courier> = either {
            if (name.isBlank()) raise(CourierError.IncorrectName)
            Courier(CourierId.new(), name, transportType, location)
        }
    }

    private fun currentDelivery(): DeliveryOrderAssignment? {
        return getAssignments().find { it.status() == AssignmentStatus.OnTheWay }
    }

    fun location() = location

    fun status(): CourierStatus =
        if (currentDelivery() != null) CourierStatus.Busy
        else CourierStatus.Free

    fun assignOrder(order: DeliveryOrder): Either<CourierError, Unit> = either {
        assignments.add(DeliveryOrderAssignment.create(order, this@Courier))
    }

    fun getAssignments(): List<DeliveryOrderAssignment> = assignments.toList()

    fun estimateTimeToReachLocation(targetLocation: Location): Int {
        val distance = location().distanceTo(targetLocation)
        val time = distance / transportType.speed.toDouble()
        return ceil(time).toInt()
    }

    fun startNextDelivery(): Either<DeliveryProcessingError, Unit> = either {
        if (status() != CourierStatus.Free) raise(DeliveryProcessingError.CourierIsBusy)
        val assignmentToDeliver = getAssignments().find { it.status() == AssignmentStatus.Created }
        if (assignmentToDeliver == null) raise(DeliveryProcessingError.NoAssignmentsToDeliver)
        assignmentToDeliver.setStatus(AssignmentStatus.OnTheWay)
    }

    fun getCurrentDeliveryOrderId(): Either<DeliveryProcessingError, DeliveryOrderId> = either {
        currentDelivery()?.orderId
            ?: raise(DeliveryProcessingError.NoCurrentDelivery)
    }

    fun completeCurrentDelivery(): Either<DeliveryProcessingError, Unit> = either {
        currentDelivery()?.setStatus(AssignmentStatus.Delivered)
            ?: raise(DeliveryProcessingError.NoCurrentDelivery)
    }

    fun cancelDeliveryOrderAssignment(orderId: DeliveryOrderId): Either<DeliveryProcessingError, Unit> = either {
        val orderAssignments = getAssignments().filter { it.orderId == orderId }

        if (orderAssignments.isEmpty())
            raise(DeliveryProcessingError.DeliveryOrderNotFoundInAssignments)
        if (orderAssignments.find { it.status() == AssignmentStatus.Delivered } != null)
            raise(DeliveryProcessingError.CantCancelDeliveredOrder)

        orderAssignments.forEach { assignment ->
            assignment.setStatus(AssignmentStatus.Cancelled)
        }
    }

    fun stepToCurrentDeliveryDestination() : Either<DeliveryProcessingError, Unit> = either {
        val destination = currentDelivery()?.orderDestination
            ?: raise(DeliveryProcessingError.NoCurrentDelivery)

        var newX = location().xToInt()
        var newY = location().yToInt()
        var xDiff = destination.xToInt() - newX
        var yDiff = destination.yToInt() - newY
        val stepsAllowed = transportType.speed
        var curStep = 0

        while (curStep < stepsAllowed){
            if (abs(xDiff) > 0){
                newX += (xDiff / abs(xDiff))
                xDiff = destination.xToInt() - newX
                curStep++
            }

            if (curStep >= stepsAllowed) break

            if (abs(yDiff) > 0){
                newY += (yDiff / abs(yDiff))
                yDiff = destination.yToInt() - newY
                curStep++
            }

            if (xDiff == 0 && yDiff == 0) break
        }

        Location.from(newX, newY).map {
            location = it
        }
    }
}

enum class CourierStatus(val id: Int) {
    Free(1), // Свободен
    Busy(2)  // Занят
}

data class CourierId
internal constructor(private val id: UUID) : ValueObject {
    fun toUUID() = id
    override fun toString() = id.toString()

    companion object {
        fun from(id: UUID) = CourierId(id)
        fun from(id: String) = CourierId(UUID.fromString(id))
        fun new(): CourierId = CourierId(UUID.randomUUID())
    }
}

sealed class CourierError {
    data object IncorrectName : CourierError()
}
