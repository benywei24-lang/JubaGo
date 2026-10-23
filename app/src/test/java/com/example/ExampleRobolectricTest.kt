package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.JubaGoRepository
import com.example.model.DeliveryStatus
import com.example.model.PaymentMethod
import com.example.model.ServiceCategory
import com.example.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches JubaGo`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("JubaGo", appName)
    }

    @Test
    fun `repository initial state contains authentic Juba services`() {
        val repo = JubaGoRepository()
        val services = repo.services.value

        assertTrue(services.isNotEmpty())
        assertTrue(services.any { it.category == ServiceCategory.CARS })
        assertTrue(services.any { it.category == ServiceCategory.FLIGHTS })
        assertTrue(services.any { it.category == ServiceCategory.HOTELS })
        assertTrue(services.any { it.category == ServiceCategory.FOOD })
        assertTrue(services.any { it.category == ServiceCategory.GROCERY })
    }

    @Test
    fun `order creation and payment processing succeeds`() {
        val repo = JubaGoRepository()
        val firstService = repo.services.value.first()

        val order = repo.createOrder(
            service = firstService,
            customerName = "Deng Majok",
            customerPhone = "+211 92 123 4567",
            deliveryAddress = "Hai Cinema, Juba",
            paymentMethod = PaymentMethod.M_GURUSH
        )

        assertNotNull(order.id)
        assertTrue(order.paymentReference.startsWith("MG-"))
        assertEquals(DeliveryStatus.CONFIRMED, order.status)
        assertEquals("Deng Majok", order.customerName)
    }

    @Test
    fun `merchant analytics and report export work correctly`() {
        val repo = JubaGoRepository()
        val analytics = repo.getMerchantAnalytics()

        assertTrue(analytics.todaySalesSsp > 0)
        assertTrue(analytics.customerRating > 4.0)

        val csv = repo.generateReportCsv()
        assertTrue(csv.contains("JubaGo Platform Audit & Sales Report"))
        assertTrue(csv.contains("South Sudanese Pound (SSP)"))
    }

    @Test
    fun `role switching and offline mode toggling`() {
        val repo = JubaGoRepository()
        assertEquals(UserRole.CUSTOMER, repo.currentRole.value)

        repo.switchRole(UserRole.MERCHANT)
        assertEquals(UserRole.MERCHANT, repo.currentRole.value)

        val initialOffline = repo.isOfflineMode.value
        repo.toggleOfflineMode()
        assertEquals(!initialOffline, repo.isOfflineMode.value)
    }

    @Test
    fun `room to firestore sync helper initializes and manages offline queue`() = kotlinx.coroutines.test.runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = com.example.data.local.JubaGoDatabase.getDatabase(context)
        val dao = db.orderDao()

        val syncHelper = com.example.data.sync.RoomFirestoreSyncHelper(dao, context)
        assertNotNull(syncHelper)

        val entity = com.example.data.local.OrderEntity(
            id = "JB-TEST-1",
            serviceId = "food_1",
            title = "Kisra Test",
            categoryName = "FOOD",
            providerName = "Mama Nyandeng",
            customerName = "Deng",
            customerPhone = "+211 92 111 2233",
            deliveryAddress = "Hai Cinema",
            totalPriceSsp = 6500,
            totalPriceUsd = 5.2,
            paymentMethodName = "M_GURUSH",
            paymentReference = "MG-123456-SS",
            statusName = "CONFIRMED",
            courierName = "Garang",
            courierPhone = "+211 91 222 3344",
            courierVehicle = "Boda",
            courierRating = 4.9,
            orderedAt = "12:00 PM",
            etaMinutes = 15,
            liveProgress = 0.3f,
            riderLocation = "Custom Market",
            isSynced = false
        )
        dao.insertOrder(entity)
        val unsynced = dao.getUnsyncedOrders()
        assertTrue(unsynced.any { it.id == "JB-TEST-1" })

        dao.markAsSynced("JB-TEST-1")
        val remaining = dao.getUnsyncedOrders()
        assertTrue(remaining.none { it.id == "JB-TEST-1" })
    }

    @Test
    fun `navigation bottom destinations include Home, Orders, and Profile`() {
        val bottomItems = com.example.ui.navigation.JubaGoDestination.bottomNavDestinations
        assertEquals(3, bottomItems.size)
        assertEquals("home", bottomItems[0].route)
        assertEquals("orders", bottomItems[1].route)
        assertEquals("profile", bottomItems[2].route)

        assertEquals("nav_item_home", bottomItems[0].testTag)
        assertEquals("nav_item_orders", bottomItems[1].testTag)
        assertEquals("nav_item_profile", bottomItems[2].testTag)

        // Route resolution helper
        assertEquals(com.example.ui.navigation.JubaGoDestination.Home, com.example.ui.navigation.JubaGoDestination.fromRoute("home"))
        assertEquals(com.example.ui.navigation.JubaGoDestination.Orders, com.example.ui.navigation.JubaGoDestination.fromRoute("orders"))
        assertEquals(com.example.ui.navigation.JubaGoDestination.Profile, com.example.ui.navigation.JubaGoDestination.fromRoute("profile"))
        assertEquals(com.example.ui.navigation.JubaGoDestination.Tracking, com.example.ui.navigation.JubaGoDestination.fromRoute("tracking"))
        assertEquals(com.example.ui.navigation.JubaGoDestination.Chat, com.example.ui.navigation.JubaGoDestination.fromRoute("chat"))
        assertEquals(com.example.ui.navigation.JubaGoDestination.Merchant, com.example.ui.navigation.JubaGoDestination.fromRoute("merchant"))
    }

    @Test
    fun `navigation titles are localized in English and Juba Arabic`() {
        val engHome = com.example.data.localization.LocalizationManager.getString("nav_home", com.example.model.AppLanguage.ENGLISH)
        val engOrders = com.example.data.localization.LocalizationManager.getString("nav_orders", com.example.model.AppLanguage.ENGLISH)
        val engProfile = com.example.data.localization.LocalizationManager.getString("nav_profile", com.example.model.AppLanguage.ENGLISH)

        assertEquals("Home", engHome)
        assertEquals("Orders", engOrders)
        assertEquals("Profile", engProfile)

        val arabHome = com.example.data.localization.LocalizationManager.getString("nav_home", com.example.model.AppLanguage.JUBA_ARABIC)
        val arabOrders = com.example.data.localization.LocalizationManager.getString("nav_orders", com.example.model.AppLanguage.JUBA_ARABIC)
        val arabProfile = com.example.data.localization.LocalizationManager.getString("nav_profile", com.example.model.AppLanguage.JUBA_ARABIC)

        assertTrue(arabHome.isNotEmpty())
        assertTrue(arabOrders.isNotEmpty())
        assertTrue(arabProfile.isNotEmpty())
    }

    @Test
    fun `search filters travel and food service listings correctly`() = kotlinx.coroutines.test.runTest {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = com.example.ui.JubaGoViewModel(app)

        org.robolectric.shadows.ShadowLooper.idleMainLooper()
        val initialServices = viewModel.filteredServices.value
        assertTrue(initialServices.isNotEmpty())

        // Search for "food"
        viewModel.setSearchQuery("food")
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
        val foodServices = viewModel.filteredServices.value
        assertTrue("Food search must return food listings", foodServices.isNotEmpty())
        assertTrue("All results should match food intent or category", foodServices.all {
            it.category == ServiceCategory.FOOD ||
            it.category == ServiceCategory.GROCERY ||
            it.title.contains("food", ignoreCase = true) ||
            it.tags.any { tag -> tag.contains("food", ignoreCase = true) }
        })

        // Search for "travel"
        viewModel.setSearchQuery("travel")
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
        val travelServices = viewModel.filteredServices.value
        assertTrue("Travel search must return travel listings", travelServices.isNotEmpty())
        assertTrue("All results should match travel categories (cars, flights, hotels)", travelServices.all {
            it.category in listOf(ServiceCategory.CARS, ServiceCategory.FLIGHTS, ServiceCategory.HOTELS) ||
            it.title.contains("travel", ignoreCase = true) ||
            it.tags.any { tag -> tag.contains("travel", ignoreCase = true) }
        })

        // Search for specific Juba food: "kisra"
        viewModel.setSearchQuery("kisra")
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
        val kisraServices = viewModel.filteredServices.value
        assertTrue("Kisra search must return Kisra dish listing", kisraServices.isNotEmpty())
        assertTrue(kisraServices.any { it.title.contains("kisra", ignoreCase = true) || it.description.contains("kisra", ignoreCase = true) })

        // Clear search query restores all services
        viewModel.setSearchQuery("")
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
        assertEquals(initialServices.size, viewModel.filteredServices.value.size)
    }

    @Test
    fun `google maps live driver location and route waypoints calculate accurately in Juba`() {
        val repo = JubaGoRepository()
        val inTransitOrder = repo.orders.value.first { it.status == DeliveryStatus.ON_THE_WAY }
        assertNotNull(inTransitOrder)

        val pickup = com.example.ui.components.JubaGeoCoordinates.getPickupLocation(inTransitOrder)
        val destination = com.example.ui.components.JubaGeoCoordinates.getDestinationLocation(inTransitOrder)

        // Latitudes and Longitudes should be within Juba bounding box (~4.8°N, ~31.5°E to 31.6°E)
        assertTrue(pickup.latitude in 4.80..4.90)
        assertTrue(pickup.longitude in 31.50..31.65)
        assertTrue(destination.latitude in 4.80..4.90)
        assertTrue(destination.longitude in 31.50..31.65)

        val waypoints = com.example.ui.components.JubaGeoCoordinates.buildRouteWaypoints(pickup, destination)
        assertTrue("Route must contain waypoints", waypoints.size >= 4)

        // Driver position at 0% should be at pickup
        val startPos = com.example.ui.components.JubaGeoCoordinates.getLiveDriverPosition(pickup, destination, 0.0f)
        assertEquals(pickup.latitude, startPos.latitude, 0.0001)
        assertEquals(pickup.longitude, startPos.longitude, 0.0001)

        // Driver position at 100% should be at destination
        val endPos = com.example.ui.components.JubaGeoCoordinates.getLiveDriverPosition(pickup, destination, 1.0f)
        assertEquals(destination.latitude, endPos.latitude, 0.0001)
        assertEquals(destination.longitude, endPos.longitude, 0.0001)

        // Advancing driver progress updates order in repository
        val initialProgress = inTransitOrder.liveProgress
        repo.advanceLiveDriverProgress(inTransitOrder.id)
        val updatedOrder = repo.orders.value.first { it.id == inTransitOrder.id }
        assertTrue(updatedOrder.liveProgress > initialProgress)
        assertTrue(updatedOrder.riderCurrentLocationName.isNotEmpty())
    }

    @Test
    fun `reordering past order generates new booking with confirmed status and syncs`() {
        val repo = JubaGoRepository()
        val originalOrder = repo.orders.value.first()
        val initialCount = repo.orders.value.size

        val reordered = repo.reorderBooking(originalOrder)

        assertNotNull(reordered.id)
        assertTrue("Reordered ID must differ from original", reordered.id != originalOrder.id)
        assertEquals(originalOrder.title, reordered.title)
        assertEquals(originalOrder.serviceId, reordered.serviceId)
        assertEquals(originalOrder.totalPriceSsp, reordered.totalPriceSsp)
        assertEquals(DeliveryStatus.CONFIRMED, reordered.status)
        assertEquals(initialCount + 1, repo.orders.value.size)
        assertEquals(reordered.id, repo.selectedOrder.value?.id)
    }

    @Test
    fun `submitting star rating review updates service rating and records order feedback`() {
        val repo = JubaGoRepository()
        val foodService = repo.services.value.first { it.category == ServiceCategory.FOOD }
        val initialReviewCount = repo.reviews.value.size

        val review = repo.addReview(
            serviceId = foodService.id,
            authorName = "Bol Deng",
            rating = 5.0,
            comment = "Steaming hot Nile perch delivered in 20 minutes! Super fast rider.",
            orderId = "JB-8921",
            tags = listOf("⚡ Fast Delivery", "🍲 Hot & Fresh", "🛵 Courteous Rider")
        )

        assertNotNull(review.id)
        assertEquals(foodService.id, review.serviceId)
        assertEquals("JB-8921", review.orderId)
        assertEquals(5.0, review.rating, 0.01)
        assertTrue(review.tags.contains("⚡ Fast Delivery"))
        assertEquals(initialReviewCount + 1, repo.reviews.value.size)

        // Verify dynamic service rating update
        val updatedService = repo.services.value.first { it.id == foodService.id }
        assertTrue("Service review count should increase", updatedService.reviewCount >= foodService.reviewCount)
        assertTrue("Service rating should be valid range", updatedService.rating in 1.0..5.0)
    }

    @Test
    fun `payment gateway processes mobile money m-GURUSH authorization successfully`() = kotlinx.coroutines.runBlocking {
        val result = com.example.data.payment.PaymentGatewayService.processMobileMoneyPayment(
            orderId = "JB-9901",
            serviceId = "food_1",
            serviceTitle = "Fresh Nile Perch",
            amountSsp = 6500,
            amountUsd = 5.2,
            method = PaymentMethod.M_GURUSH,
            phoneNumber = "+211 92 123 4567",
            pin = "4444"
        )

        assertTrue("Payment must succeed", result is com.example.data.payment.PaymentResult.Success)
        val success = result as com.example.data.payment.PaymentResult.Success
        assertEquals("JB-9901", success.transaction.orderId)
        assertEquals(PaymentMethod.M_GURUSH, success.transaction.method)
        assertTrue(success.transaction.gatewayReference.startsWith("MG-"))
        assertEquals(com.example.data.payment.PaymentStatus.AUTHORIZED, success.transaction.status)
        assertTrue(success.transaction.escrowConfirmed)
    }

    @Test
    fun `payment gateway processes Stripe card payment successfully`() = kotlinx.coroutines.runBlocking {
        val result = com.example.data.payment.PaymentGatewayService.processStripePayment(
            orderId = "JB-9902",
            serviceId = "car_1",
            serviceTitle = "Toyota Land Cruiser",
            amountSsp = 145000,
            amountUsd = 120.0,
            cardNumber = "4242 4242 4242 4242",
            expiry = "12/28",
            cvv = "888",
            cardholderName = "Sarah Lado"
        )

        assertTrue("Stripe card charge must succeed", result is com.example.data.payment.PaymentResult.Success)
        val success = result as com.example.data.payment.PaymentResult.Success
        assertEquals("JB-9902", success.transaction.orderId)
        assertEquals(PaymentMethod.BANK_CARD, success.transaction.method)
        assertTrue(success.transaction.id.startsWith("ch_stripe_"))
        assertEquals(com.example.data.payment.PaymentStatus.AUTHORIZED, success.transaction.status)
    }

    @Test
    fun `payment gateway rejects invalid short PIN or invalid phone`() = kotlinx.coroutines.runBlocking {
        val badPinResult = com.example.data.payment.PaymentGatewayService.processMobileMoneyPayment(
            orderId = "JB-9903",
            serviceId = "food_1",
            serviceTitle = "Fresh Nile Perch",
            amountSsp = 3000,
            amountUsd = 2.5,
            method = PaymentMethod.MTN_MOMO,
            phoneNumber = "0912345678",
            pin = "12" // Too short (< 4)
        )
        assertTrue(badPinResult is com.example.data.payment.PaymentResult.Error)

        val badPhoneResult = com.example.data.payment.PaymentGatewayService.processMobileMoneyPayment(
            orderId = "JB-9903",
            serviceId = "food_1",
            serviceTitle = "Fresh Nile Perch",
            amountSsp = 3000,
            amountUsd = 2.5,
            method = PaymentMethod.MTN_MOMO,
            phoneNumber = "123", // Too short
            pin = "1234"
        )
        assertTrue(badPhoneResult is com.example.data.payment.PaymentResult.Error)
    }

    @Test
    fun `fcm notification channels creation registers all three required channels`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

        com.example.data.notification.FcmNotificationManager.createNotificationChannels(context)

        val orderChannel = notificationManager.getNotificationChannel(com.example.data.notification.FcmNotificationManager.CHANNEL_ORDER_UPDATES)
        val deliveryChannel = notificationManager.getNotificationChannel(com.example.data.notification.FcmNotificationManager.CHANNEL_DELIVERY_TRACKING)
        val promoChannel = notificationManager.getNotificationChannel(com.example.data.notification.FcmNotificationManager.CHANNEL_PROMOTIONS)

        assertNotNull("Order updates notification channel must exist", orderChannel)
        assertNotNull("Delivery tracking notification channel must exist", deliveryChannel)
        assertNotNull("Promotions notification channel must exist", promoChannel)

        assertEquals("Order Status Updates", orderChannel.name.toString())
        assertEquals("Delivery & Courier Progress", deliveryChannel.name.toString())
        assertEquals("Promotions & Special Deals", promoChannel.name.toString())
    }

    @Test
    fun `fcm token storage and payload simulation records successfully`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val testToken = "test_fcm_token_juba_2026_xyz"

        com.example.data.notification.FcmNotificationManager.storeToken(context, testToken)
        val retrieved = com.example.data.notification.FcmNotificationManager.getStoredToken(context)
        assertEquals(testToken, retrieved)

        com.example.data.notification.FcmNotificationManager.triggerSimulatedNotification(
            context = context,
            type = com.example.data.notification.PushNotificationType.DELIVERY_TRACKING,
            title = "Courier 2 Mins Away",
            body = "Garang Bol is reaching Hai Cinema",
            orderId = "JB-8921"
        )

        val recent = com.example.data.notification.FcmNotificationManager.recentNotifications.value
        assertTrue("Recent notifications should have at least 1 item", recent.isNotEmpty())
        assertEquals("Courier 2 Mins Away", recent.first().title)
        assertEquals(com.example.data.notification.PushNotificationType.DELIVERY_TRACKING, recent.first().type)
        assertEquals("JB-8921", recent.first().orderId)
    }

    @Test
    fun `user profile repository initializes with default Juba addresses and payment methods`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val authProvider = com.example.data.auth.UserAuthenticationProvider(context)
        val profileRepo = com.example.data.user.UserProfileRepository(context, authProvider)

        val addresses = profileRepo.addresses.value
        assertTrue("Initial addresses should not be empty", addresses.isNotEmpty())
        assertTrue("At least one address must be marked default", addresses.any { it.isDefault })
        assertTrue("Contains Hai Cinema address", addresses.any { it.area.contains("Hai Cinema") })

        val payments = profileRepo.paymentMethods.value
        assertTrue("Initial payment methods should not be empty", payments.isNotEmpty())
        assertTrue("Contains m-GURUSH wallet", payments.any { it.method == PaymentMethod.M_GURUSH })
        assertTrue("At least one payment method is default", payments.any { it.isDefault })
    }

    @Test
    fun `delivery address operations create update and set default correctly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val authProvider = com.example.data.auth.UserAuthenticationProvider(context)
        val profileRepo = com.example.data.user.UserProfileRepository(context, authProvider)

        val newAddress = com.example.model.DeliveryAddress(
            id = "test_munuki_1",
            label = "Munuki Family Home",
            streetAddress = "Block B, Plot 12",
            area = "Munuki",
            city = "Juba, South Sudan",
            landmark = "Behind Munuki Market",
            contactPhone = "+211 92 999 1111",
            isDefault = true
        )

        profileRepo.saveAddress(newAddress)
        val afterSave = profileRepo.addresses.value
        assertTrue("Must contain newly saved address", afterSave.any { it.id == "test_munuki_1" })
        assertEquals("Newly saved address must be default", "test_munuki_1", afterSave.first { it.isDefault }.id)

        // Set another address as default
        val anotherId = afterSave.first { it.id != "test_munuki_1" }.id
        profileRepo.setDefaultAddress(anotherId)
        val afterDefaultChange = profileRepo.addresses.value
        assertEquals("Another address must now be default", anotherId, afterDefaultChange.first { it.isDefault }.id)

        // Delete address
        profileRepo.deleteAddress("test_munuki_1")
        val afterDelete = profileRepo.addresses.value
        assertTrue("Deleted address should no longer exist", afterDelete.none { it.id == "test_munuki_1" })
    }

    @Test
    fun `payment method operations save and toggle default correctly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val authProvider = com.example.data.auth.UserAuthenticationProvider(context)
        val profileRepo = com.example.data.user.UserProfileRepository(context, authProvider)

        val newMethod = com.example.model.SavedPaymentMethod(
            id = "test_pm_digitel",
            method = PaymentMethod.DIGITEL_PAY,
            title = "Digitel Express Wallet",
            accountIdentifier = "+211 98 000 2222",
            accountHolder = "John Lado",
            isDefault = true
        )

        profileRepo.savePaymentMethod(newMethod)
        val methods = profileRepo.paymentMethods.value
        assertTrue("Must contain newly saved Digitel payment", methods.any { it.id == "test_pm_digitel" })
        assertEquals("Digitel must be default", "test_pm_digitel", methods.first { it.isDefault }.id)

        profileRepo.deletePaymentMethod("test_pm_digitel")
        val afterDelete = profileRepo.paymentMethods.value
        assertTrue("Deleted payment method should no longer exist", afterDelete.none { it.id == "test_pm_digitel" })
    }

    @Test
    fun `account details update reflects in repository and local session`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val authProvider = com.example.data.auth.UserAuthenticationProvider(context)
        val profileRepo = com.example.data.user.UserProfileRepository(context, authProvider)

        val result = profileRepo.updateAccountDetails(
            displayName = "Beny Wei",
            phoneNumber = "+211 92 888 9999",
            cityAndCountry = "Juba, Central Equatoria"
        )

        assertTrue(result.isSuccess)
        val updatedUser = result.getOrNull()
        assertNotNull(updatedUser)
        assertEquals("Beny Wei", updatedUser?.displayName)
        assertEquals("+211 92 888 9999", updatedUser?.phoneNumber)
        assertEquals("Juba, Central Equatoria", updatedUser?.cityAndCountry)

        val currentUser = authProvider.currentUser.value
        assertEquals("Beny Wei", currentUser?.displayName)
    }
}
