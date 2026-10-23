package com.example.data.notification

enum class PushNotificationType(
    val code: String,
    val titleEn: String,
    val titleAr: String,
    val defaultChannelId: String
) {
    ORDER_STATUS(
        code = "ORDER_STATUS",
        titleEn = "Order Status Update",
        titleAr = "تحديث حالة الطلب",
        defaultChannelId = FcmNotificationManager.CHANNEL_ORDER_UPDATES
    ),
    DELIVERY_TRACKING(
        code = "DELIVERY_TRACKING",
        titleEn = "Delivery & Courier Tracking",
        titleAr = "تتبع السائق والتوصيل",
        defaultChannelId = FcmNotificationManager.CHANNEL_DELIVERY_TRACKING
    ),
    PROMOTION(
        code = "PROMOTION",
        titleEn = "Promotions & Flash Deals",
        titleAr = "العروض والخصومات الخاصة",
        defaultChannelId = FcmNotificationManager.CHANNEL_PROMOTIONS
    );

    companion object {
        fun fromCode(code: String?): PushNotificationType {
            return values().firstOrNull { it.code.equals(code, ignoreCase = true) } ?: ORDER_STATUS
        }
    }
}

data class PushNotificationPayload(
    val id: String = System.currentTimeMillis().toString(),
    val type: PushNotificationType,
    val title: String,
    val body: String,
    val orderId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val clickAction: String? = null
)
