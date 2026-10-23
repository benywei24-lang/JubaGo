package com.example.data.payment

import com.example.model.PaymentMethod

enum class PaymentStatus(val label: String) {
    INITIATED("Initiated"),
    PROCESSING("Processing Gateway Handshake"),
    AWAITING_PIN_AUTHORIZATION("Awaiting USSD PIN Confirmation"),
    AUTHORIZED("Authorized & Escrow Protected"),
    COMPLETED("Completed"),
    FAILED("Payment Failed")
}

data class PaymentTransaction(
    val id: String = "",
    val orderId: String = "",
    val serviceId: String = "",
    val serviceTitle: String = "",
    val amountSsp: Long = 0L,
    val amountUsd: Double = 0.0,
    val method: PaymentMethod = PaymentMethod.M_GURUSH,
    val maskedAccount: String = "",
    val status: PaymentStatus = PaymentStatus.INITIATED,
    val gatewayReference: String = "",
    val escrowConfirmed: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

sealed class PaymentResult {
    data class Success(val transaction: PaymentTransaction) : PaymentResult()
    data class Error(val errorMessage: String) : PaymentResult()
}
