package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.sync.SyncStatus
import com.example.model.DeliveryStatus
import com.example.model.OrderBooking
import com.example.model.Review
import java.text.NumberFormat
import java.util.Locale

enum class OrderFilterTab(val title: String) {
    ALL("All Orders"),
    FAVORITES("Favorites ❤️"),
    ACTIVE("In Transit 🚚"),
    DELIVERED("Delivered ✅")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OrdersHistoryScreen(
    orders: List<OrderBooking>,
    reviews: List<Review> = emptyList(),
    favoriteIds: Set<String> = emptySet(),
    isRefreshing: Boolean = false,
    syncStatus: SyncStatus? = null,
    onTrackOrder: (OrderBooking) -> Unit,
    onExploreServices: () -> Unit,
    onToggleFavorite: (serviceId: String) -> Unit = {},
    onReorder: (OrderBooking) -> Unit = {},
    onLeaveReview: (OrderBooking) -> Unit = {},
    onRefreshOrders: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(OrderFilterTab.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var orderToReorderConfirm by remember { mutableStateOf<OrderBooking?>(null) }
    var expandedReceiptOrderId by remember { mutableStateOf<String?>(null) }

    // Filter orders based on tab and search
    val filteredOrders = orders.filter { order ->
        val matchesTab = when (selectedTab) {
            OrderFilterTab.ALL -> true
            OrderFilterTab.FAVORITES -> favoriteIds.contains(order.serviceId)
            OrderFilterTab.ACTIVE -> order.status != DeliveryStatus.DELIVERED && order.status != DeliveryStatus.CANCELLED
            OrderFilterTab.DELIVERED -> order.status == DeliveryStatus.DELIVERED
        }

        val q = searchQuery.trim().lowercase()
        val matchesSearch = q.isEmpty() ||
                order.title.lowercase().contains(q) ||
                order.providerName.lowercase().contains(q) ||
                order.id.lowercase().contains(q) ||
                order.deliveryAddress.lowercase().contains(q)

        matchesTab && matchesSearch
    }

    val favoritesCount = orders.count { favoriteIds.contains(it.serviceId) }
    val activeCount = orders.count { it.status != DeliveryStatus.DELIVERED && it.status != DeliveryStatus.CANCELLED }
    val deliveredCount = orders.count { it.status == DeliveryStatus.DELIVERED }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("orders_history_screen"),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 100.dp)
    ) {
        // 1. Header with Title & Cloud Firestore Sync Controls
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "My Orders & Bookings",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Cloud-synced with Firestore • Instant Reorder & Reviews",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                // Firestore Sync status badge & refresh button
                Surface(
                    onClick = onRefreshOrders,
                    shape = RoundedCornerShape(12.dp),
                    color = when (syncStatus) {
                        SyncStatus.SYNCING -> MaterialTheme.colorScheme.primaryContainer
                        SyncStatus.OFFLINE -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    modifier = Modifier.testTag("refresh_firestore_orders_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isRefreshing || syncStatus == SyncStatus.SYNCING) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = when (syncStatus) {
                                    SyncStatus.OFFLINE -> Icons.Default.CloudOff
                                    SyncStatus.SYNCED -> Icons.Default.CloudDone
                                    else -> Icons.Default.Sync
                                },
                                contentDescription = "Sync",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isRefreshing || syncStatus == SyncStatus.SYNCING) "Syncing..." else "Firestore",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // 2. Search Field
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search orders, meals, rides, ID...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.outline)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("orders_search_input")
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 3. Filter Chips: All, Favorites, Active, Delivered
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedTab == OrderFilterTab.ALL,
                    onClick = { selectedTab = OrderFilterTab.ALL },
                    label = { Text("All (${orders.size})", fontSize = 12.sp) },
                    modifier = Modifier.testTag("filter_tab_all")
                )
                FilterChip(
                    selected = selectedTab == OrderFilterTab.FAVORITES,
                    onClick = { selectedTab = OrderFilterTab.FAVORITES },
                    label = { Text("Favorites ($favoritesCount)", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFFEBEE),
                        selectedLabelColor = Color(0xFFC2185B)
                    ),
                    modifier = Modifier.testTag("filter_tab_favorites")
                )
                FilterChip(
                    selected = selectedTab == OrderFilterTab.ACTIVE,
                    onClick = { selectedTab = OrderFilterTab.ACTIVE },
                    label = { Text("Active ($activeCount)", fontSize = 12.sp) },
                    modifier = Modifier.testTag("filter_tab_active")
                )
                FilterChip(
                    selected = selectedTab == OrderFilterTab.DELIVERED,
                    onClick = { selectedTab = OrderFilterTab.DELIVERED },
                    label = { Text("Delivered ($deliveredCount)", fontSize = 12.sp) },
                    modifier = Modifier.testTag("filter_tab_delivered")
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 4. Empty State Handling
        if (filteredOrders.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (selectedTab == OrderFilterTab.FAVORITES) Icons.Default.Favorite else Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = if (selectedTab == OrderFilterTab.FAVORITES) Color(0xFFE53935) else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val emptyTitle = when {
                            searchQuery.isNotEmpty() -> "No matching orders found"
                            selectedTab == OrderFilterTab.FAVORITES -> "No Favorite Orders Yet"
                            selectedTab == OrderFilterTab.ACTIVE -> "No Active In-Transit Orders"
                            else -> "No bookings or orders yet"
                        }

                        val emptyDesc = when {
                            searchQuery.isNotEmpty() -> "Try searching with a different item name or clear your search term."
                            selectedTab == OrderFilterTab.FAVORITES -> "Tap the heart icon (❤️) on any past order to mark it as a favorite for quick 1-tap reordering!"
                            selectedTab == OrderFilterTab.ACTIVE -> "All your past orders have been completed. Place a new order to track live delivery."
                            else -> "Discover restaurants, hire 4x4 cars, or book flights across Juba and South Sudan."
                        }

                        Text(
                            text = emptyTitle,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = emptyDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (searchQuery.isNotEmpty()) {
                            OutlinedButton(onClick = { searchQuery = "" }) {
                                Text("Clear Search")
                            }
                        } else {
                            Button(
                                onClick = onExploreServices,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("empty_explore_services_button")
                            ) {
                                Text("Explore Juba Services")
                            }
                        }
                    }
                }
            }
        }

        // 5. Scrollable Orders List with Reorder & Review Actions
        items(filteredOrders, key = { it.id }) { order ->
            val isFavorite = favoriteIds.contains(order.serviceId)
            val sspFormatted = NumberFormat.getNumberInstance(Locale.US).format(order.totalPriceSsp)
            val isDelivered = order.status == DeliveryStatus.DELIVERED
            val isReceiptExpanded = expandedReceiptOrderId == order.id

            // Check if there is an existing review for this order or service
            val existingReview = reviews.firstOrNull { it.orderId == order.id || (it.serviceId == order.serviceId && it.authorName != "Amina Chol") }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 7.dp)
                    .testTag("order_history_card_${order.id}"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Top Row: Order ID, Category, Favorite Button, Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "#${order.id}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = order.category.displayName,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Favorite toggle heart button
                            IconButton(
                                onClick = { onToggleFavorite(order.serviceId) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .testTag("toggle_favorite_order_${order.id}")
                            ) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                                    tint = if (isFavorite) Color(0xFFE53935) else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // Status badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when (order.status) {
                                            DeliveryStatus.DELIVERED -> Color(0xFFE8F5E9)
                                            DeliveryStatus.ON_THE_WAY -> MaterialTheme.colorScheme.primaryContainer
                                            DeliveryStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer
                                            else -> Color(0xFFFFF3E0)
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = order.status.label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = when (order.status) {
                                        DeliveryStatus.DELIVERED -> Color(0xFF2E7D32)
                                        DeliveryStatus.ON_THE_WAY -> MaterialTheme.colorScheme.onPrimaryContainer
                                        DeliveryStatus.CANCELLED -> MaterialTheme.colorScheme.onErrorContainer
                                        else -> Color(0xFFE65100)
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Title & Provider
                    Text(
                        text = order.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2
                    )

                    Text(
                        text = "From: ${order.providerName}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Location & Courier details
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = order.deliveryAddress,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Courier: ${order.courierName} (${order.courierVehicle})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$sspFormatted SSP",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Existing Review Section (if user already reviewed this order)
                    if (existingReview != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFFF8E1),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Your Rating: ${existingReview.rating}★",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFF8D6E63)
                                        )
                                    }
                                    Text(
                                        text = "Firestore Synced ✓",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                                if (existingReview.comment.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "\"${existingReview.comment}\"",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                if (existingReview.tags.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        existingReview.tags.forEach { tag ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFFFFECB3))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(tag, fontSize = 10.sp, color = Color(0xFF5D4037))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Expandable Digital Receipt Details
                    AnimatedVisibility(
                        visible = isReceiptExpanded,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(12.dp)
                        ) {
                            Text("Official Digital Receipt", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Payment Gateway:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                Text(order.paymentMethod.title, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold))
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Reference Code:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                Text(order.paymentReference, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.secondary)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("USD Amount:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                Text("$${order.totalPriceUsd}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Placed At:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                Text(order.orderedAt, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))

                    // Bottom Action Bar: Reorder Favorite, Leave Review, Track GPS, Receipt Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Toggle receipt drawer
                        TextButton(
                            onClick = {
                                expandedReceiptOrderId = if (isReceiptExpanded) null else order.id
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isReceiptExpanded) "Hide Receipt" else "Receipt", fontSize = 11.sp)
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // If completed, allow leaving review / star-rating
                            if (isDelivered) {
                                OutlinedButton(
                                    onClick = { onLeaveReview(order) },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("leave_review_button_${order.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp),
                                        tint = Color(0xFFFFB300)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (existingReview != null) "Update Review" else "★ Review",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                // Live Track GPS for in-progress orders
                                OutlinedButton(
                                    onClick = { onTrackOrder(order) },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("track_button_${order.id}")
                                ) {
                                    Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Track GPS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Primary Action: Reorder Favorite / Reorder Button
                            Button(
                                onClick = { orderToReorderConfirm = order },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isFavorite) Color(0xFFD81B60) else MaterialTheme.colorScheme.primary
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("reorder_button_${order.id}")
                            ) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.Replay,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isFavorite) "Reorder Favorite" else "Reorder",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Reorder Confirmation Dialog
    orderToReorderConfirm?.let { order ->
        val ssp = NumberFormat.getNumberInstance(Locale.US).format(order.totalPriceSsp)
        AlertDialog(
            onDismissRequest = { orderToReorderConfirm = null },
            icon = {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            title = {
                Text("Confirm Reorder", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "Would you like to reorder \"${order.title}\" from ${order.providerName}?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Amount:", style = MaterialTheme.typography.labelSmall)
                                Text("$ssp SSP ($${order.totalPriceUsd})", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Payment:", style = MaterialTheme.typography.labelSmall)
                                Text(order.paymentMethod.title, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold))
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Delivery Landmark:", style = MaterialTheme.typography.labelSmall)
                                Text(order.deliveryAddress, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This will immediately push the new order to Cloud Firestore and dispatch a courier.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val ord = order
                        orderToReorderConfirm = null
                        onReorder(ord)
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_reorder_dialog_button")
                ) {
                    Text("Confirm & Reorder Now")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { orderToReorderConfirm = null },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
