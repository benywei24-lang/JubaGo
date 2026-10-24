package com.example.data

import com.example.model.AuditLogEntry
import com.example.model.BusinessRegistrationRequest
import com.example.model.ChatMessage
import com.example.model.CoverageScope
import com.example.model.DeliveryStatus
import com.example.model.MerchantAnalytics
import com.example.model.OrderBooking
import com.example.model.PaymentMethod
import com.example.model.Review
import com.example.model.ServiceCategory
import com.example.model.ServiceItem
import com.example.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.content.Context
import com.example.data.local.JubaGoDatabase
import com.example.data.local.OrderDao
import com.example.data.local.OrderEntity
import com.example.data.sync.RoomFirestoreSyncHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class JubaGoRepository(
    private val context: Context? = null,
    private val customOrderDao: OrderDao? = null,
    private val syncHelperOverride: RoomFirestoreSyncHelper? = null,
    private val customSearchHistoryDao: com.example.data.local.SearchHistoryDao? = null
) {
    private val repoScope = CoroutineScope(Dispatchers.IO)
    private val orderDao: OrderDao? = customOrderDao ?: context?.let { JubaGoDatabase.getDatabase(it).orderDao() }
    private val favoriteDao: com.example.data.local.FavoriteDao? = context?.let { JubaGoDatabase.getDatabase(it).favoriteDao() }
    private val searchHistoryDao: com.example.data.local.SearchHistoryDao? = customSearchHistoryDao ?: context?.let { JubaGoDatabase.getDatabase(it).searchHistoryDao() }

    val syncHelper: RoomFirestoreSyncHelper? = syncHelperOverride ?: if (orderDao != null && context != null) {
        RoomFirestoreSyncHelper(orderDao, context).apply {
            startObservingAndSync()
        }
    } else null

    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<com.example.data.local.SearchQueryEntity>>(emptyList())
    val searchHistory: StateFlow<List<com.example.data.local.SearchQueryEntity>> = _searchHistory.asStateFlow()

    init {
        favoriteDao?.let { dao ->
            repoScope.launch {
                dao.getFavoriteServiceIds().collect { list ->
                    _favoriteIds.value = list.toSet()
                }
            }
        }

        searchHistoryDao?.let { dao ->
            repoScope.launch {
                val existing = dao.getRecentSearches()
                if (existing.isEmpty()) {
                    val initialQueries = listOf(
                        com.example.data.local.SearchQueryEntity(query = "Grilled Nile Tilapia", categoryContext = "FOOD", timestamp = System.currentTimeMillis() - 600000),
                        com.example.data.local.SearchQueryEntity(query = "Boda-Boda Express", categoryContext = "CARS", timestamp = System.currentTimeMillis() - 1200000),
                        com.example.data.local.SearchQueryEntity(query = "Toyota Land Cruiser", categoryContext = "CARS", timestamp = System.currentTimeMillis() - 1800000),
                        com.example.data.local.SearchQueryEntity(query = "Konyo Konyo Market", categoryContext = "GROCERY", timestamp = System.currentTimeMillis() - 2400000),
                        com.example.data.local.SearchQueryEntity(query = "Shawarma Combo", categoryContext = "FOOD", timestamp = System.currentTimeMillis() - 3000000)
                    )
                    initialQueries.forEach { dao.insertSearch(it) }
                }

                dao.getRecentSearchesFlow().collect { list ->
                    _searchHistory.value = list
                }
            }
        }
    }

    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    private val initialServices = listOf(
        // CARS & TRANSPORT
        ServiceItem(
            id = "car_1",
            title = "Toyota Land Cruiser V8 (4x4)",
            category = ServiceCategory.CARS,
            providerName = "Equatoria Safari & Rentals",
            rating = 4.9,
            reviewCount = 142,
            isVerified = true,
            priceSsp = 145000,
            priceUsd = 120.0,
            priceUnit = "/day",
            location = "Airport Road, Juba",
            etaMinutes = 15,
            description = "Heavy duty 4x4 with professional driver, full AC, high ground clearance ideal for Juba town and regional field travel. Insured.",
            badge = "Popular 4x4",
            tags = listOf("4WD", "Chauffeur", "AC", "Full Tank Option")
        ),
        ServiceItem(
            id = "car_2",
            title = "Boda-Boda Express City Ride",
            category = ServiceCategory.CARS,
            providerName = "Juba Swift Riders",
            rating = 4.8,
            reviewCount = 389,
            isVerified = true,
            priceSsp = 2500,
            priceUsd = 2.0,
            priceUnit = "/trip",
            location = "Custom Market to Ministries",
            etaMinutes = 4,
            description = "Fastest way through Juba traffic. Certified helmet provided, vetted driver with GPS tracking.",
            badge = "Lightning Fast",
            tags = listOf("Helmet Included", "Instant Pickup", "City Commute")
        ),
        ServiceItem(
            id = "car_3",
            title = "Juba Airport VIP Sedan Transfer",
            category = ServiceCategory.CARS,
            providerName = "Nile Executive Chauffeurs",
            rating = 5.0,
            reviewCount = 88,
            isVerified = true,
            priceSsp = 45000,
            priceUsd = 38.0,
            priceUnit = "/trip",
            location = "Juba International Airport (JUB)",
            etaMinutes = 10,
            description = "Pre-booked arrival greeting at JUB international arrivals, luggage assistance, cold bottled water, air conditioned luxury sedan.",
            badge = "Airport VIP",
            tags = listOf("Flight Monitor", "Meet & Greet", "Spacious")
        ),

        // FLIGHTS
        ServiceItem(
            id = "flight_1",
            title = "Juba (JUB) to Wau (WUU) Direct",
            category = ServiceCategory.FLIGHTS,
            providerName = "Kush Air Express",
            rating = 4.7,
            reviewCount = 95,
            isVerified = true,
            priceSsp = 210000,
            priceUsd = 175.0,
            priceUnit = "/ticket",
            location = "Terminal 1, Juba Airport",
            etaMinutes = 55,
            description = "Scheduled direct domestic passenger flight. 20kg checked baggage included. Reliable on-time performance.",
            badge = "Daily Flights",
            tags = listOf("Direct", "20kg Luggage", "Domestic")
        ),
        ServiceItem(
            id = "flight_2",
            title = "Juba (JUB) to Entebbe (EBB)",
            category = ServiceCategory.FLIGHTS,
            providerName = "Uganda Airlines & Partners",
            rating = 4.9,
            reviewCount = 312,
            isVerified = true,
            priceSsp = 320000,
            priceUsd = 265.0,
            priceUnit = "/ticket",
            location = "International Departures JUB",
            etaMinutes = 65,
            description = "Daily regional commercial link connecting Juba to Uganda and onward international hubs. In-flight refreshment included.",
            badge = "Top Regional Route",
            tags = listOf("Daily", "Regional", "Meals Included")
        ),
        ServiceItem(
            id = "flight_3",
            title = "Juba (JUB) to Malakal (MAK)",
            category = ServiceCategory.FLIGHTS,
            providerName = "Tarco Aviation Services",
            rating = 4.6,
            reviewCount = 64,
            isVerified = true,
            priceSsp = 195000,
            priceUsd = 160.0,
            priceUnit = "/ticket",
            location = "Terminal 2, Juba Airport",
            etaMinutes = 50,
            description = "Upper Nile regional connection. Regular scheduled passenger and cargo service with experienced local crew.",
            tags = listOf("Scheduled", "Upper Nile", "Cargo Option")
        ),

        // HOTELS & APARTMENTS
        ServiceItem(
            id = "hotel_1",
            title = "Pyramid Continental Hotel - Nile Deluxe Suite",
            category = ServiceCategory.HOTELS,
            providerName = "Pyramid Continental",
            rating = 4.9,
            reviewCount = 215,
            isVerified = true,
            priceSsp = 240000,
            priceUsd = 195.0,
            priceUnit = "/night",
            location = "Ministries Road, Juba",
            etaMinutes = 0,
            description = "5-star luxury by the White Nile. 24/7 solar & backup power, high-speed fiber internet, swimming pool, gym, and rooftop restaurant.",
            badge = "Luxury Stay",
            tags = listOf("24/7 Power", "High-Speed WiFi", "Nile View", "Pool")
        ),
        ServiceItem(
            id = "hotel_2",
            title = "Hai Cinema Modern Furnished Apartment",
            category = ServiceCategory.HOTELS,
            providerName = "Juba Haven Residences",
            rating = 4.8,
            reviewCount = 76,
            isVerified = true,
            priceSsp = 95000,
            priceUsd = 80.0,
            priceUnit = "/night",
            location = "Hai Cinema, Central Juba",
            etaMinutes = 0,
            description = "Fully furnished 2-bedroom serviced apartment with kitchen, smart TV, security guard, washing machine, and quiet ambiance.",
            badge = "Self-Catering",
            tags = listOf("Kitchen", "Gated Security", "Washer", "Balcony")
        ),
        ServiceItem(
            id = "hotel_3",
            title = "Crown Hotel Juba - Executive Room",
            category = ServiceCategory.HOTELS,
            providerName = "Crown Hotel Juba",
            rating = 4.7,
            reviewCount = 180,
            isVerified = true,
            priceSsp = 180000,
            priceUsd = 150.0,
            priceUnit = "/night",
            location = "Airport Road, Juba",
            etaMinutes = 0,
            description = "Minutes from the airport with complimentary shuttle, conference center, buffet breakfast, and spacious garden terrace.",
            tags = listOf("Airport Shuttle", "Breakfast Included", "Business Center")
        ),

        // FOOD & RESTAURANTS
        ServiceItem(
            id = "food_1",
            title = "Traditional Kisra with Bamia & Tender Beef",
            category = ServiceCategory.FOOD,
            providerName = "Mama Nyandeng Kitchen",
            rating = 4.9,
            reviewCount = 520,
            isVerified = true,
            priceSsp = 6500,
            priceUsd = 5.2,
            priceUnit = "/portion",
            location = "Konyo Konyo Market",
            etaMinutes = 25,
            description = "Authentic South Sudanese sourdough Kisra sheets served with slow-cooked Bamia Mafruka okra stew and seasoned Nile beef.",
            badge = "Local Favorite",
            tags = listOf("Traditional", "Hot & Fresh", "Generous Portion")
        ),
        ServiceItem(
            id = "food_2",
            title = "Grilled Fresh Nile Tilapia with Spicy Salad",
            category = ServiceCategory.FOOD,
            providerName = "Nile Breeze Grill & Terrace",
            rating = 5.0,
            reviewCount = 412,
            isVerified = true,
            priceSsp = 9500,
            priceUsd = 7.8,
            priceUnit = "/plate",
            location = "Nile River Bank, Hai Malakal",
            etaMinutes = 30,
            description = "Catch of the day whole Nile tilapia charcoal-grilled with lemon, garlic herbs, served with fried plantains and spicy chili dip.",
            badge = "Chef's Special",
            tags = listOf("Fresh Seafood", "Charcoal Grilled", "Spicy")
        ),
        ServiceItem(
            id = "food_3",
            title = "Juba Shawarma & Crispy Fries Combo",
            category = ServiceCategory.FOOD,
            providerName = "Al-Pasha Express",
            rating = 4.8,
            reviewCount = 310,
            isVerified = true,
            priceSsp = 5000,
            priceUsd = 4.0,
            priceUnit = "/combo",
            location = "Juba Town Commercial Center",
            etaMinutes = 20,
            description = "Juicy chicken shawarma wrapped in freshly baked pita bread, garlic toum sauce, pickled veggies, and golden crisp fries.",
            tags = listOf("Fast Food", "Halal", "Best Seller")
        ),

        // SUPERMARKET & ESSENTIALS
        ServiceItem(
            id = "groc_1",
            title = "Fresh Organic Konyo Konyo Fruit & Veg Basket",
            category = ServiceCategory.GROCERY,
            providerName = "Green Valley Juba Farm",
            rating = 4.9,
            reviewCount = 134,
            isVerified = true,
            priceSsp = 8500,
            priceUsd = 7.0,
            priceUnit = "/basket",
            location = "Konyo Konyo Market",
            etaMinutes = 28,
            description = "Hand-picked ripe mangoes, fresh tomatoes, onions, leafy greens, and local sweet bananas packed securely in ventilated crate.",
            badge = "Organic Farm",
            tags = listOf("Farm Fresh", "Organic", "Same-Day Delivery")
        ),
        ServiceItem(
            id = "groc_2",
            title = "Pack of 12 Pure Mineral Water (1.5L Bottles)",
            category = ServiceCategory.GROCERY,
            providerName = "Juba SuperMart Wholesale",
            rating = 4.8,
            reviewCount = 240,
            isVerified = true,
            priceSsp = 4500,
            priceUsd = 3.6,
            priceUnit = "/carton",
            location = "Gudele Road, Munuki",
            etaMinutes = 22,
            description = "Essential certified purified drinking water for home and office. Fast delivery right to your doorstep or apartment.",
            tags = listOf("Heavy Delivery", "Essentials", "Clean Water")
        ),

        // SOUTH SUDAN NATIONWIDE
        ServiceItem(
            id = "nat_1",
            title = "Wau Nile Basin Hotel & Executive Lodge",
            category = ServiceCategory.HOTELS,
            providerName = "Bahr el Ghazal Hospitality Group",
            rating = 4.8,
            reviewCount = 92,
            isVerified = true,
            priceSsp = 160000,
            priceUsd = 135.0,
            priceUnit = "/night",
            location = "Wau Central, South Sudan",
            etaMinutes = 0,
            description = "Premier hotel accommodation in Wau city. 24/7 solar power, high-security compound, airport shuttle from Wau Airport (WUU).",
            badge = "Wau Premier",
            tags = listOf("Wau City", "Solar Power", "Conference", "WiFi"),
            coverageScope = CoverageScope.SOUTH_SUDAN_NATIONAL,
            country = "South Sudan"
        ),
        ServiceItem(
            id = "nat_2",
            title = "Nimule Border Customs Clearing & 4x4 Heavy Haul",
            category = ServiceCategory.CARS,
            providerName = "Trans-Equatoria Freight & Border Logistics",
            rating = 4.9,
            reviewCount = 118,
            isVerified = true,
            priceSsp = 220000,
            priceUsd = 180.0,
            priceUnit = "/consignment",
            location = "Nimule Border Post, South Sudan",
            etaMinutes = 60,
            description = "Express cross-border clearing between South Sudan and Uganda, heavy escort vehicles, verified customs paperwork.",
            badge = "Border Transit",
            tags = listOf("Nimule Post", "Cross-Border", "Heavy Duty", "Escort"),
            coverageScope = CoverageScope.SOUTH_SUDAN_NATIONAL,
            country = "South Sudan"
        ),

        // EAST AFRICA REGIONAL
        ServiceItem(
            id = "reg_1",
            title = "Nairobi Westlands Luxury Furnished Suites",
            category = ServiceCategory.HOTELS,
            providerName = "East Africa Grand Residences",
            rating = 4.9,
            reviewCount = 205,
            isVerified = true,
            priceSsp = 190000,
            priceUsd = 155.0,
            priceUnit = "/night",
            location = "Westlands, Nairobi, Kenya",
            etaMinutes = 0,
            description = "Direct booking from Juba: 5-star serviced apartments in Nairobi, airport pickup from JKIA, gym, heated pool, 24/7 concierge.",
            badge = "Nairobi Stay",
            tags = listOf("Nairobi", "Kenya", "Airport Shuttle", "Pool"),
            coverageScope = CoverageScope.EAST_AFRICA_REGIONAL,
            country = "Kenya"
        ),
        ServiceItem(
            id = "reg_2",
            title = "Kampala Kololo Hill Executive Residence",
            category = ServiceCategory.HOTELS,
            providerName = "Pearl of Africa Stays",
            rating = 4.8,
            reviewCount = 144,
            isVerified = true,
            priceSsp = 140000,
            priceUsd = 115.0,
            priceUnit = "/night",
            location = "Kololo, Kampala, Uganda",
            etaMinutes = 0,
            description = "Upscale diplomatic enclave suites in Kampala. Ideal for South Sudanese families and professionals traveling to Uganda.",
            badge = "Kampala Suites",
            tags = listOf("Kampala", "Uganda", "Diplomatic Zone", "Kitchen"),
            coverageScope = CoverageScope.EAST_AFRICA_REGIONAL,
            country = "Uganda"
        ),

        // BEYOND AFRICA & GLOBAL
        ServiceItem(
            id = "glob_1",
            title = "Dubai to Juba Express Air Cargo & Trade Logistics",
            category = ServiceCategory.CARS,
            providerName = "Gulf & Nile Global Cargo LLC",
            rating = 5.0,
            reviewCount = 310,
            isVerified = true,
            priceSsp = 360000,
            priceUsd = 295.0,
            priceUnit = "/shipment",
            location = "Dubai (DXB) to Juba (JUB)",
            etaMinutes = 180,
            description = "Fast direct air freight from Deira & Al Garhoud Dubai straight to Juba International Airport. Customs handling & warehouse pickup.",
            badge = "Dubai-Juba Express",
            tags = listOf("Dubai UAE", "Air Cargo", "Electronics & Goods", "Door to Door"),
            coverageScope = CoverageScope.GLOBAL_BEYOND_AFRICA,
            country = "United Arab Emirates"
        ),
        ServiceItem(
            id = "glob_2",
            title = "Cairo Nile View Furnished Medical & Family Suites",
            category = ServiceCategory.HOTELS,
            providerName = "Pharaohs Nile Hospitality",
            rating = 4.8,
            reviewCount = 168,
            isVerified = true,
            priceSsp = 120000,
            priceUsd = 98.0,
            priceUnit = "/night",
            location = "Maadi & Zamalek, Cairo, Egypt",
            etaMinutes = 0,
            description = "Convenient apartments near Cairo hospitals and embassies, Arabic and English assistance, Nile river view, and Juba community support.",
            badge = "Cairo Gateway",
            tags = listOf("Cairo Egypt", "Medical Travel", "Nile View", "Arabic/English"),
            coverageScope = CoverageScope.GLOBAL_BEYOND_AFRICA,
            country = "Egypt"
        )
    )

    private val initialOrders = listOf(
        OrderBooking(
            id = "JB-8921",
            serviceId = "food_2",
            title = "Grilled Fresh Nile Tilapia with Spicy Salad",
            category = ServiceCategory.FOOD,
            providerName = "Nile Breeze Grill & Terrace",
            customerName = "Deng Majok",
            customerPhone = "+211 92 123 4567",
            deliveryAddress = "Hai Cinema, Block 3, House 14, Juba",
            totalPriceSsp = 9500,
            totalPriceUsd = 7.8,
            paymentMethod = PaymentMethod.M_GURUSH,
            paymentReference = "MG-908123-OK",
            status = DeliveryStatus.ON_THE_WAY,
            courierName = "Garang Bol",
            courierPhone = "+211 91 888 7766",
            courierVehicle = "TVS Apache Boda (SSD-241B)",
            courierRating = 4.95,
            orderedAt = "12:15 PM",
            etaMinutes = 8,
            liveProgress = 0.65f,
            riderCurrentLocationName = "Ministries Road near Custom Roundabout"
        ),
        OrderBooking(
            id = "JB-7740",
            serviceId = "car_1",
            title = "Toyota Land Cruiser V8 (4x4)",
            category = ServiceCategory.CARS,
            providerName = "Equatoria Safari & Rentals",
            customerName = "Sarah Lado",
            customerPhone = "+211 92 444 3322",
            deliveryAddress = "Pyramid Hotel Lobby, Juba",
            totalPriceSsp = 145000,
            totalPriceUsd = 120.0,
            paymentMethod = PaymentMethod.MTN_MOMO,
            paymentReference = "MTN-55102-CONF",
            status = DeliveryStatus.CONFIRMED,
            courierName = "Joseph Kenyi",
            courierPhone = "+211 92 777 1122",
            courierVehicle = "Land Cruiser 4x4 (SSD-808A)",
            courierRating = 5.0,
            orderedAt = "11:40 AM",
            etaMinutes = 15,
            liveProgress = 0.2f,
            riderCurrentLocationName = "Airport Road depot"
        )
    )

    private val initialReviews = listOf(
        Review("rev_1", "food_1", "Amina Chol", 5.0, "Yesterday", "Best Kisra in Juba! Arrived steaming hot in under 25 minutes. Will definitely order every week."),
        Review("rev_2", "food_2", "John Mabior", 5.0, "2 days ago", "The grilled fish was fresh out of the Nile, season was incredible and m-GURUSH checkout was instant."),
        Review("rev_3", "car_1", "David K.", 4.9, "3 days ago", "Cruiser was spotless and AC worked great through the dusty road to Bilpam. Chauffeur was courteous."),
        Review("rev_4", "hotel_1", "Grace A.", 5.0, "Last week", "Unmatched internet speed and backup generator kept my remote work uninterrupted. Excellent poolside dining.")
    )

    private val initialChat = listOf(
        ChatMessage("msg_1", "Mary (JubaGo Helpdesk)", "Hello! Welcome to JubaGo South Sudan support. How can we assist your bookings or delivery today?", "12:10 PM", false, "Support"),
        ChatMessage("msg_2", "Garang Bol (Courier)", "JubaGo courier Garang here: I have picked up your Nile Breeze Tilapia, heading down Ministries Road towards Hai Cinema now!", "12:20 PM", false, "Courier")
    )

    private val initialAuditLogs = listOf(
        AuditLogEntry("aud_1", "12:15 PM", "ORDER_CREATED", "Customer", "Order JB-8921 placed with m-GURUSH payment authorization"),
        AuditLogEntry("aud_2", "12:18 PM", "MERCHANT_CONFIRM", "Merchant", "Nile Breeze Grill confirmed order & began kitchen prep"),
        AuditLogEntry("aud_3", "12:20 PM", "DISPATCH_ASSIGNED", "System", "Boda rider Garang Bol assigned for delivery")
    )

    // Reactive State
    private val _services = MutableStateFlow(initialServices)
    val services: StateFlow<List<ServiceItem>> = _services.asStateFlow()

    private val _orders = MutableStateFlow(initialOrders)
    val orders: StateFlow<List<OrderBooking>> = _orders.asStateFlow()

    private val _selectedOrder = MutableStateFlow<OrderBooking?>(initialOrders.firstOrNull())
    val selectedOrder: StateFlow<OrderBooking?> = _selectedOrder.asStateFlow()

    private val _reviews = MutableStateFlow(initialReviews)
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    private val _chatMessages = MutableStateFlow(initialChat)
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _auditLogs = MutableStateFlow(initialAuditLogs)
    val auditLogs: StateFlow<List<AuditLogEntry>> = _auditLogs.asStateFlow()

    private val _currentRole = MutableStateFlow(UserRole.CUSTOMER)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    private val _notifications = MutableStateFlow(
        listOf(
            "Rider Garang is 4 mins away with your Nile Breeze meal!",
            "Flight JUB -> EBB booking gate opens in 2 hours at Juba Int. Airport",
            "Flash Deal: 15% off Land Cruiser 4x4 weekend rentals in Juba!"
        )
    )
    val notifications: StateFlow<List<String>> = _notifications.asStateFlow()

    init {
        orderDao?.let { dao ->
            repoScope.launch {
                val existing = dao.getAllOrders()
                if (existing.isEmpty()) {
                    dao.insertOrders(initialOrders.map { OrderEntity.fromDomainModel(it, isSynced = false) })
                }
                dao.getAllOrdersFlow().collect { roomList ->
                    if (roomList.isNotEmpty()) {
                        _orders.value = roomList.map { it.toDomainModel() }
                    }
                }
            }
        }
    }

    fun switchRole(role: UserRole) {
        _currentRole.value = role
        addAuditLog("ROLE_SWITCH", role.label, "Switched active workspace view to ${role.label}")
    }

    fun toggleOfflineMode() {
        val next = !_isOfflineMode.value
        _isOfflineMode.value = next
        addAuditLog("NETWORK_STATE", "System", if (next) "Simulated Offline Mode enabled (cached storage)" else "Network reconnected, queued updates synced")
    }

    fun toggleServiceAvailability(serviceId: String) {
        _services.value = _services.value.map { item ->
            if (item.id == serviceId) {
                val updated = item.copy(isAvailable = !item.isAvailable)
                addAuditLog("ITEM_AVAILABILITY", "Merchant", "${item.title} toggled to ${if (updated.isAvailable) "AVAILABLE" else "OUT OF STOCK"}")
                updated
            } else item
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: DeliveryStatus) {
        _orders.value = _orders.value.map { order ->
            if (order.id == orderId) {
                val updatedProgress = when (newStatus) {
                    DeliveryStatus.PLACED -> 0.1f
                    DeliveryStatus.CONFIRMED -> 0.25f
                    DeliveryStatus.PREPARING -> 0.45f
                    DeliveryStatus.ON_THE_WAY -> 0.75f
                    DeliveryStatus.DELIVERED -> 1.0f
                    DeliveryStatus.CANCELLED -> 0.0f
                }
                val updated = order.copy(
                    status = newStatus,
                    liveProgress = updatedProgress,
                    etaMinutes = if (newStatus == DeliveryStatus.DELIVERED) 0 else (order.etaMinutes - 3).coerceAtLeast(1)
                )
                if (_selectedOrder.value?.id == orderId) {
                    _selectedOrder.value = updated
                }
                addAuditLog("ORDER_STATUS_UPDATE", "Merchant", "Order $orderId status changed to ${newStatus.label}")
                val messageText = "Order $orderId: ${newStatus.label}"
                _notifications.value = listOf(messageText) + _notifications.value
                context?.let { ctx ->
                    com.example.data.notification.FcmNotificationManager.triggerSimulatedNotification(
                        context = ctx,
                        type = com.example.data.notification.PushNotificationType.ORDER_STATUS,
                        title = "Order $orderId Update",
                        body = "Status is now: ${newStatus.label}",
                        orderId = orderId
                    )
                }
                updated
            } else order
        }
    }

    fun addNotification(message: String) {
        _notifications.value = listOf(message) + _notifications.value
    }

    fun clearNotifications() {
        _notifications.value = emptyList()
    }

    fun recordSearchQuery(query: String, categoryContext: String? = null) {
        val trimmed = query.trim()
        if (trimmed.length < 2) return

        searchHistoryDao?.let { dao ->
            repoScope.launch {
                val existing = dao.findSearchByQuery(trimmed)
                if (existing != null) {
                    dao.insertSearch(
                        existing.copy(
                            timestamp = System.currentTimeMillis(),
                            searchCount = existing.searchCount + 1,
                            categoryContext = categoryContext ?: existing.categoryContext
                        )
                    )
                } else {
                    dao.insertSearch(
                        com.example.data.local.SearchQueryEntity(
                            query = trimmed,
                            categoryContext = categoryContext,
                            timestamp = System.currentTimeMillis(),
                            searchCount = 1
                        )
                    )
                }
            }
        } ?: run {
            val current = _searchHistory.value.filter { !it.query.equals(trimmed, ignoreCase = true) }
            val updated = listOf(
                com.example.data.local.SearchQueryEntity(
                    query = trimmed,
                    categoryContext = categoryContext,
                    timestamp = System.currentTimeMillis()
                )
            ) + current
            _searchHistory.value = updated.take(15)
        }
    }

    fun removeSearchQuery(query: String) {
        searchHistoryDao?.let { dao ->
            repoScope.launch {
                dao.deleteSearchByQuery(query)
            }
        } ?: run {
            _searchHistory.value = _searchHistory.value.filter { !it.query.equals(query, ignoreCase = true) }
        }
    }

    fun clearSearchHistory() {
        searchHistoryDao?.let { dao ->
            repoScope.launch {
                dao.clearAll()
            }
        } ?: run {
            _searchHistory.value = emptyList()
        }
    }

    fun selectOrderForTracking(order: OrderBooking) {
        _selectedOrder.value = order
    }

    fun advanceLiveDriverProgress(orderId: String) {
        _orders.value = _orders.value.map { order ->
            if (order.id == orderId) {
                val nextProgress = (order.liveProgress + 0.15f).let { if (it > 0.95f) 1.0f else it }
                val nextEta = ((1.0f - nextProgress) * 15).toInt().coerceAtLeast(1)
                val nextStatus = if (nextProgress >= 1.0f) DeliveryStatus.DELIVERED else DeliveryStatus.ON_THE_WAY
                val nextLocation = when {
                    nextProgress < 0.35f -> "Custom Market Roundabout, Juba"
                    nextProgress < 0.65f -> "Ministries Road Avenue, Juba"
                    nextProgress < 0.90f -> "Approaching Hai Cinema, Juba"
                    else -> "Arrived at Hai Cinema Destination"
                }
                val updated = order.copy(
                    liveProgress = nextProgress,
                    etaMinutes = if (nextStatus == DeliveryStatus.DELIVERED) 0 else nextEta,
                    status = nextStatus,
                    riderCurrentLocationName = nextLocation
                )
                if (_selectedOrder.value?.id == orderId) {
                    _selectedOrder.value = updated
                }
                orderDao?.let { dao ->
                    repoScope.launch {
                        dao.updateOrder(OrderEntity.fromDomainModel(updated, isSynced = false))
                    }
                }
                addAuditLog("DRIVER_GPS_TELEMETRY", "Courier", "Driver ${order.courierName} location updated: $nextLocation")
                updated
            } else order
        }
    }

    fun createOrder(
        service: ServiceItem,
        customerName: String,
        customerPhone: String,
        deliveryAddress: String,
        paymentMethod: PaymentMethod
    ): OrderBooking {
        val orderId = "JB-" + (1000..9999).random()
        val refCode = when (paymentMethod) {
            PaymentMethod.M_GURUSH -> "MG-" + (100000..999999).random() + "-SS"
            PaymentMethod.MTN_MOMO -> "MTN-" + (100000..999999).random() + "-SS"
            PaymentMethod.DIGITEL_PAY -> "DIG-" + (100000..999999).random()
            PaymentMethod.CASH_ON_DELIVERY -> "COD-RECEIPT-VERIFIED"
            PaymentMethod.BANK_CARD -> "TXN-CARD-" + (100000..999999).random()
        }

        val newOrder = OrderBooking(
            id = orderId,
            serviceId = service.id,
            title = service.title,
            category = service.category,
            providerName = service.providerName,
            customerName = customerName.ifBlank { "Valued Resident" },
            customerPhone = customerPhone.ifBlank { "+211 92 000 1122" },
            deliveryAddress = deliveryAddress.ifBlank { "Juba Central, South Sudan" },
            totalPriceSsp = service.priceSsp,
            totalPriceUsd = service.priceUsd,
            paymentMethod = paymentMethod,
            paymentReference = refCode,
            status = DeliveryStatus.CONFIRMED,
            courierName = "Emmanuel Lodu",
            courierPhone = "+211 92 555 4321",
            courierVehicle = "Boda Express #31",
            courierRating = 4.9,
            orderedAt = timeFormat.format(Date()),
            etaMinutes = service.etaMinutes.coerceAtLeast(12),
            liveProgress = 0.35f,
            riderCurrentLocationName = "Near Ministries Roundabout"
        )

        _orders.value = listOf(newOrder) + _orders.value
        _selectedOrder.value = newOrder

        // Persist offline-first to Room database
        orderDao?.let { dao ->
            repoScope.launch {
                val entity = OrderEntity.fromDomainModel(newOrder, isSynced = false)
                dao.insertOrder(entity)
                syncHelper?.triggerSync()
            }
        }

        addAuditLog("NEW_BOOKING", "Customer", "Order $orderId placed for ${service.title} via ${paymentMethod.title} ($refCode)")
        _notifications.value = listOf("Booking $orderId Confirmed! Courier dispatched.") + _notifications.value

        // Auto add a greeting message from courier
        val courierMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            senderName = newOrder.courierName,
            message = "Hi $customerName, I have received your request for ${service.title}. I'm en route from ${service.location} to your location ($deliveryAddress).",
            timestamp = timeFormat.format(Date()),
            isFromUser = false,
            roleBadge = "Courier"
        )
        _chatMessages.value = _chatMessages.value + courierMsg

        return newOrder
    }

    fun reorderBooking(previousOrder: OrderBooking): OrderBooking {
        val newOrderId = "JB-" + (1000..9999).random()
        val refCode = when (previousOrder.paymentMethod) {
            PaymentMethod.M_GURUSH -> "MG-" + (100000..999999).random() + "-RE"
            PaymentMethod.MTN_MOMO -> "MTN-" + (100000..999999).random() + "-RE"
            PaymentMethod.DIGITEL_PAY -> "DIG-" + (100000..999999).random() + "-RE"
            PaymentMethod.CASH_ON_DELIVERY -> "COD-REORDER-VERIFIED"
            PaymentMethod.BANK_CARD -> "TXN-CARD-" + (100000..999999).random()
        }

        val reordered = previousOrder.copy(
            id = newOrderId,
            paymentReference = refCode,
            status = DeliveryStatus.CONFIRMED,
            orderedAt = timeFormat.format(Date()),
            etaMinutes = previousOrder.etaMinutes.coerceAtLeast(15),
            liveProgress = 0.25f,
            riderCurrentLocationName = "Dispatched from ${previousOrder.providerName}"
        )

        _orders.value = listOf(reordered) + _orders.value
        _selectedOrder.value = reordered

        // Persist offline-first to Room and sync to Cloud Firestore
        orderDao?.let { dao ->
            repoScope.launch {
                val entity = OrderEntity.fromDomainModel(reordered, isSynced = false)
                dao.insertOrder(entity)
                syncHelper?.triggerSync()
            }
        }

        addAuditLog("REORDER_PLACED", "Customer", "Reordered ${reordered.title} (New Order #${reordered.id} from original #${previousOrder.id})")
        _notifications.value = listOf("Reorder #${reordered.id} confirmed! Courier assigned.") + _notifications.value

        return reordered
    }

    suspend fun refreshOrdersFromFirestore(): Pair<Boolean, String> {
        val result = syncHelper?.fetchOrdersFromFirestore()
            ?: Pair(false, "Firestore sync helper not initialized")

        orderDao?.let { dao ->
            val roomList = dao.getAllOrders()
            if (roomList.isNotEmpty()) {
                _orders.value = roomList.map { it.toDomainModel() }
            }
        }
        return result
    }

    suspend fun syncReviewsFromFirestore(): Pair<Boolean, String> {
        val result = syncHelper?.fetchReviewsFromFirestore()
            ?: Pair(false, emptyList())
        if (result.first && result.second.isNotEmpty()) {
            val incoming = result.second
            val existingIds = _reviews.value.map { it.id }.toSet()
            val merged = incoming.filter { it.id !in existingIds } + _reviews.value
            _reviews.value = merged
            return Pair(true, "Synchronized ${incoming.size} reviews from Firestore")
        }
        return Pair(result.first, "Reviews up to date")
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            senderName = "You",
            message = text,
            timestamp = timeFormat.format(Date()),
            isFromUser = true,
            roleBadge = "Customer"
        )
        _chatMessages.value = _chatMessages.value + userMsg

        // Instant automated friendly reply based on keywords
        val lower = text.lowercase()
        val replyText = when {
            lower.contains("boda") || lower.contains("rider") || lower.contains("driver") ->
                "Your driver is currently navigating via Ministries Road. You can follow their live pulse marker on the Juba GPS map screen!"
            lower.contains("gurush") || lower.contains("momo") || lower.contains("payment") ->
                "Payment is securely held in escrow until your service is marked delivered. If you need a refund or transaction receipt, download it from your order screen."
            lower.contains("hotel") || lower.contains("flight") ->
                "Hotels and flights in Juba come with guaranteed reservation confirmations. Flight tickets will also display boarding codes."
            lower.contains("gudele") || lower.contains("munuki") || lower.contains("konyo") ->
                "We provide full coverage to Gudele, Munuki, Hai Cinema, Custom, and Juba town centers with live GPS tracking."
            else ->
                "Thank you for contacting JubaGo South Sudan Support! We are here 24/7 to guarantee safe bookings and reliable local delivery."
        }

        val supportMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            senderName = "JubaGo Support Desk",
            message = replyText,
            timestamp = timeFormat.format(Date()),
            isFromUser = false,
            roleBadge = "Support"
        )
        _chatMessages.value = _chatMessages.value + supportMsg
    }

    fun addReview(
        serviceId: String,
        authorName: String,
        rating: Double,
        comment: String,
        orderId: String? = null,
        tags: List<String> = emptyList()
    ): Review {
        val reviewId = "rev_" + UUID.randomUUID().toString().take(8)
        val newRev = Review(
            id = reviewId,
            serviceId = serviceId,
            orderId = orderId,
            authorName = authorName.ifBlank { "Juba Citizen" },
            rating = rating,
            date = "Just now",
            comment = comment,
            tags = tags,
            isVerifiedBuyer = true,
            timestamp = System.currentTimeMillis()
        )
        _reviews.value = listOf(newRev) + _reviews.value

        // Recompute dynamic service rating and review count
        _services.value = _services.value.map { item ->
            if (item.id == serviceId) {
                val allServiceRevs = _reviews.value.filter { it.serviceId == serviceId }
                val newAvg = if (allServiceRevs.isNotEmpty()) allServiceRevs.map { it.rating }.average() else rating
                val roundedRating = ((newAvg * 10).toInt() / 10.0).coerceIn(1.0, 5.0)
                item.copy(
                    rating = roundedRating,
                    reviewCount = item.reviewCount + 1
                )
            } else item
        }

        // Push to Cloud Firestore in background
        repoScope.launch {
            syncHelper?.saveReviewToFirestore(newRev)
        }

        addAuditLog("REVIEW_SUBMITTED", "Customer", "Review of $rating★ added for service $serviceId${if (orderId != null) " (Order #$orderId)" else ""}")
        _notifications.value = listOf("Review posted! Rating updated for service.") + _notifications.value
        return newRev
    }

    fun getMerchantAnalytics(): MerchantAnalytics {
        val ordersList = _orders.value
        val todaySalesSsp = ordersList.sumOf { it.totalPriceSsp }
        val todaySalesUsd = ordersList.sumOf { it.totalPriceUsd }
        val activeCount = ordersList.count { it.status != DeliveryStatus.DELIVERED && it.status != DeliveryStatus.CANCELLED }
        val completedCount = ordersList.count { it.status == DeliveryStatus.DELIVERED }

        val weeklyTrend = listOf(
            "Mon" to 1250000L,
            "Tue" to 1420000L,
            "Wed" to 1680000L,
            "Thu" to 1950000L,
            "Fri" to 2400000L,
            "Sat" to 2850000L,
            "Sun" to todaySalesSsp.coerceAtLeast(1800000L)
        )

        return MerchantAnalytics(
            todaySalesSsp = todaySalesSsp,
            todaySalesUsd = todaySalesUsd,
            activeOrdersCount = activeCount,
            completedOrdersCount = completedCount,
            averageDeliveryMinutes = 18,
            customerRating = 4.88,
            weeklySalesTrend = weeklyTrend
        )
    }

    fun generateReportCsv(): String {
        val sb = StringBuilder()
        sb.append("JubaGo Platform Audit & Sales Report\n")
        sb.append("Generated,${dateFormat.format(Date())} ${timeFormat.format(Date())}\n")
        sb.append("Currency,South Sudanese Pound (SSP) & USD\n\n")
        sb.append("Order ID,Service Title,Category,Provider,Customer,Price SSP,Price USD,Payment,Status\n")
        for (order in _orders.value) {
            sb.append("${order.id},\"${order.title}\",${order.category.displayName},\"${order.providerName}\",\"${order.customerName}\",${order.totalPriceSsp},${order.totalPriceUsd},${order.paymentMethod.title},${order.status.name}\n")
        }
        sb.append("\nAudit Log History:\n")
        sb.append("Timestamp,Action,Role,Details\n")
        for (log in _auditLogs.value) {
            sb.append("${log.timestamp},${log.action},${log.userRole},\"${log.details}\"\n")
        }
        return sb.toString()
    }

    fun registerBusiness(request: BusinessRegistrationRequest): ServiceItem {
        val newId = if (request.id.isNotBlank()) request.id else "biz_" + (1000..9999).random()
        val normalizedRequest = request.copy(id = newId)

        val newService = ServiceItem(
            id = newId,
            title = normalizedRequest.serviceOfferingTitle.ifBlank { normalizedRequest.businessName },
            category = normalizedRequest.category,
            providerName = normalizedRequest.businessName,
            rating = 5.0,
            reviewCount = 1,
            isVerified = true,
            priceSsp = normalizedRequest.priceSsp.coerceAtLeast(1000L),
            priceUsd = normalizedRequest.priceUsd.coerceAtLeast(1.0),
            priceUnit = normalizedRequest.priceUnit.ifBlank { "/unit" },
            location = "${normalizedRequest.physicalAddress}, ${normalizedRequest.cityAndCountry}",
            etaMinutes = 15,
            description = normalizedRequest.serviceDescription.ifBlank { "Registered business on JubaGo network providing ${normalizedRequest.businessType} services." },
            isAvailable = true,
            badge = "Verified Partner",
            tags = listOf("Verified Merchant", normalizedRequest.businessType, normalizedRequest.cityAndCountry),
            coverageScope = normalizedRequest.coverageScope,
            country = normalizedRequest.cityAndCountry
        )

        _services.value = listOf(newService) + _services.value
        addAuditLog("BUSINESS_REGISTERED", "Merchant", "New business registered: ${normalizedRequest.businessName} (${normalizedRequest.businessType}) in ${normalizedRequest.cityAndCountry} with coverage ${normalizedRequest.coverageScope.displayName}")
        _notifications.value = listOf("Welcome ${normalizedRequest.businessName}! Verified and active across ${normalizedRequest.coverageScope.displayName} on JubaGo.") + _notifications.value

        // Asynchronously persist to Cloud Firestore
        repoScope.launch {
            try {
                val syncResult = syncHelper?.saveVendorToFirestore(normalizedRequest)
                if (syncResult?.first == true) {
                    addAuditLog("FIRESTORE_SYNC", "System", "Vendor ${normalizedRequest.businessName} synced to Cloud Firestore")
                } else {
                    addAuditLog("FIRESTORE_QUEUED", "System", "Vendor ${normalizedRequest.businessName} cached locally: ${syncResult?.second}")
                }
            } catch (e: Exception) {
                addAuditLog("FIRESTORE_ERROR", "System", "Failed to sync vendor to Firestore: ${e.message}")
            }
        }

        return newService
    }

    suspend fun saveVendorDirectlyToFirestore(request: BusinessRegistrationRequest): Pair<Boolean, String> {
        val newId = if (request.id.isNotBlank()) request.id else "biz_" + (1000..9999).random()
        val normalizedRequest = request.copy(id = newId)

        // Ensure it's in the local services list
        val newService = ServiceItem(
            id = newId,
            title = normalizedRequest.serviceOfferingTitle.ifBlank { normalizedRequest.businessName },
            category = normalizedRequest.category,
            providerName = normalizedRequest.businessName,
            rating = 5.0,
            reviewCount = 1,
            isVerified = true,
            priceSsp = normalizedRequest.priceSsp.coerceAtLeast(1000L),
            priceUsd = normalizedRequest.priceUsd.coerceAtLeast(1.0),
            priceUnit = normalizedRequest.priceUnit.ifBlank { "/unit" },
            location = "${normalizedRequest.physicalAddress}, ${normalizedRequest.cityAndCountry}",
            etaMinutes = 15,
            description = normalizedRequest.serviceDescription.ifBlank { "Registered business on JubaGo network providing ${normalizedRequest.businessType} services." },
            isAvailable = true,
            badge = "Verified Partner",
            tags = listOf("Verified Merchant", normalizedRequest.businessType, normalizedRequest.cityAndCountry),
            coverageScope = normalizedRequest.coverageScope,
            country = normalizedRequest.cityAndCountry
        )

        val existing = _services.value.any { it.id == newId }
        if (!existing) {
            _services.value = listOf(newService) + _services.value
            addAuditLog("BUSINESS_REGISTERED", "Merchant", "New business registered: ${normalizedRequest.businessName} (${normalizedRequest.businessType}) in ${normalizedRequest.cityAndCountry} with coverage ${normalizedRequest.coverageScope.displayName}")
            _notifications.value = listOf("Welcome ${normalizedRequest.businessName}! Verified and active across ${normalizedRequest.coverageScope.displayName} on JubaGo.") + _notifications.value
        }

        return syncHelper?.saveVendorToFirestore(normalizedRequest)
            ?: Pair(true, "Saved locally (Firestore sync helper not initialized)")
    }

    suspend fun toggleFavorite(serviceId: String) {
        val current = _favoriteIds.value
        if (current.contains(serviceId)) {
            _favoriteIds.value = current - serviceId
            favoriteDao?.deleteFavorite(serviceId)
        } else {
            _favoriteIds.value = current + serviceId
            favoriteDao?.insertFavorite(com.example.data.local.FavoriteEntity(serviceId))
        }
    }

    private fun addAuditLog(action: String, role: String, details: String) {
        val entry = AuditLogEntry(
            id = UUID.randomUUID().toString(),
            timestamp = timeFormat.format(Date()),
            action = action,
            userRole = role,
            details = details
        )
        _auditLogs.value = listOf(entry) + _auditLogs.value
    }
}
