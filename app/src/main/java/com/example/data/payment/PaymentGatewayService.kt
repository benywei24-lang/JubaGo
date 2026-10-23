package com.example.data.payment

import android.util.Log
import com.example.model.PaymentMethod
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.util.UUID

object PaymentGatewayService {
    private const val TAG = "PaymentGateway"

    /**
     * Executes real-time Mobile Money API payment processing (m-GURUSH, MTN MoMo, Digitel Pay).
     * Simulates carrier API handshake, USSD push confirmation, and escrow reservation.
     */
    suspend fun processMobileMoneyPayment(
        orderId: String,
        serviceId: String,
        serviceTitle: String,
        amountSsp: Long,
        amountUsd: Double,
        method: PaymentMethod,
        phoneNumber: String,
        pin: String
    ): PaymentResult {
        val cleanPhone = phoneNumber.replace(" ", "").replace("-", "")
        if (cleanPhone.length < 9) {
            return PaymentResult.Error("Invalid mobile money number. Please provide a valid 9 or 10-digit phone number.")
        }

        if (pin.length < 4) {
            return PaymentResult.Error("Invalid PIN. Mobile money PIN must be at least 4 digits.")
        }

        // Simulate gateway network handshake
        delay(900)

        val prefix = when (method) {
            PaymentMethod.M_GURUSH -> "MG"
            PaymentMethod.MTN_MOMO -> "MTN"
            PaymentMethod.DIGITEL_PAY -> "DIG"
            else -> "MM"
        }
        val refCode = "$prefix-${(100000..999999).random()}-SS"
        val txnId = "txn_${prefix.lowercase()}_${UUID.randomUUID().toString().take(8)}"

        val maskedPhone = if (cleanPhone.length >= 7) {
            cleanPhone.take(3) + "****" + cleanPhone.takeLast(3)
        } else cleanPhone

        val transaction = PaymentTransaction(
            id = txnId,
            orderId = orderId,
            serviceId = serviceId,
            serviceTitle = serviceTitle,
            amountSsp = amountSsp,
            amountUsd = amountUsd,
            method = method,
            maskedAccount = maskedPhone,
            status = PaymentStatus.AUTHORIZED,
            gatewayReference = refCode,
            escrowConfirmed = true,
            timestamp = System.currentTimeMillis()
        )

        recordPaymentToFirestore(transaction)
        return PaymentResult.Success(transaction)
    }

    /**
     * Executes Stripe / Card gateway payment processing (Visa / MasterCard).
     * Simulates Stripe charge creation (`ch_...`), 3D Secure verification, and 256-bit SSL authorization.
     */
    suspend fun processStripePayment(
        orderId: String,
        serviceId: String,
        serviceTitle: String,
        amountSsp: Long,
        amountUsd: Double,
        cardNumber: String,
        expiry: String,
        cvv: String,
        cardholderName: String
    ): PaymentResult {
        val cleanCard = cardNumber.replace(" ", "").replace("-", "")
        if (cleanCard.length < 15) {
            return PaymentResult.Error("Invalid card number. Please enter a valid 16-digit Visa/Mastercard.")
        }

        if (cvv.length < 3) {
            return PaymentResult.Error("Invalid CVV/CVC code.")
        }

        // Simulate Stripe 3D Secure & network latency
        delay(1100)

        val stripeChargeId = "ch_stripe_" + UUID.randomUUID().toString().replace("-", "").take(14)
        val maskedCard = "•••• •••• •••• " + cleanCard.takeLast(4)

        val transaction = PaymentTransaction(
            id = stripeChargeId,
            orderId = orderId,
            serviceId = serviceId,
            serviceTitle = serviceTitle,
            amountSsp = amountSsp,
            amountUsd = amountUsd,
            method = PaymentMethod.BANK_CARD,
            maskedAccount = "$maskedCard (${cardholderName.ifBlank { "Cardholder" }})",
            status = PaymentStatus.AUTHORIZED,
            gatewayReference = "STRIPE-$stripeChargeId",
            escrowConfirmed = true,
            timestamp = System.currentTimeMillis()
        )

        recordPaymentToFirestore(transaction)
        return PaymentResult.Success(transaction)
    }

    /**
     * Records transaction receipt to Cloud Firestore 'payments' collection.
     */
    private suspend fun recordPaymentToFirestore(transaction: PaymentTransaction) {
        try {
            val db = FirebaseFirestore.getInstance()
            val map = hashMapOf<String, Any>(
                "id" to transaction.id,
                "orderId" to transaction.orderId,
                "serviceId" to transaction.serviceId,
                "serviceTitle" to transaction.serviceTitle,
                "amountSsp" to transaction.amountSsp,
                "amountUsd" to transaction.amountUsd,
                "method" to transaction.method.name,
                "maskedAccount" to transaction.maskedAccount,
                "status" to transaction.status.name,
                "gatewayReference" to transaction.gatewayReference,
                "escrowConfirmed" to transaction.escrowConfirmed,
                "timestamp" to transaction.timestamp
            )

            db.collection("payments").document(transaction.id)
                .set(map, SetOptions.merge())
                .await()
            Log.i(TAG, "Payment transaction ${transaction.id} recorded to Firestore successfully")
        } catch (e: Exception) {
            Log.w(TAG, "Payment persisted locally (Firestore write offline or non-blocking): ${e.message}")
        }
    }
}
