package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.localization.LocalizationManager
import com.example.model.AppLanguage
import com.example.model.CoverageScope
import com.example.model.ServiceCategory
import com.example.model.ServiceItem
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerExploreScreen(
    services: List<ServiceItem>,
    selectedCategory: ServiceCategory,
    selectedScope: CoverageScope,
    searchQuery: String,
    onlyVerified: Boolean,
    isOffline: Boolean,
    unreadNotificationCount: Int,
    onCategorySelected: (ServiceCategory) -> Unit,
    onScopeSelected: (CoverageScope) -> Unit,
    onOpenBusinessRegistration: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onToggleVerified: () -> Unit,
    onToggleOffline: () -> Unit,
    onOpenNotifications: () -> Unit,
    onServiceClicked: (ServiceItem) -> Unit,
    onQuickBookClicked: (ServiceItem) -> Unit,
    onOpenOnboardingTour: (() -> Unit)? = null,
    currentLanguage: AppLanguage = AppLanguage.ENGLISH,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("customer_explore_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Top Header Location & Actions
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Location dropdown/display
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.testTag("location_indicator")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Current Delivery Location",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = "Hai Cinema, Juba (SSD)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Offline toggle & Notification bell
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Offline button
                        FilterChip(
                            selected = isOffline,
                            onClick = onToggleOffline,
                            label = {
                                Text(
                                    if (isOffline) "Offline" else "Online",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            leadingIcon = {
                                if (isOffline) {
                                    Icon(Icons.Default.WifiOff, contentDescription = null, modifier = Modifier.size(14.dp))
                                } else {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFFE082),
                                selectedLabelColor = Color(0xFF5D4037)
                            ),
                            modifier = Modifier.testTag("offline_toggle_chip")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Notification Icon
                        IconButton(
                            onClick = onOpenNotifications,
                            modifier = Modifier.testTag("notifications_bell_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (unreadNotificationCount > 0) {
                                        Badge(containerColor = MaterialTheme.colorScheme.error) {
                                            Text("$unreadNotificationCount")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Top Search Bar for Filtering Travel & Food Service Listings
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("home_top_search_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChanged,
                        placeholder = {
                            Text(
                                text = LocalizationManager.getString("search_placeholder", currentLanguage),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                maxLines = 1
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search travel and food",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { onSearchQueryChanged("") },
                                    modifier = Modifier.testTag("clear_search_button")
                                ) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Clear search",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_top_search_bar")
                            .testTag("search_services_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Travel and Food Category Filter Shortcuts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // All Services reset
                        FilterChip(
                            selected = selectedCategory == ServiceCategory.ALL && searchQuery.isEmpty(),
                            onClick = {
                                onCategorySelected(ServiceCategory.ALL)
                                if (searchQuery.isNotEmpty()) onSearchQueryChanged("")
                            },
                            label = { Text("All", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("quick_filter_all")
                        )

                        // Quick Food Filter
                        FilterChip(
                            selected = selectedCategory == ServiceCategory.FOOD,
                            onClick = {
                                if (selectedCategory == ServiceCategory.FOOD) {
                                    onCategorySelected(ServiceCategory.ALL)
                                } else {
                                    onCategorySelected(ServiceCategory.FOOD)
                                }
                            },
                            label = { Text("Food", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            leadingIcon = {
                                Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(13.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("quick_filter_food")
                        )

                        // Quick Cars / Travel Filter
                        FilterChip(
                            selected = selectedCategory == ServiceCategory.CARS,
                            onClick = {
                                if (selectedCategory == ServiceCategory.CARS) {
                                    onCategorySelected(ServiceCategory.ALL)
                                } else {
                                    onCategorySelected(ServiceCategory.CARS)
                                }
                            },
                            label = { Text("Cars", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            leadingIcon = {
                                Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(13.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("quick_filter_cars")
                        )

                        // Quick Flights Travel Filter
                        FilterChip(
                            selected = selectedCategory == ServiceCategory.FLIGHTS,
                            onClick = {
                                if (selectedCategory == ServiceCategory.FLIGHTS) {
                                    onCategorySelected(ServiceCategory.ALL)
                                } else {
                                    onCategorySelected(ServiceCategory.FLIGHTS)
                                }
                            },
                            label = { Text("Flights", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            leadingIcon = {
                                Icon(Icons.Default.Flight, contentDescription = null, modifier = Modifier.size(13.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("quick_filter_flights")
                        )

                        // Quick Hotels Travel Filter
                        FilterChip(
                            selected = selectedCategory == ServiceCategory.HOTELS,
                            onClick = {
                                if (selectedCategory == ServiceCategory.HOTELS) {
                                    onCategorySelected(ServiceCategory.ALL)
                                } else {
                                    onCategorySelected(ServiceCategory.HOTELS)
                                }
                            },
                            label = { Text("Hotels", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            leadingIcon = {
                                Icon(Icons.Default.Hotel, contentDescription = null, modifier = Modifier.size(13.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("quick_filter_hotels")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Result summary and Verified switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${services.size} listings found",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (searchQuery.isNotBlank() || selectedCategory != ServiceCategory.ALL) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "• Reset",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier
                                        .clickable {
                                            onSearchQueryChanged("")
                                            onCategorySelected(ServiceCategory.ALL)
                                        }
                                        .testTag("reset_search_filter_button")
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = null,
                                tint = Color(0xFF1E88E5),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = LocalizationManager.getString("verified_only", currentLanguage),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Switch(
                                checked = onlyVerified,
                                onCheckedChange = { onToggleVerified() },
                                modifier = Modifier
                                    .testTag("verified_only_switch")
                            )
                        }
                    }
                }
            }
        }

        // Offline Banner Alert
        if (isOffline) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFF3CD))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WifiOff, contentDescription = null, tint = Color(0xFF856404), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Offline Mode: Browsing local cached listings. Orders will sync automatically.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF856404)
                        )
                    }
                }
            }
        }

        // Hero Banner Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("hero_banner_card"),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(170.dp)) {
                    // Try to display the generated hero banner, fallback to gradient if needed
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_banner),
                        contentDescription = "JubaGo Super-App Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Gradient overlay for contrast
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0xCC0D5C3A)
                                    )
                                )
                            )
                    )

                    // Banner Text
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = LocalizationManager.getString("hero_title", currentLanguage),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color(0xFFFEE8A3)
                        )
                        Text(
                            text = LocalizationManager.getString("hero_subtitle", currentLanguage),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = Color.White
                        )
                        Text(
                            text = LocalizationManager.getString("hero_tagline", currentLanguage),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFE0E0E0)
                        )
                        if (onOpenOnboardingTour != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                onClick = onOpenOnboardingTour,
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.testTag("hero_tour_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = LocalizationManager.getString("hero_tutorial_button", currentLanguage),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Business Registration Onboarding Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("business_onboarding_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Register Your Business & Services",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Shops, apartments, hotels, car rentals & restaurants: provide services across South Sudan & beyond Africa.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = onOpenBusinessRegistration,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("open_business_reg_button")
                    ) {
                        Text("Register", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Coverage Scope / Geographic Destination Filter
        item {
            Text(
                text = "Coverage & Destination Scope",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 6.dp)
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("scope_filter_row"),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(CoverageScope.values()) { scope ->
                    val isSelected = selectedScope == scope
                    FilterChip(
                        selected = isSelected,
                        onClick = { onScopeSelected(scope) },
                        label = { Text(LocalizationManager.getScopeName(scope, currentLanguage), fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Public,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondary,
                            selectedLabelColor = Color.Black,
                            selectedLeadingIconColor = Color.Black
                        ),
                        modifier = Modifier.testTag("scope_chip_${scope.name.lowercase()}")
                    )
                }
            }
        }

        // Service Category Filter Chips
        item {
            Text(
                text = "Explore Services & Bookings",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 6.dp)
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("category_filter_row"),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ServiceCategory.values()) { category ->
                    val isSelected = selectedCategory == category
                    val icon = when (category) {
                        ServiceCategory.ALL -> Icons.Default.Apps
                        ServiceCategory.CARS -> Icons.Default.DirectionsCar
                        ServiceCategory.FLIGHTS -> Icons.Default.Flight
                        ServiceCategory.HOTELS -> Icons.Default.Hotel
                        ServiceCategory.FOOD -> Icons.Default.Restaurant
                        ServiceCategory.GROCERY -> Icons.Default.ShoppingCart
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategorySelected(category) },
                        label = { Text(LocalizationManager.getCategoryName(category, currentLanguage), fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        leadingIcon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = category.displayName,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        ),
                        modifier = Modifier.testTag("category_chip_${category.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Empty state
        if (services.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No matching services found in Juba",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Try adjusting your search or category filter",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }

        // List of Service Items
        items(services, key = { it.id }) { service ->
            ServiceCard(
                service = service,
                onClick = { onServiceClicked(service) },
                onQuickBook = { onQuickBookClicked(service) },
                currentLanguage = currentLanguage
            )
        }
    }
}

@Composable
fun ServiceCard(
    service: ServiceItem,
    onClick: () -> Unit,
    onQuickBook: () -> Unit,
    currentLanguage: AppLanguage = AppLanguage.ENGLISH
) {
    val sspFormatted = NumberFormat.getNumberInstance(Locale.US).format(service.priceSsp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() }
            .testTag("service_card_${service.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Category & Availability Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = service.category.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (service.badge != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = service.badge,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Provider Live Availability indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (service.isAvailable) Color(0xFF2E7D32) else Color(0xFFC62828))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (service.isAvailable) "Available Now" else "Busy / Out of Stock",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (service.isAvailable) Color(0xFF2E7D32) else Color(0xFFC62828),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = service.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Provider & Location
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = service.providerName,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
                if (service.isVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified Provider",
                        tint = Color(0xFF1E88E5),
                        modifier = Modifier.size(15.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "• ${service.location}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1
                )
            }

            if (service.coverageScope != CoverageScope.JUBA_LOCAL) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(service.coverageScope.badgeColorHex).copy(alpha = 0.12f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "🌐 ${service.coverageScope.displayName} • ${service.country}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = Color(service.coverageScope.badgeColorHex)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Price & Rating & CTA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$sspFormatted SSP",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = " ${service.priceUnit}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFD4AF37), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${service.rating} (${service.reviewCount})",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        if (service.etaMinutes > 0) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "~${service.etaMinutes} mins",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                Button(
                    onClick = onQuickBook,
                    enabled = service.isAvailable,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("quick_book_${service.id}")
                ) {
                    Text(
                        text = if (currentLanguage == AppLanguage.JUBA_ARABIC) {
                            when (service.category) {
                                ServiceCategory.CARS -> "إحجز مشوار"
                                ServiceCategory.FLIGHTS -> "إحجز مقعد"
                                ServiceCategory.HOTELS -> "إحجز غرفة"
                                ServiceCategory.FOOD -> "أطلب أكل"
                                else -> "أطلب هسة"
                            }
                        } else {
                            when (service.category) {
                                ServiceCategory.CARS -> "Ride / Rent"
                                ServiceCategory.FLIGHTS -> "Book Seat"
                                ServiceCategory.HOTELS -> "Reserve"
                                ServiceCategory.FOOD -> "Order Food"
                                else -> "Order Now"
                            }
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
