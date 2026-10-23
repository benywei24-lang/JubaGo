package com.example.data.sync

import android.content.Context
import android.util.Log
import com.example.data.local.OrderDao
import com.example.data.local.OrderEntity
import com.example.model.BusinessRegistrationRequest
import com.example.model.DeliveryStatus
import com.example.model.PaymentMethod
import com.example.model.Review
import com.example.model.ServiceCategory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

enum class SyncStatus {
    IDLE,
    SYNCING,
    SYNCED,
    OFFLINE,
    ERROR
}

/**
 * Room-to-Firestore synchronization helper class.
 * Ensures seamless bidirectional synchronization between local Room storage
 * and cloud Firebase Firestore to support full offline functionality in Juba.
 */
class RoomFirestoreSyncHelper(
    private val orderDao: OrderDao,
    private val context: Context,
    private val firestore: FirebaseFirestore? = null
) {
    private val TAG = "JubaGoSync"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _pendingSyncCount = MutableStateFlow(0)
    val pendingSyncCount: StateFlow<Int> = _pendingSyncCount.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow(0L)
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

    private var firestoreListener: ListenerRegistration? = null

    // Safe accessor for Firestore instance
    private fun getFirestoreInstance(): FirebaseFirestore? {
        if (firestore != null) return firestore
        return try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseFirestore instance not ready or unprovisioned: ${e.message}")
            null
        }
    }

    /**
     * Start observing Firestore for incoming changes from other devices/merchants,
     * and periodically synchronize pending local Room records.
     */
    fun startObservingAndSync() {
        startFirestoreListener()
        triggerSync()
    }

    /**
     * Listen to remote Firestore collection changes and merge into local Room database.
     */
    private fun startFirestoreListener() {
        val db = getFirestoreInstance() ?: return

        try {
            firestoreListener?.remove()
            firestoreListener = db.collection("orders")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Firestore listen error (running offline mode): ${error.message}")
                        _syncStatus.value = SyncStatus.OFFLINE
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        scope.launch {
                            try {
                                val entitiesToUpsert = mutableListOf<OrderEntity>()
                                for (doc in snapshot.documents) {
                                    val data = doc.data ?: continue
                                    val entity = OrderEntity(
                                        id = doc.id,
                                        serviceId = data["serviceId"] as? String ?: "",
                                        title = data["title"] as? String ?: "Order",
                                        categoryName = data["categoryName"] as? String ?: ServiceCategory.FOOD.name,
                                        providerName = data["providerName"] as? String ?: "Provider",
                                        customerName = data["customerName"] as? String ?: "Resident",
                                        customerPhone = data["customerPhone"] as? String ?: "",
                                        deliveryAddress = data["deliveryAddress"] as? String ?: "Juba",
                                        totalPriceSsp = (data["totalPriceSsp"] as? Number)?.toLong() ?: 0L,
                                        totalPriceUsd = (data["totalPriceUsd"] as? Number)?.toDouble() ?: 0.0,
                                        paymentMethodName = data["paymentMethodName"] as? String ?: PaymentMethod.M_GURUSH.name,
                                        paymentReference = data["paymentReference"] as? String ?: "",
                                        statusName = data["statusName"] as? String ?: DeliveryStatus.CONFIRMED.name,
                                        courierName = data["courierName"] as? String ?: "Courier",
                                        courierPhone = data["courierPhone"] as? String ?: "",
                                        courierVehicle = data["courierVehicle"] as? String ?: "Boda",
                                        courierRating = (data["courierRating"] as? Number)?.toDouble() ?: 4.9,
                                        orderedAt = data["orderedAt"] as? String ?: "Today",
                                        etaMinutes = (data["etaMinutes"] as? Number)?.toInt() ?: 10,
                                        liveProgress = (data["liveProgress"] as? Number)?.toFloat() ?: 0.5f,
                                        riderLocation = data["riderLocation"] as? String ?: "Juba",
                                        isSynced = true,
                                        updatedAtTimestamp = (data["updatedAtTimestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
                                    )
                                    entitiesToUpsert.add(entity)
                                }

                                if (entitiesToUpsert.isNotEmpty()) {
                                    orderDao.insertOrders(entitiesToUpsert)
                                    _syncStatus.value = SyncStatus.SYNCED
                                    _lastSyncTimestamp.value = System.currentTimeMillis()
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error caching Firestore documents into Room", e)
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to attach Firestore snapshot listener: ${e.message}")
            _syncStatus.value = SyncStatus.OFFLINE
        }
    }

    /**
     * Manually triggers a push sync: reads all unsynced records from Room
     * and uploads them to Firestore, marking them as synced locally.
     */
    fun triggerSync() {
        scope.launch {
            val unsynced = orderDao.getUnsyncedOrders()
            _pendingSyncCount.value = unsynced.size

            if (unsynced.isEmpty()) {
                _syncStatus.value = SyncStatus.SYNCED
                return@launch
            }

            val db = getFirestoreInstance()
            if (db == null) {
                _syncStatus.value = SyncStatus.OFFLINE
                return@launch
            }

            _syncStatus.value = SyncStatus.SYNCING
            try {
                for (order in unsynced) {
                    val map = hashMapOf(
                        "serviceId" to order.serviceId,
                        "title" to order.title,
                        "categoryName" to order.categoryName,
                        "providerName" to order.providerName,
                        "customerName" to order.customerName,
                        "customerPhone" to order.customerPhone,
                        "deliveryAddress" to order.deliveryAddress,
                        "totalPriceSsp" to order.totalPriceSsp,
                        "totalPriceUsd" to order.totalPriceUsd,
                        "paymentMethodName" to order.paymentMethodName,
                        "paymentReference" to order.paymentReference,
                        "statusName" to order.statusName,
                        "courierName" to order.courierName,
                        "courierPhone" to order.courierPhone,
                        "courierVehicle" to order.courierVehicle,
                        "courierRating" to order.courierRating,
                        "orderedAt" to order.orderedAt,
                        "etaMinutes" to order.etaMinutes,
                        "liveProgress" to order.liveProgress,
                        "riderLocation" to order.riderLocation,
                        "updatedAtTimestamp" to order.updatedAtTimestamp
                    )

                    db.collection("orders").document(order.id)
                        .set(map, SetOptions.merge())
                        .await()

                    orderDao.markAsSynced(order.id)
                }

                _pendingSyncCount.value = 0
                _syncStatus.value = SyncStatus.SYNCED
                _lastSyncTimestamp.value = System.currentTimeMillis()
                Log.i(TAG, "Successfully synced ${unsynced.size} orders from Room to Firestore")
            } catch (e: Exception) {
                Log.w(TAG, "Sync to Firestore failed (offline or network issue): ${e.message}")
                _syncStatus.value = SyncStatus.OFFLINE
            }
        }
    }

    /**
     * Explicitly queries and retrieves all past orders directly from Cloud Firestore,
     * updating local Room cache and providing live sync feedback.
     */
    suspend fun fetchOrdersFromFirestore(): Pair<Boolean, String> {
        val db = getFirestoreInstance()
        if (db == null) {
            _syncStatus.value = SyncStatus.OFFLINE
            return Pair(false, "Firestore offline / local cache active")
        }

        return try {
            _syncStatus.value = SyncStatus.SYNCING
            val snapshot = db.collection("orders").get().await()
            val entitiesToUpsert = mutableListOf<OrderEntity>()
            for (doc in snapshot.documents) {
                val data = doc.data ?: continue
                val entity = OrderEntity(
                    id = doc.id,
                    serviceId = data["serviceId"] as? String ?: "",
                    title = data["title"] as? String ?: "Order",
                    categoryName = data["categoryName"] as? String ?: ServiceCategory.FOOD.name,
                    providerName = data["providerName"] as? String ?: "Provider",
                    customerName = data["customerName"] as? String ?: "Resident",
                    customerPhone = data["customerPhone"] as? String ?: "",
                    deliveryAddress = data["deliveryAddress"] as? String ?: "Juba",
                    totalPriceSsp = (data["totalPriceSsp"] as? Number)?.toLong() ?: 0L,
                    totalPriceUsd = (data["totalPriceUsd"] as? Number)?.toDouble() ?: 0.0,
                    paymentMethodName = data["paymentMethodName"] as? String ?: PaymentMethod.M_GURUSH.name,
                    paymentReference = data["paymentReference"] as? String ?: "",
                    statusName = data["statusName"] as? String ?: DeliveryStatus.CONFIRMED.name,
                    courierName = data["courierName"] as? String ?: "Courier",
                    courierPhone = data["courierPhone"] as? String ?: "",
                    courierVehicle = data["courierVehicle"] as? String ?: "Boda",
                    courierRating = (data["courierRating"] as? Number)?.toDouble() ?: 4.9,
                    orderedAt = data["orderedAt"] as? String ?: "Today",
                    etaMinutes = (data["etaMinutes"] as? Number)?.toInt() ?: 10,
                    liveProgress = (data["liveProgress"] as? Number)?.toFloat() ?: 0.5f,
                    riderLocation = data["riderLocation"] as? String ?: "Juba",
                    isSynced = true,
                    updatedAtTimestamp = (data["updatedAtTimestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
                )
                entitiesToUpsert.add(entity)
            }
            if (entitiesToUpsert.isNotEmpty()) {
                orderDao.insertOrders(entitiesToUpsert)
            }
            _syncStatus.value = SyncStatus.SYNCED
            _lastSyncTimestamp.value = System.currentTimeMillis()
            Pair(true, "Retrieved ${snapshot.size()} orders from Cloud Firestore")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch orders from Firestore: ${e.message}")
            _syncStatus.value = SyncStatus.OFFLINE
            Pair(false, e.message ?: "Failed to query Cloud Firestore")
        }
    }

    /**
     * Save an order locally in Room first (offline immediate write),
     * and queue for background sync to Firestore.
     */
    suspend fun saveOrderOfflineFirst(entity: OrderEntity) {
        orderDao.insertOrder(entity)
        _pendingSyncCount.value = _pendingSyncCount.value + 1
        triggerSync()
    }

    /**
     * Directly persists a vendor registration record to Cloud Firestore collections
     * ("vendors" and "services") for instant visibility across South Sudan and beyond.
     */
    suspend fun saveVendorToFirestore(request: BusinessRegistrationRequest): Pair<Boolean, String> {
        val db = getFirestoreInstance()
        if (db == null) {
            Log.w(TAG, "Cannot save vendor to Firestore: Firestore instance is null / unprovisioned.")
            return Pair(false, "Firestore instance not available; saved to local cache")
        }

        return try {
            val vendorId = request.id.ifBlank { "vnd_${System.currentTimeMillis()}" }
            val vendorMap = hashMapOf<String, Any>(
                "id" to vendorId,
                "businessName" to request.businessName,
                "businessType" to request.businessType,
                "category" to request.category.name,
                "categoryDisplayName" to request.category.displayName,
                "coverageScope" to request.coverageScope.name,
                "coverageScopeDisplayName" to request.coverageScope.displayName,
                "cityAndCountry" to request.cityAndCountry,
                "physicalAddress" to request.physicalAddress,
                "contactPhone" to request.contactPhone,
                "contactEmail" to request.contactEmail,
                "registrationTaxNumber" to request.registrationTaxNumber,
                "serviceOfferingTitle" to request.serviceOfferingTitle,
                "serviceDescription" to request.serviceDescription,
                "priceSsp" to request.priceSsp,
                "priceUsd" to request.priceUsd,
                "priceUnit" to request.priceUnit,
                "isVerified" to request.isVerified,
                "registeredAt" to request.registeredAt,
                "status" to "ACTIVE",
                "updatedAtTimestamp" to System.currentTimeMillis()
            )

            // Save to "vendors" collection
            db.collection("vendors").document(vendorId)
                .set(vendorMap, SetOptions.merge())
                .await()

            // Also mirror to "services" collection for customer discovery
            db.collection("services").document(vendorId)
                .set(vendorMap, SetOptions.merge())
                .await()

            _syncStatus.value = SyncStatus.SYNCED
            _lastSyncTimestamp.value = System.currentTimeMillis()
            Log.i(TAG, "Vendor ${request.businessName} ($vendorId) saved to Cloud Firestore successfully")
            Pair(true, "Vendor successfully registered and synced with Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write vendor to Firestore: ${e.message}", e)
            _syncStatus.value = SyncStatus.OFFLINE
            Pair(false, e.message ?: "Failed to write vendor to Firestore")
        }
    }

    /**
     * Persists a customer review with star-rating and feedback tags to Cloud Firestore "reviews" collection.
     */
    suspend fun saveReviewToFirestore(review: Review): Pair<Boolean, String> {
        val db = getFirestoreInstance()
        if (db == null) {
            Log.w(TAG, "Cannot save review to Firestore: instance unavailable")
            return Pair(false, "Firestore instance not available; review stored locally")
        }

        return try {
            val reviewId = review.id.ifBlank { "rev_${System.currentTimeMillis()}" }
            val map = hashMapOf<String, Any>(
                "id" to reviewId,
                "serviceId" to review.serviceId,
                "orderId" to (review.orderId ?: ""),
                "authorName" to review.authorName,
                "rating" to review.rating,
                "date" to review.date,
                "comment" to review.comment,
                "tags" to review.tags,
                "isVerifiedBuyer" to review.isVerifiedBuyer,
                "timestamp" to review.timestamp
            )

            db.collection("reviews").document(reviewId)
                .set(map, SetOptions.merge())
                .await()

            _syncStatus.value = SyncStatus.SYNCED
            _lastSyncTimestamp.value = System.currentTimeMillis()
            Log.i(TAG, "Review $reviewId successfully pushed to Cloud Firestore reviews collection")
            Pair(true, "Review submitted and stored in Cloud Firestore")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to write review to Firestore: ${e.message}")
            _syncStatus.value = SyncStatus.OFFLINE
            Pair(false, e.message ?: "Failed to write review to Firestore")
        }
    }

    /**
     * Queries all reviews from Cloud Firestore "reviews" collection.
     */
    suspend fun fetchReviewsFromFirestore(): Pair<Boolean, List<Review>> {
        val db = getFirestoreInstance()
        if (db == null) return Pair(false, emptyList())

        return try {
            val snapshot = db.collection("reviews").get().await()
            val list = mutableListOf<Review>()
            for (doc in snapshot.documents) {
                val data = doc.data ?: continue
                @Suppress("UNCHECKED_CAST")
                val tags = (data["tags"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                val rev = Review(
                    id = doc.id,
                    serviceId = data["serviceId"] as? String ?: "",
                    orderId = (data["orderId"] as? String)?.ifEmpty { null },
                    authorName = data["authorName"] as? String ?: "Resident",
                    rating = (data["rating"] as? Number)?.toDouble() ?: 5.0,
                    date = data["date"] as? String ?: "Recently",
                    comment = data["comment"] as? String ?: "",
                    tags = tags,
                    isVerifiedBuyer = data["isVerifiedBuyer"] as? Boolean ?: true,
                    timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
                )
                list.add(rev)
            }
            Pair(true, list)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch reviews from Firestore: ${e.message}")
            Pair(false, emptyList())
        }
    }

    /**
     * Cleanup listener on teardown.
     */
    fun stop() {
        firestoreListener?.remove()
        firestoreListener = null
    }
}
