package org.ama.delivery.core.domain.entities

class Order {

}

enum class OrderStatus (val id: Int){
    Created (1),  // Ждёт назначения
    Assigned (2), // Назначена
    Completed (3) // Доставлена
}