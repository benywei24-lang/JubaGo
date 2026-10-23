package com.example.data.notification

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class JubaGoFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Token received: $token")
        FcmNotificationManager.storeToken(applicationContext, token)
        FcmNotificationManager.saveTokenToFirestore(applicationContext, token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Incoming FCM message from: ${remoteMessage.from}")

        // 1. Extract data payload
        val data = remoteMessage.data
        val typeCode = data["type"] ?: "ORDER_STATUS"
        val orderId = data["order_id"]
        val pushType = PushNotificationType.fromCode(typeCode)

        // 2. Extract title & body (check notification object, fallback to data fields)
        val title = remoteMessage.notification?.title
            ?: data["title"]
            ?: pushType.titleEn

        val body = remoteMessage.notification?.body
            ?: data["body"]
            ?: "New real-time update from JubaGo."

        Log.d(TAG, "Processing FCM push: type=$pushType, title=$title, body=$body, orderId=$orderId")

        // 3. Delegate to FcmNotificationManager to display notification in appropriate channel
        val extras = mutableMapOf<String, String>()
        extras.putAll(data)
        if (orderId != null) {
            extras["order_id"] = orderId
        }
        extras["type"] = pushType.code

        val channelId = when (pushType) {
            PushNotificationType.DELIVERY_TRACKING -> FcmNotificationManager.CHANNEL_DELIVERY_TRACKING
            PushNotificationType.PROMOTION -> FcmNotificationManager.CHANNEL_PROMOTIONS
            PushNotificationType.ORDER_STATUS -> FcmNotificationManager.CHANNEL_ORDER_UPDATES
        }

        FcmNotificationManager.showNotification(
            context = applicationContext,
            channelId = channelId,
            title = title,
            message = body,
            extras = extras
        )
    }

    companion object {
        private const val TAG = "JubaGoFcmService"
    }
}
