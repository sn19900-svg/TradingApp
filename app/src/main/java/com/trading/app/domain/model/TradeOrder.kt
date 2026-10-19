package com.trading.app.domain.model

data class TradeOrder(
    val orderId: String,
    val symbol: String,
    val type: OrderType,       // BUY أو SELL
    val price: Double,
    val quantity: Double,
    val percentage: Double,    // النسبة المئوية المحددة من رصيد المستخدم
    val timestamp: Long,
    val status: OrderStatus
)

enum class OrderType {
    BUY, SELL
}

enum class OrderStatus {
    PENDING, COMPLETED, CANCELLED
}
