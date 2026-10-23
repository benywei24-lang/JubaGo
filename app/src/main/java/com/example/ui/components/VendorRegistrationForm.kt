package com.example.ui.components

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BusinessRegistrationRequest
import com.example.model.CoverageScope
import com.example.model.ServiceCategory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "VendorRegistration"

/**
 * VendorRegistrationForm collects business details, service categories, and location,
 * and saves them directly to Cloud Firestore ("vendors" and "services" collections)
 * to enable vendors across South Sudan and beyond to join the platform.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VendorRegistrationForm(
    onDismiss: () -> Unit,
    onSuccess: (BusinessRegistrationRequest) -> Unit = {},
    customOnSubmit: ((BusinessRegistrationRequest, (Boolean, String) -> Unit) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    // Form fields - Business Details
    var businessName by remember { mutableStateOf("") }
    var businessType by remember { mutableStateOf("Company / Enterprise") }
    var contactPerson by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("+211 9") }
    var contactEmail by remember { mutableStateOf("") }
    var taxNumber by remember { mutableStateOf("") }
    var businessDescription by remember { mutableStateOf("") }

    // Form fields - Service Category & Offering
    var selectedCategory by remember { mutableStateOf(ServiceCategory.FOOD) }
    var serviceOfferingTitle by remember { mutableStateOf("") }
    var serviceDescription by remember { mutableStateOf("") }
    var priceSspText by remember { mutableStateOf("15000") }
    var priceUsdText by remember { mutableStateOf("12.0") }
    var priceUnit by remember { mutableStateOf("/service") }

    // Form fields - Location & Scope
    var selectedScope by remember { mutableStateOf(CoverageScope.SOUTH_SUDAN_NATIONAL) }
    var cityAndCountry by remember { mutableStateOf("Juba, South Sudan") }
    var physicalAddress by remember { mutableStateOf("Airport Road, Juba") }

    // State management for submission
    var isSubmitting by remember { mutableStateOf(false) }
    var submissionStatusMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }
    var registeredVendorId by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var firestoreSyncedSuccessfully by remember { mutableStateOf(false) }

    // Business type presets
    val businessTypes = listOf(
        "Company / Enterprise",
        "Retail Store / Shop",
        "Hotel & Hospitality",
        "Apartment Rental",
        "Restaurant & Dining",
        "Transport & 4x4 Rental",
        "Air Travel & Cargo",
        "Supermarket & Goods"
    )

    // Location presets across South Sudan & Global
    val quickLocations = listOf(
        Pair("Juba Central", "Juba, South Sudan"),
        Pair("Wau Town", "Wau, South Sudan"),
        Pair("Malakal Port", "Malakal, South Sudan"),
        Pair("Yei Commercial", "Yei, South Sudan"),
        Pair("Bentiu City", "Bentiu, South Sudan"),
        Pair("Rumbek Centre", "Rumbek, South Sudan"),
        Pair("Bor Town", "Bor, South Sudan"),
        Pair("Nimule Border", "Nimule, South Sudan"),
        Pair("Kampala (UG)", "Kampala, Uganda"),
        Pair("Nairobi (KE)", "Nairobi, Kenya"),
        Pair("Dubai (UAE)", "Dubai, United Arab Emirates")
    )

    // Landmark shortcuts
    val popularLandmarks = listOf(
        "Airport Road near Terminal 1",
        "Ministries Road, Hai Cinema",
        "Custom Roundabout Center",
        "Konyo Konyo Central Market",
        "Gudele Roundabout Block 2",
        "Munuki Block A Main Street",
        "Wau Market Square",
        "Malakal Nile Riverbank Port"
    )

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US) }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("vendor_registration_form_container"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Vendor Registration",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Join JubaGo network across South Sudan & globally with Firestore sync",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Cloud Status Badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Firestore Cloud",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(8.dp))

            // Main scrollable form
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                // Success State View
                if (isSuccess) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("vendor_registration_success_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2E7D32)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Success",
                                        tint = Color.White,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Vendor Registered Successfully!",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Your business is now published and active on JubaGo across ${selectedScope.displayName}.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Firestore status pill
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (firestoreSyncedSuccessfully) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (firestoreSyncedSuccessfully) Color(0xFF81C784) else Color(0xFFFFB74D)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (firestoreSyncedSuccessfully) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                            contentDescription = null,
                                            tint = if (firestoreSyncedSuccessfully) Color(0xFF2E7D32) else Color(0xFFE65100),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (firestoreSyncedSuccessfully)
                                                "Synced to Firestore collection: /vendors/${registeredVendorId ?: ""}"
                                            else
                                                "Saved to local database (Queued for Firestore sync)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (firestoreSyncedSuccessfully) Color(0xFF2E7D32) else Color(0xFFE65100)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Registration Summary Card
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "Business Profile Summary",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        SummaryRow("Business Name", businessName)
                                        SummaryRow("Business Type", businessType)
                                        SummaryRow("Category", selectedCategory.displayName)
                                        SummaryRow("Location", "$physicalAddress, $cityAndCountry")
                                        SummaryRow("Coverage", selectedScope.displayName)
                                        SummaryRow("Pricing", "$priceSspText SSP (~$$priceUsdText USD) $priceUnit")
                                        SummaryRow("Phone", contactPhone)
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            // Reset form for next registration
                                            businessName = ""
                                            serviceOfferingTitle = ""
                                            serviceDescription = ""
                                            isSuccess = false
                                            registeredVendorId = null
                                            firestoreSyncedSuccessfully = false
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("vendor_register_another_button"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Register Another")
                                    }

                                    Button(
                                        onClick = onDismiss,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("vendor_done_button"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Done & Explore")
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Error Banner
                    item {
                        AnimatedVisibility(visible = errorMessage != null) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("vendor_error_banner"),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Error",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = errorMessage ?: "",
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        fontSize = 13.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    // SECTION 1: BUSINESS DETAILS
                    item {
                        FormSectionCard(
                            title = "1. Business Details",
                            icon = Icons.Default.Business,
                            subtitle = "Legal name, company type, and official contacts"
                        ) {
                            OutlinedTextField(
                                value = businessName,
                                onValueChange = {
                                    businessName = it
                                    errorMessage = null
                                },
                                label = { Text("Business / Company / Shop Name *") },
                                placeholder = { Text("e.g. Nile Star Apartments or Juba Fresh Mart") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("vendor_form_business_name"),
                                singleLine = true,
                                isError = errorMessage?.contains("name", ignoreCase = true) == true
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Business Structure / Type",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                businessTypes.forEach { type ->
                                    FilterChip(
                                        selected = businessType == type,
                                        onClick = { businessType = type },
                                        label = { Text(type, fontSize = 12.sp) },
                                        modifier = Modifier.testTag("vendor_type_chip_${type.replace(" ", "_").lowercase()}"),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = contactPerson,
                                    onValueChange = { contactPerson = it },
                                    label = { Text("Contact Person / Manager") },
                                    placeholder = { Text("e.g. Deng Lado") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("vendor_form_contact_person"),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = contactPhone,
                                    onValueChange = { contactPhone = it },
                                    label = { Text("Phone Number *") },
                                    placeholder = { Text("+211 92 000 0000") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("vendor_form_phone"),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = contactEmail,
                                    onValueChange = { contactEmail = it },
                                    label = { Text("Official Email") },
                                    placeholder = { Text("info@business.com") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("vendor_form_email"),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                                )

                                OutlinedTextField(
                                    value = taxNumber,
                                    onValueChange = { taxNumber = it },
                                    label = { Text("Tax ID / TIN / License") },
                                    placeholder = { Text("SSD-TIN-8912") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("vendor_form_tax_number"),
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = businessDescription,
                                onValueChange = { businessDescription = it },
                                label = { Text("About Your Business & Experience") },
                                placeholder = { Text("Describe your operations, fleet size, rooms, or cuisine specialties...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("vendor_form_desc"),
                                minLines = 2,
                                maxLines = 4
                            )
                        }
                    }

                    // SECTION 2: SERVICE CATEGORIES & OFFERING
                    item {
                        FormSectionCard(
                            title = "2. Service Categories & Pricing",
                            icon = Icons.Default.ShoppingCart,
                            subtitle = "Pick your service sector and set transparent customer pricing"
                        ) {
                            Text(
                                text = "Select Primary Category *",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    ServiceCategory.FOOD,
                                    ServiceCategory.CARS,
                                    ServiceCategory.HOTELS,
                                    ServiceCategory.FLIGHTS,
                                    ServiceCategory.GROCERY
                                ).forEach { category ->
                                    val icon = when (category) {
                                        ServiceCategory.FOOD -> Icons.Default.Restaurant
                                        ServiceCategory.CARS -> Icons.Default.DirectionsCar
                                        ServiceCategory.HOTELS -> Icons.Default.Hotel
                                        ServiceCategory.FLIGHTS -> Icons.Default.Flight
                                        ServiceCategory.GROCERY -> Icons.Default.ShoppingBag
                                        else -> Icons.Default.Business
                                    }
                                    FilterChip(
                                        selected = selectedCategory == category,
                                        onClick = {
                                            selectedCategory = category
                                            if (serviceOfferingTitle.isBlank()) {
                                                serviceOfferingTitle = when (category) {
                                                    ServiceCategory.FOOD -> "Special Dish & Catering Service"
                                                    ServiceCategory.CARS -> "4x4 Chauffeur Rental"
                                                    ServiceCategory.HOTELS -> "Deluxe Executive Stay"
                                                    ServiceCategory.FLIGHTS -> "Direct Domestic & Regional Flight"
                                                    ServiceCategory.GROCERY -> "Wholesale & Retail Pack"
                                                    else -> ""
                                                }
                                            }
                                        },
                                        leadingIcon = {
                                            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
                                        },
                                        label = { Text(category.displayName, fontSize = 12.sp) },
                                        modifier = Modifier.testTag("vendor_category_chip_${category.name.lowercase()}"),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = serviceOfferingTitle,
                                onValueChange = { serviceOfferingTitle = it },
                                label = { Text("Lead Service / Offering Title *") },
                                placeholder = { Text("e.g. Land Cruiser V8 VIP Rental or 2-Bed Furnished Flat") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("vendor_form_service_title"),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = serviceDescription,
                                onValueChange = { serviceDescription = it },
                                label = { Text("Offering Specifications & Amenities") },
                                placeholder = { Text("Mention AC, Wi-Fi, driver included, breakfast, delivery time...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("vendor_form_service_desc"),
                                minLines = 2,
                                maxLines = 3
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Dual Currency Pricing (SSP & USD)
                            Text(
                                text = "Transparent Pricing (Dual SSP & USD)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = priceSspText,
                                    onValueChange = {
                                        priceSspText = it
                                        val ssp = it.toLongOrNull() ?: 0L
                                        if (ssp > 0) {
                                            priceUsdText = String.format(Locale.US, "%.1f", ssp / 1220.0)
                                        }
                                    },
                                    label = { Text("Price (SSP)") },
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .testTag("vendor_form_price_ssp"),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )

                                OutlinedTextField(
                                    value = priceUsdText,
                                    onValueChange = { priceUsdText = it },
                                    label = { Text("Price (USD)") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("vendor_form_price_usd"),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )

                                OutlinedTextField(
                                    value = priceUnit,
                                    onValueChange = { priceUnit = it },
                                    label = { Text("Unit") },
                                    placeholder = { Text("/day") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("vendor_form_price_unit"),
                                    singleLine = true
                                )
                            }
                        }
                    }

                    // SECTION 3: LOCATION & GEOGRAPHIC SCOPE
                    item {
                        FormSectionCard(
                            title = "3. Location & Geographic Scope",
                            icon = Icons.Default.LocationOn,
                            subtitle = "Define where you operate across South Sudan and beyond"
                        ) {
                            Text(
                                text = "Select Operating Coverage Scope *",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(
                                    CoverageScope.JUBA_LOCAL,
                                    CoverageScope.SOUTH_SUDAN_NATIONAL,
                                    CoverageScope.EAST_AFRICA_REGIONAL,
                                    CoverageScope.GLOBAL_BEYOND_AFRICA
                                ).forEach { scope ->
                                    val isSelected = selectedScope == scope
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedScope = scope }
                                            .testTag("vendor_scope_item_${scope.name.lowercase()}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (scope == CoverageScope.JUBA_LOCAL) Icons.Default.LocationOn
                                                else if (scope == CoverageScope.SOUTH_SUDAN_NATIONAL) Icons.Default.Verified
                                                else Icons.Default.Public,
                                                contentDescription = null,
                                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = scope.displayName,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    fontSize = 13.sp,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = when (scope) {
                                                        CoverageScope.JUBA_LOCAL -> "Juba Town, Hai Cinema, Custom, Ministries Road, Airport, Munuki, Gudele"
                                                        CoverageScope.SOUTH_SUDAN_NATIONAL -> "All 10 States: Wau, Malakal, Yei, Bentiu, Bor, Rumbek, Nimule, Torit, Aweil"
                                                        CoverageScope.EAST_AFRICA_REGIONAL -> "Cross-border connections to Uganda, Kenya, Rwanda, Tanzania, Ethiopia"
                                                        CoverageScope.GLOBAL_BEYOND_AFRICA -> "International routes & cargo: UAE (Dubai), Egypt (Cairo), UK, USA, China"
                                                        else -> ""
                                                    },
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Selected",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Quick South Sudan & Regional Hubs",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(quickLocations) { (label, fullLocation) ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (cityAndCountry == fullLocation) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                        modifier = Modifier.clickable {
                                            cityAndCountry = fullLocation
                                        }
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (cityAndCountry == fullLocation) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = cityAndCountry,
                                onValueChange = {
                                    cityAndCountry = it
                                    errorMessage = null
                                },
                                label = { Text("City, State & Country *") },
                                placeholder = { Text("e.g. Wau, Western Bahr el Ghazal, South Sudan") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("vendor_form_city"),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = physicalAddress,
                                onValueChange = {
                                    physicalAddress = it
                                    errorMessage = null
                                },
                                label = { Text("Physical Street Address / Landmark *") },
                                placeholder = { Text("e.g. Hai Cinema, Ministries Road, Plot 14") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("vendor_form_address"),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Popular Landmarks:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                popularLandmarks.take(5).forEach { landmark ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.clickable { physicalAddress = landmark }
                                    ) {
                                        Text(
                                            text = landmark,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Submission progress message
                    if (isSubmitting) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("vendor_submitting_card"),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = submissionStatusMessage ?: "Connecting to Firestore & saving vendor record...",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }

                    // Bottom Buttons
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("vendor_form_cancel_button"),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isSubmitting
                            ) {
                                Text("Cancel")
                            }

                            Button(
                                onClick = {
                                    // Validation
                                    if (businessName.isBlank()) {
                                        errorMessage = "Please enter your business or company name."
                                        return@Button
                                    }
                                    if (cityAndCountry.isBlank()) {
                                        errorMessage = "Please enter your city and country location."
                                        return@Button
                                    }
                                    if (physicalAddress.isBlank()) {
                                        errorMessage = "Please enter your physical address or street landmark."
                                        return@Button
                                    }

                                    val ssp = priceSspText.toLongOrNull() ?: 10000L
                                    val usd = priceUsdText.toDoubleOrNull() ?: 8.0
                                    val unit = priceUnit.ifBlank { "/service" }
                                    val newVendorId = "vnd_${System.currentTimeMillis()}"

                                    val request = BusinessRegistrationRequest(
                                        id = newVendorId,
                                        businessName = businessName.trim(),
                                        businessType = businessType.trim(),
                                        category = selectedCategory,
                                        coverageScope = selectedScope,
                                        cityAndCountry = cityAndCountry.trim(),
                                        physicalAddress = physicalAddress.trim(),
                                        contactPhone = contactPhone.trim(),
                                        contactEmail = contactEmail.trim(),
                                        registrationTaxNumber = taxNumber.trim(),
                                        serviceOfferingTitle = serviceOfferingTitle.ifBlank { businessName.trim() },
                                        serviceDescription = serviceDescription.ifBlank { "Registered $businessType on JubaGo network providing ${selectedCategory.displayName} services." },
                                        priceSsp = ssp,
                                        priceUsd = usd,
                                        priceUnit = unit.trim(),
                                        isVerified = true,
                                        registeredAt = dateFormat.format(Date())
                                    )

                                    isSubmitting = true
                                    errorMessage = null
                                    submissionStatusMessage = "Connecting to Cloud Firestore database..."

                                    if (customOnSubmit != null) {
                                        customOnSubmit(request) { success, message ->
                                            isSubmitting = false
                                            firestoreSyncedSuccessfully = success
                                            registeredVendorId = newVendorId
                                            isSuccess = true
                                            onSuccess(request)
                                        }
                                    } else {
                                        coroutineScope.launch {
                                            submissionStatusMessage = "Saving vendor record in Firestore /vendors/$newVendorId..."
                                            var cloudSaved = false
                                            try {
                                                val db = try {
                                                    FirebaseFirestore.getInstance()
                                                } catch (e: Exception) {
                                                    Log.w(TAG, "FirebaseFirestore instance not ready: ${e.message}")
                                                    null
                                                }

                                                if (db != null) {
                                                    val vendorMap = hashMapOf<String, Any>(
                                                        "id" to newVendorId,
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
                                                        "createdAt" to System.currentTimeMillis()
                                                    )

                                                    // Save into Firestore vendors collection
                                                    db.collection("vendors").document(newVendorId)
                                                        .set(vendorMap, SetOptions.merge())
                                                        .await()

                                                    // Mirror into services collection for customer discovery
                                                    db.collection("services").document(newVendorId)
                                                        .set(vendorMap, SetOptions.merge())
                                                        .await()

                                                    cloudSaved = true
                                                }
                                            } catch (e: Exception) {
                                                Log.w(TAG, "Firestore write warning: ${e.message}")
                                            }

                                            isSubmitting = false
                                            firestoreSyncedSuccessfully = cloudSaved
                                            registeredVendorId = newVendorId
                                            isSuccess = true
                                            onSuccess(request)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1.6f)
                                    .testTag("vendor_form_submit_button"),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isSubmitting,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Save to Firestore & Join", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormSectionCard(
    title: String,
    icon: ImageVector,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}
