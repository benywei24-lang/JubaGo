package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.JubaGoRepository
import com.example.data.sync.RoomFirestoreSyncHelper
import com.example.data.sync.SyncStatus
import com.example.data.auth.UserAuthenticationProvider
import com.example.model.AppLanguage
import com.example.model.AuthState
import com.example.model.AuditLogEntry
import com.example.model.ChatMessage
import com.example.model.DeliveryStatus
import com.example.model.MerchantAnalytics
import com.example.model.OrderBooking
import com.example.model.PaymentMethod
import com.example.model.Review
import com.example.model.ServiceCategory
import com.example.model.ServiceItem
import com.example.model.UserProfile
import com.example.model.UserRole
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JubaGoViewModel(
    application: Application,
    private val repository: JubaGoRepository = JubaGoRepository(application.applicationContext)
) : AndroidViewModel(application) {

    val authProvider: UserAuthenticationProvider = UserAuthenticationProvider(application.applicationContext)
    val authState: StateFlow<AuthState> = authProvider.authState
    val currentUser: StateFlow<UserProfile?> = authProvider.currentUser

    val userProfileRepo: com.example.data.user.UserProfileRepository =
        com.example.data.user.UserProfileRepository(application.applicationContext, authProvider)

    val deliveryAddresses: StateFlow<List<com.example.model.DeliveryAddress>> = userProfileRepo.addresses
    val savedPaymentMethods: StateFlow<List<com.example.model.SavedPaymentMethod>> = userProfileRepo.paymentMethods

    fun saveDeliveryAddress(address: com.example.model.DeliveryAddress) {
        userProfileRepo.saveAddress(address)
    }

    fun setDefaultDeliveryAddress(addressId: String) {
        userProfileRepo.setDefaultAddress(addressId)
    }

    fun deleteDeliveryAddress(addressId: String) {
        userProfileRepo.deleteAddress(addressId)
    }

    fun savePaymentMethod(method: com.example.model.SavedPaymentMethod) {
        userProfileRepo.savePaymentMethod(method)
    }

    fun setDefaultPaymentMethod(methodId: String) {
        userProfileRepo.setDefaultPaymentMethod(methodId)
    }

    fun deletePaymentMethod(methodId: String) {
        userProfileRepo.deletePaymentMethod(methodId)
    }

    fun updateAccountDetails(displayName: String, phoneNumber: String, cityAndCountry: String) {
        viewModelScope.launch {
            userProfileRepo.updateAccountDetails(displayName, phoneNumber, cityAndCountry)
        }
    }

    private val _showAuthDialog = MutableStateFlow(false)
    val showAuthDialog: StateFlow<Boolean> = _showAuthDialog.asStateFlow()

    fun openAuthDialog() {
        _showAuthDialog.value = true
    }

    fun closeAuthDialog() {
        _showAuthDialog.value = false
    }

    // Language State & Persistence
    private val langPrefs = application.getSharedPreferences("jubago_language_prefs", Context.MODE_PRIVATE)
    private val savedLangCode = langPrefs.getString("selected_language", AppLanguage.ENGLISH.code) ?: AppLanguage.ENGLISH.code
    private val initialLang = AppLanguage.values().firstOrNull { it.code == savedLangCode } ?: AppLanguage.ENGLISH

    private val _currentLanguage = MutableStateFlow(initialLang)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _showLanguageDialog = MutableStateFlow(false)
    val showLanguageDialog: StateFlow<Boolean> = _showLanguageDialog.asStateFlow()

    init {
        com.example.data.localization.LocalizationManager.applyLocale(application, initialLang)
    }

    fun openLanguageDialog() {
        _showLanguageDialog.value = true
    }

    fun closeLanguageDialog() {
        _showLanguageDialog.value = false
    }

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
        langPrefs.edit().putString("selected_language", language.code).apply()
        com.example.data.localization.LocalizationManager.applyLocale(getApplication<Application>(), language)
    }

    // Onboarding Wizard State (persisted via SharedPreferences)
    private val onboardingPrefs = getApplication<Application>().getSharedPreferences("jubago_onboarding_prefs", Context.MODE_PRIVATE)
    private val _showOnboardingWizard = MutableStateFlow(!onboardingPrefs.getBoolean("has_completed_onboarding", false))
    val showOnboardingWizard: StateFlow<Boolean> = _showOnboardingWizard.asStateFlow()

    fun openOnboardingWizard() {
        _showOnboardingWizard.value = true
    }

    fun closeOnboardingWizard() {
        onboardingPrefs.edit().putBoolean("has_completed_onboarding", true).apply()
        _showOnboardingWizard.value = false
    }

    val syncHelper: RoomFirestoreSyncHelper? = repository.syncHelper
    val syncStatus: StateFlow<SyncStatus>? = repository.syncHelper?.syncStatus

    val currentRole: StateFlow<UserRole> = repository.currentRole
    val isOfflineMode: StateFlow<Boolean> = repository.isOfflineMode
    val notifications: StateFlow<List<String>> = repository.notifications
    val fcmToken: StateFlow<String?> = com.example.data.notification.FcmNotificationManager.currentToken
    val recentFcmNotifications: StateFlow<List<com.example.data.notification.PushNotificationPayload>> =
        com.example.data.notification.FcmNotificationManager.recentNotifications

    fun triggerTestPushNotification(
        type: com.example.data.notification.PushNotificationType,
        title: String,
        body: String,
        orderId: String? = null
    ) {
        val app = getApplication<Application>()
        com.example.data.notification.FcmNotificationManager.triggerSimulatedNotification(
            context = app,
            type = type,
            title = title,
            body = body,
            orderId = orderId
        )
        repository.addNotification("[$title] $body")
    }

    fun clearNotifications() {
        repository.clearNotifications()
        com.example.data.notification.FcmNotificationManager.clearRecentNotifications()
    }

    val orders: StateFlow<List<OrderBooking>> = repository.orders
    val selectedOrder: StateFlow<OrderBooking?> = repository.selectedOrder
    val reviews: StateFlow<List<Review>> = repository.reviews
    val chatMessages: StateFlow<List<ChatMessage>> = repository.chatMessages
    val auditLogs: StateFlow<List<AuditLogEntry>> = repository.auditLogs

    // Filters and search
    private val _selectedCategory = MutableStateFlow(ServiceCategory.ALL)
    val selectedCategory: StateFlow<ServiceCategory> = _selectedCategory.asStateFlow()

    private val _selectedScope = MutableStateFlow(com.example.model.CoverageScope.ALL_REGIONS)
    val selectedScope: StateFlow<com.example.model.CoverageScope> = _selectedScope.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _onlyVerified = MutableStateFlow(false)
    val onlyVerified: StateFlow<Boolean> = _onlyVerified.asStateFlow()

    val favoriteIds: StateFlow<Set<String>> = repository.favoriteIds
    private val _onlyFavorites = MutableStateFlow(false)
    val onlyFavorites: StateFlow<Boolean> = _onlyFavorites.asStateFlow()

    fun toggleOnlyFavorites() {
        _onlyFavorites.value = !_onlyFavorites.value
    }

    fun toggleFavorite(serviceId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(serviceId)
        }
    }

    private val _showBusinessRegistration = MutableStateFlow(false)
    val showBusinessRegistration: StateFlow<Boolean> = _showBusinessRegistration.asStateFlow()

    // Active bottom navigation destination
    // 0: Explore / Bookings, 1: Live Tracking, 2: Support Chat, 3: Orders, 4: Merchant Dashboard
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Active booking modal service item
    private val _bookingItem = MutableStateFlow<ServiceItem?>(null)
    val bookingItem: StateFlow<ServiceItem?> = _bookingItem.asStateFlow()

    // Service detail item
    private val _detailItem = MutableStateFlow<ServiceItem?>(null)
    val detailItem: StateFlow<ServiceItem?> = _detailItem.asStateFlow()

    // Filtered services
    val filteredServices: StateFlow<List<ServiceItem>> = combine(
        repository.services,
        combine(_selectedCategory, _selectedScope, _searchQuery) { cat, scope, q ->
            Triple(cat, scope, q)
        },
        combine(_onlyVerified, _onlyFavorites, repository.favoriteIds) { verified, favOnly, favIds ->
            Triple(verified, favOnly, favIds)
        }
    ) { allServices, (cat, scope, q), (verified, favOnly, favIds) ->
        allServices.filter { item ->
            val matchesCategory = (cat == ServiceCategory.ALL) || (item.category == cat)
            val matchesScope = (scope == com.example.model.CoverageScope.ALL_REGIONS) || (item.coverageScope == scope)
            val trimmedQ = q.trim()
            val matchesQuery = trimmedQ.isBlank() || run {
                val qLower = trimmedQ.lowercase()
                val isTravelIntent = qLower.contains("travel") || qLower.contains("trip") ||
                        qLower.contains("transport") || qLower.contains("car") ||
                        qLower.contains("ride") || qLower.contains("flight") ||
                        qLower.contains("hotel") || qLower.contains("stay")
                val isFoodIntent = qLower.contains("food") || qLower.contains("eat") ||
                        qLower.contains("restaurant") || qLower.contains("dining") ||
                        qLower.contains("grocery") || qLower.contains("meal")

                val matchesKeywords = item.title.contains(trimmedQ, ignoreCase = true) ||
                        item.providerName.contains(trimmedQ, ignoreCase = true) ||
                        item.location.contains(trimmedQ, ignoreCase = true) ||
                        item.description.contains(trimmedQ, ignoreCase = true) ||
                        item.category.displayName.contains(trimmedQ, ignoreCase = true) ||
                        item.tags.any { it.contains(trimmedQ, ignoreCase = true) }

                val matchesTravel = isTravelIntent && item.category in listOf(
                    ServiceCategory.CARS,
                    ServiceCategory.FLIGHTS,
                    ServiceCategory.HOTELS
                )
                val matchesFood = isFoodIntent && item.category in listOf(
                    ServiceCategory.FOOD,
                    ServiceCategory.GROCERY
                )

                matchesKeywords || matchesTravel || matchesFood
            }
            val matchesVerified = !verified || item.isVerified
            val matchesFavorites = !favOnly || favIds.contains(item.id)
            matchesCategory && matchesScope && matchesQuery && matchesVerified && matchesFavorites
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, repository.services.value)

    // Merchant analytics
    private val _analytics = MutableStateFlow(repository.getMerchantAnalytics())
    val analytics: StateFlow<MerchantAnalytics> = _analytics.asStateFlow()

    // Show Notification Dialog
    private val _showNotificationsSheet = MutableStateFlow(false)
    val showNotificationsSheet: StateFlow<Boolean> = _showNotificationsSheet.asStateFlow()

    // Success dialog for bookings
    private val _lastPlacedOrder = MutableStateFlow<OrderBooking?>(null)
    val lastPlacedOrder: StateFlow<OrderBooking?> = _lastPlacedOrder.asStateFlow()

    init {
        // Periodic subtle live tracking position simulator for courier on the way
        viewModelScope.launch {
            while (true) {
                delay(4000)
                val current = selectedOrder.value
                if (current != null && current.status == DeliveryStatus.ON_THE_WAY && current.liveProgress < 0.95f) {
                    val newProgress = (current.liveProgress + 0.05f).coerceAtMost(0.95f)
                    repository.selectOrderForTracking(current.copy(liveProgress = newProgress))
                }
            }
        }
    }

    fun setCategory(category: ServiceCategory) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleOnlyVerified() {
        _onlyVerified.value = !_onlyVerified.value
    }

    fun setCoverageScope(scope: com.example.model.CoverageScope) {
        _selectedScope.value = scope
    }

    fun openBusinessRegistration() {
        _showBusinessRegistration.value = true
    }

    fun closeBusinessRegistration() {
        _showBusinessRegistration.value = false
    }

    fun registerBusiness(request: com.example.model.BusinessRegistrationRequest) {
        repository.registerBusiness(request)
        _showBusinessRegistration.value = false
        refreshAnalytics()
    }

    fun registerVendorWithFirestore(
        request: com.example.model.BusinessRegistrationRequest,
        onResult: (success: Boolean, message: String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.saveVendorDirectlyToFirestore(request)
            refreshAnalytics()
            onResult(result.first, result.second)
        }
    }

    fun setTab(index: Int) {
        _selectedTab.value = index
    }

    fun openServiceDetail(item: ServiceItem) {
        _detailItem.value = item
    }

    fun closeServiceDetail() {
        _detailItem.value = null
    }

    fun startBooking(item: ServiceItem) {
        _bookingItem.value = item
    }

    fun dismissBooking() {
        _bookingItem.value = null
    }

    fun toggleNotifications(show: Boolean) {
        _showNotificationsSheet.value = show
    }

    // Courier Driver Fleet Mode State
    private val _isCourierOnline = MutableStateFlow(true)
    val isCourierOnline: StateFlow<Boolean> = _isCourierOnline.asStateFlow()

    fun toggleCourierOnline() {
        _isCourierOnline.value = !_isCourierOnline.value
    }

    private val _showQrScanner = MutableStateFlow(false)
    val showQrScanner: StateFlow<Boolean> = _showQrScanner.asStateFlow()

    private val _activeScanningOrderId = MutableStateFlow<String?>(null)
    val activeScanningOrderId: StateFlow<String?> = _activeScanningOrderId.asStateFlow()

    private val _verificationSuccessMessage = MutableStateFlow<String?>(null)
    val verificationSuccessMessage: StateFlow<String?> = _verificationSuccessMessage.asStateFlow()

    fun openQrScanner(forOrderId: String? = null) {
        _activeScanningOrderId.value = forOrderId
        _showQrScanner.value = true
    }

    fun closeQrScanner() {
        _showQrScanner.value = false
        _activeScanningOrderId.value = null
    }

    fun clearVerificationSuccess() {
        _verificationSuccessMessage.value = null
    }

    fun verifyOrderDelivery(orderId: String, scannedPayload: String): Boolean {
        val targetOrder = repository.orders.value.firstOrNull { 
            it.id == orderId || scannedPayload.contains(it.id) || scannedPayload.contains(orderId)
        }
        if (targetOrder != null) {
            repository.updateOrderStatus(targetOrder.id, DeliveryStatus.DELIVERED)
            _verificationSuccessMessage.value = "Order #${targetOrder.id} successfully verified & delivered via QR verification!"
            closeQrScanner()
            refreshAnalytics()
            return true
        }
        return false
    }

    fun acceptCourierJob(orderId: String) {
        repository.updateOrderStatus(orderId, DeliveryStatus.ON_THE_WAY)
        refreshAnalytics()
    }

    fun declineCourierJob(orderId: String) {
        refreshAnalytics()
    }

    fun switchRole(role: UserRole) {
        authProvider.switchRole(role)
        repository.switchRole(role)
        if (role == UserRole.MERCHANT || role == UserRole.COURIER) {
            _selectedTab.value = 4 // Navigate to Merchant/Courier Operations Dashboard
            refreshAnalytics()
        } else {
            _selectedTab.value = 0
        }
    }

    fun toggleOfflineMode() {
        repository.toggleOfflineMode()
    }

    fun submitBooking(
        customerName: String,
        customerPhone: String,
        deliveryAddress: String,
        paymentMethod: PaymentMethod
    ) {
        val item = _bookingItem.value ?: return
        val order = repository.createOrder(
            service = item,
            customerName = customerName,
            customerPhone = customerPhone,
            deliveryAddress = deliveryAddress,
            paymentMethod = paymentMethod
        )
        _lastPlacedOrder.value = order
        _bookingItem.value = null
        _detailItem.value = null
        refreshAnalytics()
    }

    fun dismissSuccessDialog(navigateToTracking: Boolean = true) {
        _lastPlacedOrder.value = null
        if (navigateToTracking) {
            _selectedTab.value = 1 // Live tracking tab
        }
    }

    fun selectOrderToTrack(order: OrderBooking) {
        repository.selectOrderForTracking(order)
        _selectedTab.value = 1
    }

    fun advanceDriverLocation(orderId: String) {
        repository.advanceLiveDriverProgress(orderId)
    }

    fun sendChatMessage(text: String) {
        repository.sendChatMessage(text)
    }

    private val _isRefreshingOrders = MutableStateFlow(false)
    val isRefreshingOrders: StateFlow<Boolean> = _isRefreshingOrders.asStateFlow()

    private val _orderHistoryMessage = MutableStateFlow<String?>(null)
    val orderHistoryMessage: StateFlow<String?> = _orderHistoryMessage.asStateFlow()

    fun clearOrderHistoryMessage() {
        _orderHistoryMessage.value = null
    }

    private val _activeReviewOrder = MutableStateFlow<OrderBooking?>(null)
    val activeReviewOrder: StateFlow<OrderBooking?> = _activeReviewOrder.asStateFlow()

    fun openReviewForOrder(order: OrderBooking) {
        _activeReviewOrder.value = order
    }

    fun closeReviewDialog() {
        _activeReviewOrder.value = null
    }

    fun reorderBooking(order: OrderBooking, onComplete: ((OrderBooking) -> Unit)? = null) {
        val reordered = repository.reorderBooking(order)
        _lastPlacedOrder.value = reordered
        _orderHistoryMessage.value = "Order #${reordered.id} successfully placed & synced to Firestore!"
        refreshAnalytics()
        onComplete?.invoke(reordered)
    }

    fun refreshOrdersFromFirestore(onResult: ((Boolean, String) -> Unit)? = null) {
        viewModelScope.launch {
            _isRefreshingOrders.value = true
            val res = repository.refreshOrdersFromFirestore()
            _isRefreshingOrders.value = false
            _orderHistoryMessage.value = res.second
            onResult?.invoke(res.first, res.second)
            refreshAnalytics()
        }
    }

    fun submitOrderReview(
        order: OrderBooking,
        rating: Double,
        comment: String,
        tags: List<String> = emptyList(),
        authorName: String = ""
    ) {
        val reviewerName = authorName.ifBlank {
            authProvider.currentUser.value?.displayName ?: "Verified Resident"
        }
        repository.addReview(
            serviceId = order.serviceId,
            authorName = reviewerName,
            rating = rating,
            comment = comment,
            orderId = order.id,
            tags = tags
        )
        _orderHistoryMessage.value = "Review submitted! Rating synced with Firestore."
        _activeReviewOrder.value = null
        refreshAnalytics()
    }

    fun submitReview(serviceId: String, name: String, rating: Double, comment: String) {
        repository.addReview(serviceId, name, rating, comment)
    }

    fun toggleMerchantServiceAvailability(serviceId: String) {
        repository.toggleServiceAvailability(serviceId)
        refreshAnalytics()
    }

    fun updateOrderStatus(orderId: String, newStatus: DeliveryStatus) {
        repository.updateOrderStatus(orderId, newStatus)
        refreshAnalytics()
    }

    fun refreshAnalytics() {
        _analytics.value = repository.getMerchantAnalytics()
    }

    fun getExportReportCsv(): String {
        return repository.generateReportCsv()
    }
}
