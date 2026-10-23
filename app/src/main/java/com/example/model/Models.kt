package com.example.model

enum class ServiceCategory(val displayName: String, val iconName: String) {
    ALL("All Services", "apps"),
    CARS("Rides & 4x4 Cars", "car"),
    FLIGHTS("Juba Flights", "flight"),
    HOTELS("Hotels & Stays", "hotel"),
    FOOD("Juba Food", "restaurant"),
    GROCERY("Supermarket & Goods", "shopping_cart")
}

enum class CoverageScope(val displayName: String, val badgeColorHex: Long) {
    ALL_REGIONS("All Destinations", 0xFF0D5C3A),
    JUBA_LOCAL("Juba City & Environs", 0xFF2E7D32),
    SOUTH_SUDAN_NATIONAL("Across South Sudan", 0xFF0277BD),
    EAST_AFRICA_REGIONAL("East Africa Regional", 0xFFE65100),
    GLOBAL_BEYOND_AFRICA("Beyond Africa & Global", 0xFF6A1B9A)
}

enum class DeliveryStatus(val label: String) {
    PLACED("Order Placed"),
    CONFIRMED("Confirmed by Provider"),
    PREPARING("Preparing / Dispatching"),
    ON_THE_WAY("Rider On The Way"),
    DELIVERED("Delivered & Completed"),
    CANCELLED("Cancelled")
}

enum class PaymentMethod(val title: String, val subtitle: String, val accountHint: String) {
    M_GURUSH("m-GURUSH", "South Sudan premier mobile money", "Enter 9-digit phone (e.g. 092XXXXXXX)"),
    MTN_MOMO("MTN MoMo", "Instant MTN Mobile Money transfer", "Enter MTN number (e.g. 091XXXXXXX)"),
    DIGITEL_PAY("Digitel Pay", "Digitel telecommunication wallet", "Enter Digitel number (e.g. 098XXXXXXX)"),
    CASH_ON_DELIVERY("Cash on Delivery", "Pay cash in SSP to courier upon arrival", "No mobile money PIN required"),
    BANK_CARD("Bank Card (Visa / MC)", "Credit / Debit Card processing", "Enter 16-digit card number")
}

enum class UserRole(val label: String) {
    CUSTOMER("Customer View"),
    MERCHANT("Business Owner Mode"),
    COURIER("Delivery Rider View")
}

data class ServiceItem(
    val id: String,
    val title: String,
    val category: ServiceCategory,
    val providerName: String,
    val rating: Double,
    val reviewCount: Int,
    val isVerified: Boolean,
    val priceSsp: Long,
    val priceUsd: Double,
    val priceUnit: String,
    val location: String,
    val etaMinutes: Int,
    val description: String,
    val isAvailable: Boolean = true,
    val badge: String? = null,
    val tags: List<String> = emptyList(),
    val coverageScope: CoverageScope = CoverageScope.JUBA_LOCAL,
    val country: String = "South Sudan"
)

data class BusinessRegistrationRequest(
    val id: String = "",
    val businessName: String,
    val businessType: String,
    val category: ServiceCategory,
    val coverageScope: CoverageScope,
    val cityAndCountry: String,
    val physicalAddress: String,
    val contactPhone: String,
    val contactEmail: String,
    val registrationTaxNumber: String,
    val serviceOfferingTitle: String,
    val serviceDescription: String,
    val priceSsp: Long,
    val priceUsd: Double,
    val priceUnit: String,
    val isVerified: Boolean = true,
    val registeredAt: String = ""
)

data class OrderBooking(
    val id: String,
    val serviceId: String,
    val title: String,
    val category: ServiceCategory,
    val providerName: String,
    val customerName: String,
    val customerPhone: String,
    val deliveryAddress: String,
    val totalPriceSsp: Long,
    val totalPriceUsd: Double,
    val paymentMethod: PaymentMethod,
    val paymentReference: String,
    val status: DeliveryStatus,
    val courierName: String,
    val courierPhone: String,
    val courierVehicle: String,
    val courierRating: Double,
    val orderedAt: String,
    val etaMinutes: Int,
    val liveProgress: Float = 0.5f, // 0.0f (store/pickup) to 1.0f (destination)
    val riderCurrentLocationName: String = "Ministries Road, Juba"
)

data class Review(
    val id: String = "",
    val serviceId: String = "",
    val authorName: String = "",
    val rating: Double = 5.0,
    val date: String = "",
    val comment: String = "",
    val orderId: String? = null,
    val tags: List<String> = emptyList(),
    val isVerifiedBuyer: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatMessage(
    val id: String,
    val senderName: String,
    val message: String,
    val timestamp: String,
    val isFromUser: Boolean,
    val roleBadge: String = "Support"
)

data class AuditLogEntry(
    val id: String,
    val timestamp: String,
    val action: String,
    val userRole: String,
    val details: String
)

data class MerchantAnalytics(
    val todaySalesSsp: Long,
    val todaySalesUsd: Double,
    val activeOrdersCount: Int,
    val completedOrdersCount: Int,
    val averageDeliveryMinutes: Int,
    val customerRating: Double,
    val weeklySalesTrend: List<Pair<String, Long>> // Day of week to SSP
)

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val phoneNumber: String = "",
    val role: UserRole = UserRole.CUSTOMER,
    val businessName: String? = null,
    val businessType: String? = null,
    val cityAndCountry: String = "Juba, South Sudan",
    val isVerified: Boolean = false,
    val photoUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

sealed interface AuthState {
    data object Unauthenticated : AuthState
    data object Loading : AuthState
    data class Authenticated(val user: UserProfile) : AuthState
    data class Error(val message: String) : AuthState
}

