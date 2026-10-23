package com.example.data.user

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.auth.UserAuthenticationProvider
import com.example.model.DeliveryAddress
import com.example.model.PaymentMethod
import com.example.model.SavedPaymentMethod
import com.example.model.UserProfile
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

class UserProfileRepository(
    private val context: Context,
    private val authProvider: UserAuthenticationProvider
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val prefs: SharedPreferences = context.getSharedPreferences("jubago_user_profile_prefs", Context.MODE_PRIVATE)

    private val firestore: FirebaseFirestore? by lazy {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "Firestore initialization fallback: ${e.message}")
            null
        }
    }

    private val defaultAddresses = listOf(
        DeliveryAddress(
            id = "addr_1",
            label = "Home",
            streetAddress = "Block 3, House 14",
            area = "Hai Cinema",
            city = "Juba, South Sudan",
            landmark = "Near St. Joseph Church",
            contactPhone = "+211 92 123 4567",
            isDefault = true
        ),
        DeliveryAddress(
            id = "addr_2",
            label = "Office",
            streetAddress = "Ministry Complex, Tower B",
            area = "Ministries Road",
            city = "Juba, South Sudan",
            landmark = "Opposite Custom Roundabout",
            contactPhone = "+211 91 888 7766",
            isDefault = false
        ),
        DeliveryAddress(
            id = "addr_3",
            label = "Market Shop",
            streetAddress = "Stall 42, Central Fresh Arcade",
            area = "Konyo Konyo",
            city = "Juba, South Sudan",
            landmark = "Near Nile River Bank Gate",
            contactPhone = "+211 98 765 4321",
            isDefault = false
        )
    )

    private val defaultPaymentMethods = listOf(
        SavedPaymentMethod(
            id = "pm_1",
            method = PaymentMethod.M_GURUSH,
            title = "m-GURUSH Primary Wallet",
            accountIdentifier = "+211 92 123 4567",
            accountHolder = "Deng Majok",
            isDefault = true
        ),
        SavedPaymentMethod(
            id = "pm_2",
            method = PaymentMethod.MTN_MOMO,
            title = "MTN MoMo Express",
            accountIdentifier = "+211 91 888 7766",
            accountHolder = "Deng Majok",
            isDefault = false
        ),
        SavedPaymentMethod(
            id = "pm_3",
            method = PaymentMethod.BANK_CARD,
            title = "Visa Debit Card",
            accountIdentifier = "**** **** **** 4242",
            accountHolder = "Deng Majok",
            isDefault = false,
            expiryDate = "10/28"
        )
    )

    private val _addresses = MutableStateFlow<List<DeliveryAddress>>(loadAddressesFromPrefs())
    val addresses: StateFlow<List<DeliveryAddress>> = _addresses.asStateFlow()

    private val _paymentMethods = MutableStateFlow<List<SavedPaymentMethod>>(loadPaymentMethodsFromPrefs())
    val paymentMethods: StateFlow<List<SavedPaymentMethod>> = _paymentMethods.asStateFlow()

    init {
        // Observe auth changes to sync Firestore data for signed in user
        scope.launch {
            authProvider.currentUser.collect { user ->
                if (user != null) {
                    syncWithFirestore(user.uid)
                }
            }
        }
    }

    private fun syncWithFirestore(userId: String) {
        val db = firestore ?: return
        scope.launch {
            try {
                // Fetch addresses from users/{uid}/addresses
                val addrDocs = db.collection("users").document(userId)
                    .collection("addresses").get().await()

                if (!addrDocs.isEmpty) {
                    val remoteAddresses = addrDocs.documents.mapNotNull { doc ->
                        val data = doc.data ?: return@mapNotNull null
                        DeliveryAddress(
                            id = doc.id,
                            label = data["label"] as? String ?: "Address",
                            streetAddress = data["streetAddress"] as? String ?: "",
                            area = data["area"] as? String ?: "",
                            city = data["city"] as? String ?: "Juba, South Sudan",
                            landmark = data["landmark"] as? String ?: "",
                            contactPhone = data["contactPhone"] as? String ?: "",
                            isDefault = data["isDefault"] as? Boolean ?: false
                        )
                    }
                    if (remoteAddresses.isNotEmpty()) {
                        _addresses.value = remoteAddresses
                        saveAddressesToPrefs(remoteAddresses)
                    }
                }

                // Fetch payment methods from users/{uid}/payment_methods
                val pmDocs = db.collection("users").document(userId)
                    .collection("payment_methods").get().await()

                if (!pmDocs.isEmpty) {
                    val remotePms = pmDocs.documents.mapNotNull { doc ->
                        val data = doc.data ?: return@mapNotNull null
                        val methodName = data["method"] as? String ?: PaymentMethod.M_GURUSH.name
                        val method = try { PaymentMethod.valueOf(methodName) } catch (e: Exception) { PaymentMethod.M_GURUSH }
                        SavedPaymentMethod(
                            id = doc.id,
                            method = method,
                            title = data["title"] as? String ?: method.title,
                            accountIdentifier = data["accountIdentifier"] as? String ?: "",
                            accountHolder = data["accountHolder"] as? String ?: "",
                            isDefault = data["isDefault"] as? Boolean ?: false,
                            expiryDate = data["expiryDate"] as? String
                        )
                    }
                    if (remotePms.isNotEmpty()) {
                        _paymentMethods.value = remotePms
                        savePaymentMethodsToPrefs(remotePms)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to sync user data from Firestore: ${e.message}")
            }
        }
    }

    // --- Delivery Addresses Operations ---

    fun saveAddress(address: DeliveryAddress) {
        val current = _addresses.value.toMutableList()
        val index = current.indexOfFirst { it.id == address.id }

        val adjustedAddress = if (address.isDefault) {
            // Unset previous defaults
            current.map { it.copy(isDefault = false) }.toMutableList()
        } else {
            current
        }

        if (index >= 0) {
            adjustedAddress[index] = address
        } else {
            adjustedAddress.add(0, address)
        }

        // Ensure at least one address is default if list not empty
        if (adjustedAddress.isNotEmpty() && adjustedAddress.none { it.isDefault }) {
            adjustedAddress[0] = adjustedAddress[0].copy(isDefault = true)
        }

        _addresses.value = adjustedAddress
        saveAddressesToPrefs(adjustedAddress)

        // Sync to Firestore
        syncAddressToFirestore(address)
    }

    fun setDefaultAddress(addressId: String) {
        val current = _addresses.value.map { addr ->
            addr.copy(isDefault = addr.id == addressId)
        }
        _addresses.value = current
        saveAddressesToPrefs(current)

        authProvider.currentUser.value?.uid?.let { uid ->
            val db = firestore ?: return
            scope.launch {
                current.forEach { addr ->
                    try {
                        db.collection("users").document(uid)
                            .collection("addresses").document(addr.id)
                            .update("isDefault", addr.isDefault)
                    } catch (e: Exception) {
                        Log.w(TAG, "Could not update default status in Firestore: ${e.message}")
                    }
                }
            }
        }
    }

    fun deleteAddress(addressId: String) {
        val filtered = _addresses.value.filter { it.id != addressId }.toMutableList()
        if (filtered.isNotEmpty() && filtered.none { it.isDefault }) {
            filtered[0] = filtered[0].copy(isDefault = true)
        }
        _addresses.value = filtered
        saveAddressesToPrefs(filtered)

        authProvider.currentUser.value?.uid?.let { uid ->
            val db = firestore ?: return
            scope.launch {
                try {
                    db.collection("users").document(uid)
                        .collection("addresses").document(addressId).delete()
                } catch (e: Exception) {
                    Log.w(TAG, "Could not delete address from Firestore: ${e.message}")
                }
            }
        }
    }

    private fun syncAddressToFirestore(address: DeliveryAddress) {
        val uid = authProvider.currentUser.value?.uid ?: return
        val db = firestore ?: return
        scope.launch {
            try {
                val data = hashMapOf(
                    "id" to address.id,
                    "label" to address.label,
                    "streetAddress" to address.streetAddress,
                    "area" to address.area,
                    "city" to address.city,
                    "landmark" to address.landmark,
                    "contactPhone" to address.contactPhone,
                    "isDefault" to address.isDefault,
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )
                db.collection("users").document(uid)
                    .collection("addresses").document(address.id)
                    .set(data, SetOptions.merge())
            } catch (e: Exception) {
                Log.w(TAG, "Failed saving address to Firestore: ${e.message}")
            }
        }
    }

    // --- Saved Payment Methods Operations ---

    fun savePaymentMethod(method: SavedPaymentMethod) {
        val current = _paymentMethods.value.toMutableList()
        val index = current.indexOfFirst { it.id == method.id }

        val adjusted = if (method.isDefault) {
            current.map { it.copy(isDefault = false) }.toMutableList()
        } else {
            current
        }

        if (index >= 0) {
            adjusted[index] = method
        } else {
            adjusted.add(0, method)
        }

        if (adjusted.isNotEmpty() && adjusted.none { it.isDefault }) {
            adjusted[0] = adjusted[0].copy(isDefault = true)
        }

        _paymentMethods.value = adjusted
        savePaymentMethodsToPrefs(adjusted)

        syncPaymentMethodToFirestore(method)
    }

    fun setDefaultPaymentMethod(methodId: String) {
        val current = _paymentMethods.value.map { pm ->
            pm.copy(isDefault = pm.id == methodId)
        }
        _paymentMethods.value = current
        savePaymentMethodsToPrefs(current)

        authProvider.currentUser.value?.uid?.let { uid ->
            val db = firestore ?: return
            scope.launch {
                current.forEach { pm ->
                    try {
                        db.collection("users").document(uid)
                            .collection("payment_methods").document(pm.id)
                            .update("isDefault", pm.isDefault)
                    } catch (e: Exception) {
                        Log.w(TAG, "Could not update default payment in Firestore: ${e.message}")
                    }
                }
            }
        }
    }

    fun deletePaymentMethod(methodId: String) {
        val filtered = _paymentMethods.value.filter { it.id != methodId }.toMutableList()
        if (filtered.isNotEmpty() && filtered.none { it.isDefault }) {
            filtered[0] = filtered[0].copy(isDefault = true)
        }
        _paymentMethods.value = filtered
        savePaymentMethodsToPrefs(filtered)

        authProvider.currentUser.value?.uid?.let { uid ->
            val db = firestore ?: return
            scope.launch {
                try {
                    db.collection("users").document(uid)
                        .collection("payment_methods").document(methodId).delete()
                } catch (e: Exception) {
                    Log.w(TAG, "Could not delete payment method from Firestore: ${e.message}")
                }
            }
        }
    }

    private fun syncPaymentMethodToFirestore(pm: SavedPaymentMethod) {
        val uid = authProvider.currentUser.value?.uid ?: return
        val db = firestore ?: return
        scope.launch {
            try {
                val data = hashMapOf(
                    "id" to pm.id,
                    "method" to pm.method.name,
                    "title" to pm.title,
                    "accountIdentifier" to pm.accountIdentifier,
                    "accountHolder" to pm.accountHolder,
                    "isDefault" to pm.isDefault,
                    "expiryDate" to pm.expiryDate,
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )
                db.collection("users").document(uid)
                    .collection("payment_methods").document(pm.id)
                    .set(data, SetOptions.merge())
            } catch (e: Exception) {
                Log.w(TAG, "Failed saving payment method to Firestore: ${e.message}")
            }
        }
    }

    // --- Account Details Update ---

    suspend fun updateAccountDetails(
        displayName: String,
        phoneNumber: String,
        cityAndCountry: String
    ): Result<UserProfile> {
        val current = authProvider.currentUser.value
            ?: UserProfile(
                uid = "guest_user_${System.currentTimeMillis()}",
                displayName = displayName,
                phoneNumber = phoneNumber,
                cityAndCountry = cityAndCountry
            )

        val updated = current.copy(
            displayName = displayName.ifBlank { current.displayName },
            phoneNumber = phoneNumber.ifBlank { current.phoneNumber },
            cityAndCountry = cityAndCountry.ifBlank { current.cityAndCountry }
        )

        // Update Firebase Auth user profile if available
        try {
            val fbUser = FirebaseAuth.getInstance().currentUser
            if (fbUser != null && displayName.isNotBlank()) {
                fbUser.updateProfile(
                    UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName)
                        .build()
                ).await()
            }
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseAuth profile update non-fatal error: ${e.message}")
        }

        // Update Firestore
        try {
            firestore?.let { db ->
                val userData = hashMapOf(
                    "displayName" to updated.displayName,
                    "phoneNumber" to updated.phoneNumber,
                    "cityAndCountry" to updated.cityAndCountry,
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )
                db.collection("users").document(updated.uid)
                    .set(userData, SetOptions.merge())
                    .await()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firestore user profile update non-fatal error: ${e.message}")
        }

        // Update local session
        authProvider.updateLocalProfile(updated)
        return Result.success(updated)
    }

    // --- Local Persistence Helpers (JSON) ---

    private fun loadAddressesFromPrefs(): List<DeliveryAddress> {
        val raw = prefs.getString(KEY_ADDRESSES, null) ?: return defaultAddresses
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<DeliveryAddress>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    DeliveryAddress(
                        id = obj.optString("id"),
                        label = obj.optString("label"),
                        streetAddress = obj.optString("streetAddress"),
                        area = obj.optString("area"),
                        city = obj.optString("city", "Juba, South Sudan"),
                        landmark = obj.optString("landmark"),
                        contactPhone = obj.optString("contactPhone"),
                        isDefault = obj.optBoolean("isDefault", false)
                    )
                )
            }
            if (list.isEmpty()) defaultAddresses else list
        } catch (e: Exception) {
            defaultAddresses
        }
    }

    private fun saveAddressesToPrefs(list: List<DeliveryAddress>) {
        try {
            val array = JSONArray()
            list.forEach { addr ->
                val obj = JSONObject().apply {
                    put("id", addr.id)
                    put("label", addr.label)
                    put("streetAddress", addr.streetAddress)
                    put("area", addr.area)
                    put("city", addr.city)
                    put("landmark", addr.landmark)
                    put("contactPhone", addr.contactPhone)
                    put("isDefault", addr.isDefault)
                }
                array.put(obj)
            }
            prefs.edit().putString(KEY_ADDRESSES, array.toString()).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving addresses to prefs: ${e.message}")
        }
    }

    private fun loadPaymentMethodsFromPrefs(): List<SavedPaymentMethod> {
        val raw = prefs.getString(KEY_PAYMENTS, null) ?: return defaultPaymentMethods
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<SavedPaymentMethod>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val methodName = obj.optString("method", PaymentMethod.M_GURUSH.name)
                val method = try { PaymentMethod.valueOf(methodName) } catch (e: Exception) { PaymentMethod.M_GURUSH }
                list.add(
                    SavedPaymentMethod(
                        id = obj.optString("id"),
                        method = method,
                        title = obj.optString("title"),
                        accountIdentifier = obj.optString("accountIdentifier"),
                        accountHolder = obj.optString("accountHolder"),
                        isDefault = obj.optBoolean("isDefault", false),
                        expiryDate = if (obj.has("expiryDate")) obj.optString("expiryDate") else null
                    )
                )
            }
            if (list.isEmpty()) defaultPaymentMethods else list
        } catch (e: Exception) {
            defaultPaymentMethods
        }
    }

    private fun savePaymentMethodsToPrefs(list: List<SavedPaymentMethod>) {
        try {
            val array = JSONArray()
            list.forEach { pm ->
                val obj = JSONObject().apply {
                    put("id", pm.id)
                    put("method", pm.method.name)
                    put("title", pm.title)
                    put("accountIdentifier", pm.accountIdentifier)
                    put("accountHolder", pm.accountHolder)
                    put("isDefault", pm.isDefault)
                    if (pm.expiryDate != null) put("expiryDate", pm.expiryDate)
                }
                array.put(obj)
            }
            prefs.edit().putString(KEY_PAYMENTS, array.toString()).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving payment methods to prefs: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "UserProfileRepo"
        private const val KEY_ADDRESSES = "saved_delivery_addresses"
        private const val KEY_PAYMENTS = "saved_payment_methods"
    }
}
