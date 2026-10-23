package com.example.data.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object FcmNotificationManager {
    private const val TAG = "FcmNotificationManager"
    private const val PREFS_NAME = "jubago_fcm_prefs"
    private const val KEY_FCM_TOKEN = "fcm_registration_token"

    const val CHANNEL_ORDER_UPDATES = "channel_order_updates"
    const val CHANNEL_DELIVERY_TRACKING = "channel_delivery_tracking"
    const val CHANNEL_PROMOTIONS = "channel_promotions"

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _currentToken = MutableStateFlow<String?>(null)
    val currentToken: StateFlow<String?> = _currentToken.asStateFlow()

    private val _recentNotifications = MutableStateFlow<List<PushNotificationPayload>>(emptyList())
    val recentNotifications: StateFlow<List<PushNotificationPayload>> = _recentNotifications.asStateFlow()

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            val orderChannel = NotificationChannel(
                CHANNEL_ORDER_UPDATES,
                "Order Status Updates",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time updates regarding order status, preparation, and ready for pickup"
                enableVibration(true)
                setShowBadge(true)
            }

            val deliveryChannel = NotificationChannel(
                CHANNEL_DELIVERY_TRACKING,
                "Delivery & Courier Progress",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Live courier location updates, ETA countdowns, and arrival alerts"
                enableVibration(true)
                setShowBadge(true)
            }

            val promoChannel = NotificationChannel(
                CHANNEL_PROMOTIONS,
                "Promotions & Special Deals",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Flash sales, discounts on Boda rides, Land Cruiser rentals, and weekend restaurant specials"
                setShowBadge(true)
            }

            notificationManager.createNotificationChannels(listOf(orderChannel, deliveryChannel, promoChannel))
            Log.d(TAG, "FCM Notification channels created successfully.")
        }
    }

    fun getStoredToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val token = prefs.getString(KEY_FCM_TOKEN, null)
        if (token != null && _currentToken.value == null) {
            _currentToken.value = token
        }
        return token
    }

    fun fetchAndStoreFcmToken(context: Context, onToken: ((String) -> Unit)? = null) {
        val stored = getStoredToken(context)
        if (!stored.isNullOrEmpty()) {
            _currentToken.value = stored
            onToken?.invoke(stored)
        }

        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        Log.d(TAG, "Firebase FCM Token retrieved: $token")
                        storeToken(context, token)
                        saveTokenToFirestore(context, token)
                        onToken?.invoke(token)
                    } else {
                        Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    }
                }
            } else {
                Log.d(TAG, "FirebaseApp not initialized, skipping FCM token fetch.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching FCM token: ${e.message}")
        }
    }

    fun storeToken(context: Context, token: String) {
        _currentToken.value = token
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_FCM_TOKEN, token).apply()
    }

    fun saveTokenToFirestore(context: Context, token: String, userId: String? = null) {
        scope.launch {
            try {
                if (FirebaseApp.getApps(context).isEmpty()) return@launch

                val firestore = FirebaseFirestore.getInstance()
                val currentUid = userId ?: try {
                    FirebaseAuth.getInstance().currentUser?.uid
                } catch (e: Exception) {
                    null
                } ?: "guest_${Build.MANUFACTURER}_${Build.MODEL}"

                val tokenData = hashMapOf(
                    "token" to token,
                    "userId" to currentUid,
                    "platform" to "Android",
                    "manufacturer" to Build.MANUFACTURER,
                    "model" to Build.MODEL,
                    "osVersion" to Build.VERSION.RELEASE,
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )

                firestore.collection("fcm_tokens")
                    .document(token.takeLast(32).ifEmpty { "token_device" })
                    .set(tokenData, SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d(TAG, "FCM token synced to Firestore for user: $currentUid")
                    }
                    .addOnFailureListener { err ->
                        Log.w(TAG, "Failed to sync FCM token to Firestore: ${err.message}")
                    }
            } catch (e: Exception) {
                Log.w(TAG, "Firestore sync skipped: ${e.message}")
            }
        }
    }

    fun showNotification(
        context: Context,
        channelId: String,
        title: String,
        message: String,
        notificationId: Int = (System.currentTimeMillis() % 100000).toInt(),
        extras: Map<String, String> = emptyMap()
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            extras.forEach { (key, value) -> putExtra(key, value) }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val smallIconRes = R.mipmap.ic_launcher

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(smallIconRes)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(
                if (channelId == CHANNEL_PROMOTIONS) NotificationCompat.PRIORITY_DEFAULT
                else NotificationCompat.PRIORITY_HIGH
            )
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if (channelId == CHANNEL_ORDER_UPDATES || channelId == CHANNEL_DELIVERY_TRACKING) {
            builder.setVibrate(longArrayOf(0, 250, 150, 250))
        }

        // Record in recent list
        val type = when (channelId) {
            CHANNEL_DELIVERY_TRACKING -> PushNotificationType.DELIVERY_TRACKING
            CHANNEL_PROMOTIONS -> PushNotificationType.PROMOTION
            else -> PushNotificationType.ORDER_STATUS
        }

        val payload = PushNotificationPayload(
            id = notificationId.toString(),
            type = type,
            title = title,
            body = message,
            orderId = extras["order_id"],
            timestamp = System.currentTimeMillis()
        )
        _recentNotifications.value = listOf(payload) + _recentNotifications.value.take(20)

        // Check permission on Android 13+ before posting
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "POST_NOTIFICATIONS permission not granted. Notification recorded in-app only.")
                return
            }
        }

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
            Log.d(TAG, "Notification posted: $title [$channelId]")
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException posting notification: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error posting notification: ${e.message}")
        }
    }

    fun triggerSimulatedNotification(
        context: Context,
        type: PushNotificationType,
        title: String,
        body: String,
        orderId: String? = null
    ) {
        val extras = mutableMapOf<String, String>("type" to type.code)
        if (orderId != null) {
            extras["order_id"] = orderId
        }

        showNotification(
            context = context,
            channelId = type.defaultChannelId,
            title = title,
            message = body,
            extras = extras
        )
    }

    fun clearRecentNotifications() {
        _recentNotifications.value = emptyList()
    }
}
