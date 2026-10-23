package com.example.model

import java.util.UUID

data class DeliveryAddress(
    val id: String = UUID.randomUUID().toString(),
    val label: String, // e.g. "Home", "Work", "Store", "Family"
    val streetAddress: String, // e.g. "Block 3, House 14"
    val area: String, // e.g. "Hai Cinema", "Munuki", "Hai Malakal", "Konyo Konyo", "Gudele"
    val city: String = "Juba, South Sudan",
    val landmark: String = "",
    val contactPhone: String = "",
    val isDefault: Boolean = false
) {
    val fullDisplayAddress: String
        get() = buildString {
            append(streetAddress)
            if (area.isNotBlank()) append(", ").append(area)
            if (city.isNotBlank()) append(", ").append(city)
            if (landmark.isNotBlank()) append(" (Near: ").append(landmark).append(")")
        }
}

data class SavedPaymentMethod(
    val id: String = UUID.randomUUID().toString(),
    val method: PaymentMethod,
    val title: String,
    val accountIdentifier: String, // Masked phone e.g. "+211 92 *** 4567" or card "**** 4242"
    val accountHolder: String,
    val isDefault: Boolean = false,
    val expiryDate: String? = null // e.g. "12/28" for Bank Card
)
