package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.localization.LocalizationManager
import com.example.model.AppLanguage
import com.example.model.UserRole
import com.example.ui.JubaGoViewModel
import com.example.ui.components.BookingPaymentDialog
import com.example.ui.components.BusinessRegistrationDialog
import com.example.ui.components.LanguageSelectionDialog
import com.example.ui.components.NotificationsSheet
import com.example.ui.components.OnboardingWizardDialog
import com.example.ui.components.OrderReviewDialog
import com.example.ui.components.OrderSuccessDialog
import com.example.ui.components.ServiceDetailModal
import com.example.ui.components.UserAuthDialog
import com.example.ui.navigation.JubaGoDestination
import com.example.ui.navigation.navigateToDestination
import com.example.ui.screens.CustomerExploreScreen
import com.example.ui.screens.LiveTrackingScreen
import com.example.ui.screens.MerchantDashboardScreen
import com.example.ui.screens.OrdersHistoryScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SupportChatScreen
import com.example.ui.theme.JubaGoTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        com.example.data.notification.FcmNotificationManager.createNotificationChannels(this)
        com.example.data.notification.FcmNotificationManager.fetchAndStoreFcmToken(this)
        setContent {
            JubaGoTheme {
                JubaGoApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JubaGoApp(
    viewModel: JubaGoViewModel = viewModel()
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val isOffline by viewModel.isOfflineMode.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val selectedOrder by viewModel.selectedOrder.collectAsStateWithLifecycle()
    val reviews by viewModel.reviews.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()
    val analytics by viewModel.analytics.collectAsStateWithLifecycle()
    val filteredServices by viewModel.filteredServices.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedScope by viewModel.selectedScope.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val onlyVerified by viewModel.onlyVerified.collectAsStateWithLifecycle()

    val bookingItem by viewModel.bookingItem.collectAsStateWithLifecycle()
    val detailItem by viewModel.detailItem.collectAsStateWithLifecycle()
    val showNotificationsSheet by viewModel.showNotificationsSheet.collectAsStateWithLifecycle()
    val lastPlacedOrder by viewModel.lastPlacedOrder.collectAsStateWithLifecycle()
    val showBusinessRegistration by viewModel.showBusinessRegistration.collectAsStateWithLifecycle()
    val showOnboardingWizard by viewModel.showOnboardingWizard.collectAsStateWithLifecycle()
    val showAuthDialog by viewModel.showAuthDialog.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val showLanguageDialog by viewModel.showLanguageDialog.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val isRefreshingOrders by viewModel.isRefreshingOrders.collectAsStateWithLifecycle()
    val orderHistoryMessage by viewModel.orderHistoryMessage.collectAsStateWithLifecycle()
    val activeReviewOrder by viewModel.activeReviewOrder.collectAsStateWithLifecycle()
    val fcmToken by viewModel.fcmToken.collectAsStateWithLifecycle()
    val recentFcmNotifications by viewModel.recentFcmNotifications.collectAsStateWithLifecycle()
    val deliveryAddresses by viewModel.deliveryAddresses.collectAsStateWithLifecycle()
    val savedPaymentMethods by viewModel.savedPaymentMethods.collectAsStateWithLifecycle()

    val context = androidx.compose.ui.platform.LocalContext.current
    val notificationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        android.util.Log.d("MainActivity", "POST_NOTIFICATIONS permission granted: $isGranted")
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    var showRoleMenu by remember { mutableStateOf(false) }

    // Intercept back button for open dialogs/sheets
    val hasModalOpen = showLanguageDialog || showOnboardingWizard || showAuthDialog ||
            detailItem != null || bookingItem != null || showBusinessRegistration || activeReviewOrder != null

    BackHandler(enabled = hasModalOpen) {
        if (activeReviewOrder != null) {
            viewModel.closeReviewDialog()
        } else if (showLanguageDialog) {
            viewModel.closeLanguageDialog()
        } else if (showOnboardingWizard) {
            viewModel.closeOnboardingWizard()
        } else if (showAuthDialog) {
            viewModel.closeAuthDialog()
        } else if (showBusinessRegistration) {
            viewModel.closeBusinessRegistration()
        } else if (bookingItem != null) {
            viewModel.dismissBooking()
        } else if (detailItem != null) {
            viewModel.closeServiceDetail()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.testTag("app_top_bar_title")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.secondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "JG",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = Color.Black
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = LocalizationManager.getString("app_title", currentLanguage),
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = LocalizationManager.getString("app_country_badge", currentLanguage),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                },
                actions = {
                    // Quick Language Switcher: English <-> Juba Arabic (عربي جوبا)
                    Surface(
                        onClick = {
                            val nextLang = if (currentLanguage == AppLanguage.ENGLISH) AppLanguage.JUBA_ARABIC else AppLanguage.ENGLISH
                            viewModel.setLanguage(nextLang)
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = if (currentLanguage == AppLanguage.JUBA_ARABIC)
                            MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .testTag("topbar_language_quick_toggle")
                            .padding(end = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (currentLanguage == AppLanguage.JUBA_ARABIC) "🇸🇸 عربي" else "🇬🇧 EN",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = if (currentLanguage == AppLanguage.JUBA_ARABIC)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Quick Onboarding / App Tutorial Button
                    IconButton(
                        onClick = { viewModel.openOnboardingWizard() },
                        modifier = Modifier.testTag("topbar_onboarding_guide_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "Onboarding Guide & Tutorial",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Account & Sign-In Button (Navigates directly to Profile destination or opens Auth)
                    IconButton(
                        onClick = {
                            navController.navigateToDestination(JubaGoDestination.Profile.route)
                        },
                        modifier = Modifier.testTag("topbar_auth_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = if (currentUser != null) "Account: ${currentUser?.displayName}" else "Sign In / Profile",
                            tint = if (currentUser != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Role Switcher Dropdown
                    Box {
                        FilterChip(
                            selected = true,
                            onClick = { showRoleMenu = true },
                            label = {
                                Text(
                                    text = LocalizationManager.getRoleLabel(currentRole, currentLanguage),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (currentRole == UserRole.MERCHANT) Icons.Default.Business else Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (currentRole == UserRole.MERCHANT)
                                    MaterialTheme.colorScheme.secondaryContainer
                                else MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = if (currentRole == UserRole.MERCHANT)
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                else MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("role_switcher_chip")
                        )

                        DropdownMenu(
                            expanded = showRoleMenu,
                            onDismissRequest = { showRoleMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(LocalizationManager.getString("role_customer", currentLanguage)) },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                onClick = {
                                    viewModel.switchRole(UserRole.CUSTOMER)
                                    showRoleMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(LocalizationManager.getString("role_merchant", currentLanguage)) },
                                leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null) },
                                onClick = {
                                    viewModel.switchRole(UserRole.MERCHANT)
                                    showRoleMenu = false
                                    navController.navigateToDestination(JubaGoDestination.Merchant.route)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (currentLanguage == AppLanguage.ENGLISH) "عربي جوبا (Switch to Juba Arabic)" else "English (التبديل إلى الإنجليزية)") },
                                leadingIcon = { Text(if (currentLanguage == AppLanguage.ENGLISH) "🇸🇸" else "🇬🇧") },
                                onClick = {
                                    val next = if (currentLanguage == AppLanguage.ENGLISH) AppLanguage.JUBA_ARABIC else AppLanguage.ENGLISH
                                    viewModel.setLanguage(next)
                                    showRoleMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(LocalizationManager.getString("select_language", currentLanguage)) },
                                leadingIcon = { Text("🌐") },
                                onClick = {
                                    viewModel.openLanguageDialog()
                                    showRoleMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("App Feature Tour & Tutorial") },
                                leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                                onClick = {
                                    viewModel.openOnboardingWizard()
                                    showRoleMenu = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Jetpack Navigation Compose Bottom Navigation Bar
            // Manages transitions between Home, Orders, and Profile
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("main_navigation_bar")
            ) {
                JubaGoDestination.bottomNavDestinations.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    val activeOrdersCount = orders.count { it.status.ordinal in 1..3 }

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigateToDestination(screen.route)
                        },
                        icon = {
                            if (screen == JubaGoDestination.Orders && activeOrdersCount > 0) {
                                BadgedBox(badge = { Badge { Text("$activeOrdersCount") } }) {
                                    Icon(screen.icon, contentDescription = screen.defaultLabel)
                                }
                            } else {
                                Icon(screen.icon, contentDescription = screen.defaultLabel)
                            }
                        },
                        label = {
                            Text(
                                text = LocalizationManager.getString(screen.titleKey, currentLanguage).ifEmpty { screen.defaultLabel },
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag(screen.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Jetpack Navigation Compose NavHost
            NavHost(
                navController = navController,
                startDestination = JubaGoDestination.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Home Destination (Services Discovery, Categories, Bookings, Search)
                composable(JubaGoDestination.Home.route) {
                    CustomerExploreScreen(
                        services = filteredServices,
                        selectedCategory = selectedCategory,
                        selectedScope = selectedScope,
                        searchQuery = searchQuery,
                        onlyVerified = onlyVerified,
                        isOffline = isOffline,
                        unreadNotificationCount = notifications.size,
                        onCategorySelected = { viewModel.setCategory(it) },
                        onScopeSelected = { viewModel.setCoverageScope(it) },
                        onOpenBusinessRegistration = { viewModel.openBusinessRegistration() },
                        onSearchQueryChanged = { viewModel.setSearchQuery(it) },
                        onToggleVerified = { viewModel.toggleOnlyVerified() },
                        onToggleOffline = { viewModel.toggleOfflineMode() },
                        onOpenNotifications = { viewModel.toggleNotifications(true) },
                        onServiceClicked = { viewModel.openServiceDetail(it) },
                        onQuickBookClicked = { viewModel.startBooking(it) },
                        onOpenOnboardingTour = { viewModel.openOnboardingWizard() },
                        currentLanguage = currentLanguage
                    )
                }

                // 2. Orders Destination (Order History, Statuses, Real-time tracking trigger)
                composable(JubaGoDestination.Orders.route) {
                    OrdersHistoryScreen(
                        orders = orders,
                        reviews = reviews,
                        favoriteIds = favoriteIds,
                        isRefreshing = isRefreshingOrders,
                        syncStatus = viewModel.syncStatus?.value,
                        onTrackOrder = { order ->
                            viewModel.selectOrderToTrack(order)
                            navController.navigateToDestination(JubaGoDestination.Tracking.route)
                        },
                        onExploreServices = {
                            navController.navigateToDestination(JubaGoDestination.Home.route)
                        },
                        onToggleFavorite = { serviceId ->
                            viewModel.toggleFavorite(serviceId)
                        },
                        onReorder = { order ->
                            viewModel.reorderBooking(order)
                        },
                        onLeaveReview = { order ->
                            viewModel.openReviewForOrder(order)
                        },
                        onRefreshOrders = {
                            viewModel.refreshOrdersFromFirestore()
                        }
                    )
                }

                // 3. Profile Destination (User Account, Role Switcher, Quick Hub, Settings, Tour)
                composable(JubaGoDestination.Profile.route) {
                    val activeOrdersCount = orders.count { it.status.ordinal in 1..3 }
                    ProfileScreen(
                        currentUser = currentUser,
                        currentRole = currentRole,
                        currentLanguage = currentLanguage,
                        isOffline = isOffline,
                        syncStatus = viewModel.syncStatus?.value,
                        activeOrdersCount = activeOrdersCount,
                        deliveryAddresses = deliveryAddresses,
                        savedPaymentMethods = savedPaymentMethods,
                        onOpenAuth = { viewModel.openAuthDialog() },
                        onSignOut = {
                            scope.launch {
                                viewModel.authProvider.signOut()
                            }
                        },
                        onSwitchRole = { newRole ->
                            viewModel.switchRole(newRole)
                            if (newRole == UserRole.MERCHANT) {
                                navController.navigateToDestination(JubaGoDestination.Merchant.route)
                            }
                        },
                        onOpenLanguageDialog = { viewModel.openLanguageDialog() },
                        onToggleOffline = { viewModel.toggleOfflineMode() },
                        onOpenBusinessRegistration = { viewModel.openBusinessRegistration() },
                        onOpenOnboardingTour = { viewModel.openOnboardingWizard() },
                        onNavigateToOrders = {
                            navController.navigateToDestination(JubaGoDestination.Orders.route)
                        },
                        onNavigateToTracking = {
                            navController.navigateToDestination(JubaGoDestination.Tracking.route)
                        },
                        onNavigateToChat = {
                            navController.navigateToDestination(JubaGoDestination.Chat.route)
                        },
                        onNavigateToMerchant = {
                            navController.navigateToDestination(JubaGoDestination.Merchant.route)
                        },
                        onOpenNotifications = {
                            viewModel.toggleNotifications(true)
                        },
                        onUpdateAccountDetails = { name, phone, city ->
                            viewModel.updateAccountDetails(name, phone, city)
                        },
                        onSaveDeliveryAddress = { addr ->
                            viewModel.saveDeliveryAddress(addr)
                        },
                        onSetDefaultAddress = { id ->
                            viewModel.setDefaultDeliveryAddress(id)
                        },
                        onDeleteAddress = { id ->
                            viewModel.deleteDeliveryAddress(id)
                        },
                        onSavePaymentMethod = { pm ->
                            viewModel.savePaymentMethod(pm)
                        },
                        onSetDefaultPaymentMethod = { id ->
                            viewModel.setDefaultPaymentMethod(id)
                        },
                        onDeletePaymentMethod = { id ->
                            viewModel.deletePaymentMethod(id)
                        }
                    )
                }

                // 4. Live GPS Tracking Destination
                composable(JubaGoDestination.Tracking.route) {
                    LiveTrackingScreen(
                        order = selectedOrder,
                        allOrders = orders,
                        isOffline = isOffline,
                        onSelectOrder = { viewModel.selectOrderToTrack(it) },
                        onNavigateToChat = {
                            navController.navigateToDestination(JubaGoDestination.Chat.route)
                        },
                        onSimulateStep = {
                            selectedOrder?.let { viewModel.advanceDriverLocation(it.id) }
                        }
                    )
                }

                // 5. Support Chat Destination
                composable(JubaGoDestination.Chat.route) {
                    SupportChatScreen(
                        messages = chatMessages,
                        onSendMessage = { viewModel.sendChatMessage(it) }
                    )
                }

                // 6. Merchant Dashboard Destination
                composable(JubaGoDestination.Merchant.route) {
                    MerchantDashboardScreen(
                        analytics = analytics,
                        orders = orders,
                        services = filteredServices,
                        auditLogs = auditLogs,
                        onToggleServiceAvailability = { viewModel.toggleMerchantServiceAvailability(it) },
                        onUpdateOrderStatus = { id, status -> viewModel.updateOrderStatus(id, status) },
                        onSwitchRole = { viewModel.switchRole(it) },
                        onGetExportCsv = { viewModel.getExportReportCsv() },
                        onOpenBusinessRegistration = { viewModel.openBusinessRegistration() }
                    )
                }
            }
        }

        // Service Details Sheet
        if (detailItem != null) {
            ServiceDetailModal(
                service = detailItem!!,
                reviews = reviews,
                onDismiss = { viewModel.closeServiceDetail() },
                onBookNow = {
                    viewModel.closeServiceDetail()
                    viewModel.startBooking(it)
                },
                onSubmitReview = { rating, comment ->
                    viewModel.submitReview(detailItem!!.id, "Juba Resident", rating, comment)
                }
            )
        }

        // Booking & Payment Modal
        if (bookingItem != null) {
            BookingPaymentDialog(
                service = bookingItem!!,
                onDismiss = { viewModel.dismissBooking() },
                onConfirmOrder = { name, phone, address, method ->
                    viewModel.submitBooking(name, phone, address, method)
                }
            )
        }

        // Notifications Modal
        if (showNotificationsSheet) {
            NotificationsSheet(
                notifications = notifications,
                fcmToken = fcmToken,
                recentFcmPayloads = recentFcmNotifications,
                onTriggerTestNotification = { type, title, body, orderId ->
                    viewModel.triggerTestPushNotification(type, title, body, orderId)
                },
                onClearNotifications = { viewModel.clearNotifications() },
                onDismiss = { viewModel.toggleNotifications(false) }
            )
        }

        // Order Success Confirmation Modal
        if (lastPlacedOrder != null) {
            OrderSuccessDialog(
                order = lastPlacedOrder!!,
                onTrackNow = {
                    viewModel.dismissSuccessDialog(navigateToTracking = true)
                    navController.navigateToDestination(JubaGoDestination.Tracking.route)
                },
                onDismiss = {
                    viewModel.dismissSuccessDialog(navigateToTracking = false)
                    navController.navigateToDestination(JubaGoDestination.Orders.route)
                }
            )
        }

        // Business & Provider Registration Modal
        if (showBusinessRegistration) {
            BusinessRegistrationDialog(
                onDismiss = { viewModel.closeBusinessRegistration() },
                onRegister = { viewModel.registerBusiness(it) }
            )
        }

        // Onboarding Wizard Modal
        if (showOnboardingWizard) {
            OnboardingWizardDialog(
                onDismiss = { viewModel.closeOnboardingWizard() },
                onExploreServices = {
                    viewModel.closeOnboardingWizard()
                    navController.navigateToDestination(JubaGoDestination.Home.route)
                },
                onRegisterBusiness = {
                    viewModel.closeOnboardingWizard()
                    viewModel.openBusinessRegistration()
                },
                onOpenAuth = {
                    viewModel.closeOnboardingWizard()
                    viewModel.openAuthDialog()
                }
            )
        }

        // User Authentication Modal (Google Sign-In, Email/Phone, Profile)
        if (showAuthDialog) {
            UserAuthDialog(
                authProvider = viewModel.authProvider,
                currentUser = currentUser,
                authState = authState,
                onDismiss = { viewModel.closeAuthDialog() },
                onSuccess = {
                    viewModel.closeAuthDialog()
                    viewModel.refreshAnalytics()
                }
            )
        }

        // Language Selection Dialog
        if (showLanguageDialog) {
            LanguageSelectionDialog(
                currentLanguage = currentLanguage,
                onDismiss = { viewModel.closeLanguageDialog() },
                onLanguageSelected = { viewModel.setLanguage(it) }
            )
        }

        // Order Star-Rating & Review Dialog (stores reviews in Cloud Firestore)
        if (activeReviewOrder != null) {
            OrderReviewDialog(
                order = activeReviewOrder!!,
                onDismiss = { viewModel.closeReviewDialog() },
                onSubmitReview = { rating, comment, tags, authorName ->
                    viewModel.submitOrderReview(
                        order = activeReviewOrder!!,
                        rating = rating,
                        comment = comment,
                        tags = tags,
                        authorName = authorName
                    )
                },
                defaultAuthorName = currentUser?.displayName ?: "Verified Resident"
            )
        }
    }
}
