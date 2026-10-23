package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.localization.LocalizationManager
import com.example.model.AppLanguage
import com.example.model.PaymentMethod
import com.example.model.ServiceCategory
import com.example.ui.theme.JubaGoldSecondary
import com.example.ui.theme.JubaGreenDark
import com.example.ui.theme.JubaGreenLight
import com.example.ui.theme.JubaGreenPrimary
import com.example.ui.theme.NileBlueTertiary

/**
 * Onboarding Wizard component introducing JubaGo's key capabilities to new users,
 * including an interactive tutorial on service discovery and the 3-step booking system.
 * Fully localized for English and authentic Juba Arabic (عربي جوبا).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OnboardingWizardDialog(
    onDismiss: () -> Unit,
    onExploreServices: () -> Unit,
    onRegisterBusiness: () -> Unit,
    onOpenAuth: (() -> Unit)? = null,
    currentLanguage: AppLanguage = AppLanguage.ENGLISH,
    onLanguageChanged: ((AppLanguage) -> Unit)? = null
) {
    var currentStep by remember { mutableIntStateOf(0) }
    var activeLanguage by remember(currentLanguage) { mutableStateOf(currentLanguage) }
    val totalSteps = 5

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 20.dp)
                .clip(RoundedCornerShape(24.dp))
                .testTag("onboarding_wizard_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header Bar: Progress, Title, Language Switcher & Skip button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${currentStep + 1}/$totalSteps",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (currentStep) {
                                0 -> LocalizationManager.getString("onboarding_step_welcome_header", activeLanguage)
                                1 -> LocalizationManager.getString("onboarding_step_discovery_header", activeLanguage)
                                2 -> LocalizationManager.getString("onboarding_step_booking_header", activeLanguage)
                                3 -> LocalizationManager.getString("onboarding_step_tracking_header", activeLanguage)
                                else -> LocalizationManager.getString("onboarding_step_start_header", activeLanguage)
                            },
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Quick In-Wizard Language Switcher
                        Surface(
                            onClick = {
                                val next = if (activeLanguage == AppLanguage.JUBA_ARABIC) AppLanguage.ENGLISH else AppLanguage.JUBA_ARABIC
                                activeLanguage = next
                                onLanguageChanged?.invoke(next)
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (activeLanguage == AppLanguage.JUBA_ARABIC)
                                MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .testTag("onboarding_language_toggle")
                                .padding(end = 4.dp)
                        ) {
                            Text(
                                text = if (activeLanguage == AppLanguage.JUBA_ARABIC) "🇸🇸 عربي" else "🇬🇧 EN",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = if (activeLanguage == AppLanguage.JUBA_ARABIC)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("onboarding_skip_button")
                        ) {
                            Text(
                                text = LocalizationManager.getString("onboarding_skip", activeLanguage),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Scrollable Content Area for current step
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> -width } + fadeOut()
                                )
                            } else {
                                (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> width } + fadeOut()
                                )
                            }
                        },
                        label = "onboarding_step_animation"
                    ) { step ->
                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                                .padding(horizontal = 18.dp, vertical = 14.dp)
                        ) {
                            when (step) {
                                0 -> WelcomeStepContent(activeLanguage)
                                1 -> DiscoveryTutorialStepContent(activeLanguage)
                                2 -> BookingTutorialStepContent(activeLanguage)
                                3 -> LiveTrackingStepContent(activeLanguage)
                                4 -> FinalGetStartedStepContent(
                                    language = activeLanguage,
                                    onExplore = onExploreServices,
                                    onRegisterBusiness = onRegisterBusiness,
                                    onOpenAuth = onOpenAuth
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                // Bottom Navigation & Page Indicator Controls
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back Button
                        if (currentStep > 0) {
                            OutlinedButton(
                                onClick = { currentStep-- },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("onboarding_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Previous step",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(LocalizationManager.getString("onboarding_back", activeLanguage))
                            }
                        } else {
                            Spacer(modifier = Modifier.width(70.dp))
                        }

                        // Page Dots Indicator
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(totalSteps) { idx ->
                                val isSelected = idx == currentStep
                                Box(
                                    modifier = Modifier
                                        .size(if (isSelected) 10.dp else 6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.outlineVariant
                                        )
                                )
                            }
                        }

                        // Next / Get Started Button
                        if (currentStep < totalSteps - 1) {
                            Button(
                                onClick = { currentStep++ },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("onboarding_next_button")
                            ) {
                                Text(LocalizationManager.getString("onboarding_next", activeLanguage))
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Next step",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else {
                            Button(
                                onClick = onExploreServices,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.testTag("onboarding_finish_button")
                            ) {
                                Text(LocalizationManager.getString("onboarding_finish", activeLanguage))
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Complete onboarding",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Step 0: Welcome and Core Capabilities Overview
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WelcomeStepContent(language: AppLanguage) {
    Column {
        // Hero Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(18.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_onboarding_wizard),
                contentDescription = "JubaGo services onboarding illustration",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                            startY = 100f
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp)
            ) {
                Text(
                    text = if (language == AppLanguage.JUBA_ARABIC) "🇸🇸 منصة جنوب السودان الموحدة" else "🇸🇸 South Sudan's All-in-One Platform",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = LocalizationManager.getString("onboarding_welcome_title", language),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = LocalizationManager.getString("onboarding_welcome_desc", language),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = LocalizationManager.getString("onboarding_core_services_title", language),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Core 4 Pillars
        ServiceFeatureTile(
            icon = Icons.Default.Restaurant,
            title = LocalizationManager.getString("onboarding_feature_food_title", language),
            description = LocalizationManager.getString("onboarding_feature_food_desc", language)
        )

        Spacer(modifier = Modifier.height(8.dp))

        ServiceFeatureTile(
            icon = Icons.Default.Hotel,
            title = LocalizationManager.getString("onboarding_feature_hotel_title", language),
            description = LocalizationManager.getString("onboarding_feature_hotel_desc", language)
        )

        Spacer(modifier = Modifier.height(8.dp))

        ServiceFeatureTile(
            icon = Icons.Default.DirectionsCar,
            title = LocalizationManager.getString("onboarding_feature_transport_title", language),
            description = LocalizationManager.getString("onboarding_feature_transport_desc", language)
        )

        Spacer(modifier = Modifier.height(8.dp))

        ServiceFeatureTile(
            icon = Icons.Default.Flight,
            title = LocalizationManager.getString("onboarding_feature_cargo_title", language),
            description = LocalizationManager.getString("onboarding_feature_cargo_desc", language)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // South Sudan-specific local resilience highlights
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = LocalizationManager.getString("onboarding_local_advantage_header", language),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    LocalBadgeItem(Icons.Default.WifiOff, LocalizationManager.getString("onboarding_local_advantage_map", language))
                    LocalBadgeItem(Icons.Default.BatterySaver, LocalizationManager.getString("onboarding_local_advantage_battery", language))
                    LocalBadgeItem(Icons.Default.PhoneAndroid, LocalizationManager.getString("onboarding_local_advantage_payments", language))
                }
            }
        }
    }
}

/**
 * Step 1: Interactive Service Discovery Tutorial
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DiscoveryTutorialStepContent(language: AppLanguage) {
    val isArabic = language == AppLanguage.JUBA_ARABIC
    var sampleSelectedCategory by remember { mutableStateOf(if (isArabic) "الكل" else "All") }

    Column {
        Text(
            text = LocalizationManager.getString("onboarding_discovery_title", language),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = LocalizationManager.getString("onboarding_discovery_desc", language),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        TutorialTipCard(
            number = "1",
            icon = Icons.Default.Search,
            title = LocalizationManager.getString("onboarding_discovery_tip1_title", language),
            text = LocalizationManager.getString("onboarding_discovery_tip1_desc", language)
        )

        Spacer(modifier = Modifier.height(10.dp))

        TutorialTipCard(
            number = "2",
            icon = Icons.Default.LocationOn,
            title = LocalizationManager.getString("onboarding_discovery_tip2_title", language),
            text = LocalizationManager.getString("onboarding_discovery_tip2_desc", language)
        )

        Spacer(modifier = Modifier.height(10.dp))

        TutorialTipCard(
            number = "3",
            icon = Icons.Default.WifiOff,
            title = LocalizationManager.getString("onboarding_discovery_tip3_title", language),
            text = LocalizationManager.getString("onboarding_discovery_tip3_desc", language)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Interactive Sandbox to test discovery
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = LocalizationManager.getString("onboarding_discovery_practice_title", language),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                val sampleCategories = if (isArabic) {
                    listOf("الكل", "أكل", "فنادق", "مشاوير", "دكاكين")
                } else {
                    listOf("All", "Food", "Hotels", "Transport", "Shops")
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    sampleCategories.forEach { cat ->
                        FilterChip(
                            selected = sampleSelectedCategory == cat,
                            onClick = { sampleSelectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val previewItem = if (isArabic) {
                    when (sampleSelectedCategory) {
                        "أكل" -> Triple("سمك نيلي طازة مع كسرة", "مطعم نيل كينق • حي ملكال", "SSP 6,500 (~$4.50)")
                        "فنادق" -> Triple("جناح النيل الفاخر", "فندق بيراميد • وسط جوبا", "SSP 48,000 / الليلة")
                        "مشاوير" -> Triple("طلب بودا سريع ومضمون", "فرسان البودا • سوق كاستم", "SSP 1,800")
                        "دكاكين" -> Triple("طاقة شمسية سان كينق ٥٠ واط", "إلكترونيات كوني كوني", "SSP 35,000")
                        else -> Triple("بوفيه واحة جوبا الكبير", "قراند أوتيل • شارع النيل", "SSP 8,500")
                    }
                } else {
                    when (sampleSelectedCategory) {
                        "Food" -> Triple("Nile River Fried Fish & Kisra", "Nile King Restaurant • Hai Malakal", "SSP 6,500 (~$4.50)")
                        "Hotels" -> Triple("Pyramid Continental Deluxe Suite", "Pyramid Hotel • Juba Town", "SSP 48,000 / night")
                        "Transport" -> Triple("Express Boda Pickup", "Boda Safe Riders • Customs Market", "SSP 1,800")
                        "Shops" -> Triple("SunKing Solar Power Kit 50W", "Equatoria Electronics • Konyo Konyo", "SSP 35,000")
                        else -> Triple("Juba Grand Oasis Buffet", "Grand Hotel • Nile Riverside", "SSP 8,500")
                    }
                }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (sampleSelectedCategory) {
                                    "Food", "أكل" -> Icons.Default.Restaurant
                                    "Hotels", "فنادق" -> Icons.Default.Hotel
                                    "Transport", "مشاوير" -> Icons.Default.DirectionsCar
                                    "Shops", "دكاكين" -> Icons.Default.ShoppingBag
                                    else -> Icons.Default.Verified
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = previewItem.first,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = previewItem.second,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Text(
                            text = previewItem.third,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Step 2: 3-Step Booking Tutorial with Simulated Payment Sandbox
 */
@Composable
private fun BookingTutorialStepContent(language: AppLanguage) {
    val isArabic = language == AppLanguage.JUBA_ARABIC
    var selectedPaymentDemo by remember {
        mutableStateOf(if (isArabic) "إم-قروش (m-GURUSH)" else "m-GURUSH Mobile Money")
    }
    var orderSimulatedSuccess by remember { mutableStateOf(false) }

    Column {
        Text(
            text = LocalizationManager.getString("onboarding_booking_title", language),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = LocalizationManager.getString("onboarding_booking_desc", language),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        BookingStepRow(
            stepNumber = "1",
            title = LocalizationManager.getString("onboarding_booking_step1_title", language),
            subtitle = LocalizationManager.getString("onboarding_booking_step1_desc", language)
        )

        Spacer(modifier = Modifier.height(10.dp))

        BookingStepRow(
            stepNumber = "2",
            title = LocalizationManager.getString("onboarding_booking_step2_title", language),
            subtitle = LocalizationManager.getString("onboarding_booking_step2_desc", language)
        )

        Spacer(modifier = Modifier.height(10.dp))

        BookingStepRow(
            stepNumber = "3",
            title = LocalizationManager.getString("onboarding_booking_step3_title", language),
            subtitle = LocalizationManager.getString("onboarding_booking_step3_desc", language)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Interactive Sandbox: Practice Booking Choice
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = LocalizationManager.getString("onboarding_booking_practice_title", language),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(10.dp))

                val demoMethods = if (isArabic) {
                    listOf(
                        Pair("إم-قروش (m-GURUSH)", "رقم ١ للمحافظ الرقمية في جنوب السودان"),
                        Pair("إم تي إن مومو (MTN MoMo)", "تحويل ودفع فوري عبر شبكة إم تي إن"),
                        Pair("كاش يد بيد (Cash on Delivery)", "دفع بالجنوب سوداني أو الدولار عند الاستلام")
                    )
                } else {
                    listOf(
                        Pair("m-GURUSH Mobile Money", "#1 wallet in South Sudan"),
                        Pair("MTN Mobile Money", "Instant telecom balance payment"),
                        Pair("Cash on Delivery", "Pay cash in SSP / USD upon delivery")
                    )
                }

                demoMethods.forEach { (name, note) ->
                    val isChosen = selectedPaymentDemo == name
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isChosen) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                else Color.Transparent
                            )
                            .border(
                                width = 1.dp,
                                color = if (isChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                selectedPaymentDemo = name
                                orderSimulatedSuccess = false
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .border(2.dp, if (isChosen) MaterialTheme.colorScheme.primary else Color.Gray, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isChosen) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = note,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { orderSimulatedSuccess = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_simulate_order_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isArabic) "جرّب طلب تجريبي بـ $selectedPaymentDemo" else "Test Demo Order with $selectedPaymentDemo")
                }

                AnimatedVisibility(visible = orderSimulatedSuccess) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = LocalizationManager.getString("onboarding_demo_success_title", language),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = LocalizationManager.getString("onboarding_demo_success_desc", language),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Step 3: Live GPS Tracking & Nile Customer Support
 */
@Composable
private fun LiveTrackingStepContent(language: AppLanguage) {
    val isArabic = language == AppLanguage.JUBA_ARABIC

    Column {
        Text(
            text = LocalizationManager.getString("onboarding_tracking_title", language),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = LocalizationManager.getString("onboarding_tracking_desc", language),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        TutorialTipCard(
            number = "GPS",
            icon = Icons.Default.LocationOn,
            title = LocalizationManager.getString("onboarding_tracking_tip1_title", language),
            text = LocalizationManager.getString("onboarding_tracking_tip1_desc", language)
        )

        Spacer(modifier = Modifier.height(10.dp))

        TutorialTipCard(
            number = "SYNC",
            icon = Icons.Default.Sync,
            title = LocalizationManager.getString("onboarding_tracking_tip2_title", language),
            text = LocalizationManager.getString("onboarding_tracking_tip2_desc", language)
        )

        Spacer(modifier = Modifier.height(10.dp))

        TutorialTipCard(
            number = "CHAT",
            icon = Icons.Default.SupportAgent,
            title = LocalizationManager.getString("onboarding_tracking_tip3_title", language),
            text = LocalizationManager.getString("onboarding_tracking_tip3_desc", language)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Demo Status Preview
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isArabic) "المندوب في الطريق: بودا إكسبريس" else "Courier on the Way: Boda Express",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = if (isArabic) "قريب من مجمع الوزارات • ٦ دقائق وصول" else "Approaching Ministry Complex • 6 mins away",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(JubaGreenPrimary)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isArabic) "تتبع حي" else "Live GPS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Step 4: Final Step - For Customers & Business Owners
 */
@Composable
private fun FinalGetStartedStepContent(
    language: AppLanguage,
    onExplore: () -> Unit,
    onRegisterBusiness: () -> Unit,
    onOpenAuth: (() -> Unit)?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = LocalizationManager.getString("onboarding_final_title", language),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = LocalizationManager.getString("onboarding_final_desc", language),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Big Primary Button: Explore Services
        Button(
            onClick = onExplore,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("onboarding_explore_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = LocalizationManager.getString("onboarding_explore_button", language),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Secondary Action: For Business Owners / Merchants
        OutlinedButton(
            onClick = onRegisterBusiness,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("onboarding_register_business_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Storefront,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = LocalizationManager.getString("onboarding_register_button", language),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        }

        if (onOpenAuth != null) {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = onOpenAuth,
                modifier = Modifier.testTag("onboarding_signin_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(LocalizationManager.getString("onboarding_auth_button", language))
            }
        }
    }
}

// ----------------- Helper UI Components -----------------

@Composable
private fun ServiceFeatureTile(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LocalBadgeItem(icon: ImageVector, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun TutorialTipCard(
    number: String,
    icon: ImageVector,
    title: String,
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BookingStepRow(
    stepNumber: String,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
