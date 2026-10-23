package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.payment.PaymentGatewayService
import com.example.data.payment.PaymentResult
import com.example.model.PaymentMethod
import com.example.model.ServiceItem
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@Composable
fun BookingPaymentDialog(
    service: ServiceItem,
    onDismiss: () -> Unit,
    onConfirmOrder: (name: String, phone: String, address: String, paymentMethod: PaymentMethod) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var customerName by remember { mutableStateOf("Deng Majok") }
    var customerPhone by remember { mutableStateOf("+211 92 123 4567") }
    var deliveryAddress by remember { mutableStateOf("Hai Cinema, Block 3, Juba") }

    // Payment categories: 0 = Mobile Money (m-GURUSH / MTN / Digitel), 1 = Stripe Card, 2 = Cash on Delivery
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.M_GURUSH) }

    // Mobile Money Fields
    var mobileMoneyPhone by remember { mutableStateOf("0921234567") }
    var mobileMoneyPin by remember { mutableStateOf("1234") }

    // Stripe / Card Fields
    var cardNumber by remember { mutableStateOf("4242 4242 4242 4242") }
    var cardExpiry by remember { mutableStateOf("12/28") }
    var cardCvv by remember { mutableStateOf("888") }
    var cardholderName by remember { mutableStateOf("Deng Majok") }

    // Gateway processing state
    var isProcessing by remember { mutableStateOf(false) }
    var processingStage by remember { mutableStateOf("Initializing secure handshake...") }
    var paymentErrorMessage by remember { mutableStateOf<String?>(null) }
    var paymentSuccessRef by remember { mutableStateOf<String?>(null) }

    val quickLocations = listOf(
        "Hai Cinema",
        "Ministries Road",
        "Custom Market",
        "Gudele 1",
        "Munuki Block 4",
        "Juba Town",
        "Tongping",
        "Airport Road"
    )

    val sspFormatted = NumberFormat.getNumberInstance(Locale.US).format(service.priceSsp)

    Dialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("booking_payment_modal"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Checkout & Secure Payment",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "South Sudan Mobile Money API & Stripe Gateway",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    if (!isProcessing) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_checkout_button")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Item Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(14.dp)
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
                                text = service.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1
                            )
                            Text(
                                text = "Provider: ${service.providerName} • ${service.location}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "$sspFormatted SSP",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "≈ $${service.priceUsd} USD",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Customer Info
                Text(
                    text = "1. Contact & Delivery Location",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Your Full Name") },
                    enabled = !isProcessing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("customer_name_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = { customerPhone = it },
                    label = { Text("Contact Phone (+211)") },
                    enabled = !isProcessing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("customer_phone_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = deliveryAddress,
                    onValueChange = { deliveryAddress = it },
                    label = { Text("Delivery Address / Hotel / Landmark in Juba") },
                    enabled = !isProcessing,
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("delivery_address_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Quick Juba Area Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    items(quickLocations) { loc ->
                        FilterChip(
                            selected = deliveryAddress.contains(loc),
                            onClick = { if (!isProcessing) deliveryAddress = "$loc, Juba, South Sudan" },
                            label = { Text(loc, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Payment Gateway Tabs
                Text(
                    text = "2. Select Payment Processing Gateway",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))

                TabRow(
                    selectedTabIndex = selectedCategoryIndex,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedCategoryIndex == 0,
                        onClick = {
                            if (!isProcessing) {
                                selectedCategoryIndex = 0
                                selectedPaymentMethod = PaymentMethod.M_GURUSH
                            }
                        },
                        text = { Text("Mobile Money", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    Tab(
                        selected = selectedCategoryIndex == 1,
                        onClick = {
                            if (!isProcessing) {
                                selectedCategoryIndex = 1
                                selectedPaymentMethod = PaymentMethod.BANK_CARD
                            }
                        },
                        text = { Text("Stripe / Card", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    Tab(
                        selected = selectedCategoryIndex == 2,
                        onClick = {
                            if (!isProcessing) {
                                selectedCategoryIndex = 2
                                selectedPaymentMethod = PaymentMethod.CASH_ON_DELIVERY
                            }
                        },
                        text = { Text("Cash (COD)", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.LocalAtm, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Gateway Details Container
                when (selectedCategoryIndex) {
                    // Mobile Money Gateway
                    0 -> {
                        Column {
                            Text(
                                text = "Choose Local Provider:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    PaymentMethod.M_GURUSH to "m-GURUSH",
                                    PaymentMethod.MTN_MOMO to "MTN MoMo",
                                    PaymentMethod.DIGITEL_PAY to "Digitel Pay"
                                ).forEach { (method, label) ->
                                    val isSelected = selectedPaymentMethod == method
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            if (!isProcessing) {
                                                selectedPaymentMethod = method
                                                if (method == PaymentMethod.M_GURUSH) mobileMoneyPhone = "0921234567"
                                                else if (method == PaymentMethod.MTN_MOMO) mobileMoneyPhone = "0912345678"
                                                else if (method == PaymentMethod.DIGITEL_PAY) mobileMoneyPhone = "0987654321"
                                            }
                                        },
                                        label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = mobileMoneyPhone,
                                onValueChange = { mobileMoneyPhone = it },
                                label = { Text("Mobile Money Wallet Number") },
                                enabled = !isProcessing,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("mobile_money_phone_input")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = mobileMoneyPin,
                                onValueChange = { if (it.length <= 4) mobileMoneyPin = it },
                                label = { Text("4-Digit Wallet Authorization PIN") },
                                enabled = !isProcessing,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                trailingIcon = {
                                    Icon(Icons.Default.Fingerprint, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("mobile_money_pin_input")
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "🔒 Secure USSD Push API: Encrypted end-to-end with Bank of South Sudan compliance.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    // Stripe / Card Gateway
                    1 -> {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Credit / Debit Card Details",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF6772E5))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Powered by Stripe", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = cardNumber,
                                onValueChange = { cardNumber = it },
                                label = { Text("Card Number (Visa / Mastercard)") },
                                enabled = !isProcessing,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                trailingIcon = {
                                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("stripe_card_number_input")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = cardExpiry,
                                    onValueChange = { cardExpiry = it },
                                    label = { Text("MM/YY") },
                                    enabled = !isProcessing,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("stripe_card_expiry_input")
                                )

                                OutlinedTextField(
                                    value = cardCvv,
                                    onValueChange = { if (it.length <= 4) cardCvv = it },
                                    label = { Text("CVV/CVC") },
                                    enabled = !isProcessing,
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("stripe_card_cvv_input")
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = cardholderName,
                                onValueChange = { cardholderName = it },
                                label = { Text("Cardholder Name") },
                                enabled = !isProcessing,
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("stripe_cardholder_name_input")
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "🛡️ 256-Bit SSL AES Encryption • 3D Secure Verified",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    // Cash on Delivery
                    2 -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocalAtm, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Cash on Delivery (COD) in SSP", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Pay cash directly to the courier upon arrival at your destination. A digital receipt will be generated on your phone immediately.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }

                // Error message banner
                paymentErrorMessage?.let { err ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                // Processing Indicator banner
                AnimatedVisibility(
                    visible = isProcessing,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = processingStage,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Trust & Escrow guarantee badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFE8F5E9))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.VerifiedUser,
                        contentDescription = "Escrow Security",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "100% Escrow Protected: Funds safely held in escrow until courier arrives & verifies delivery.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF1B5E20)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Confirm and Pay Button
                Button(
                    onClick = {
                        paymentErrorMessage = null
                        isProcessing = true
                        processingStage = "Contacting ${selectedPaymentMethod.title} Gateway..."

                        coroutineScope.launch {
                            val tempOrderId = "JB-" + (1000..9999).random()

                            val result = when (selectedCategoryIndex) {
                                0 -> {
                                    processingStage = "Awaiting USSD PIN confirmation..."
                                    PaymentGatewayService.processMobileMoneyPayment(
                                        orderId = tempOrderId,
                                        serviceId = service.id,
                                        serviceTitle = service.title,
                                        amountSsp = service.priceSsp,
                                        amountUsd = service.priceUsd,
                                        method = selectedPaymentMethod,
                                        phoneNumber = mobileMoneyPhone,
                                        pin = mobileMoneyPin
                                    )
                                }
                                1 -> {
                                    processingStage = "Authorizing Stripe 3D Secure..."
                                    PaymentGatewayService.processStripePayment(
                                        orderId = tempOrderId,
                                        serviceId = service.id,
                                        serviceTitle = service.title,
                                        amountSsp = service.priceSsp,
                                        amountUsd = service.priceUsd,
                                        cardNumber = cardNumber,
                                        expiry = cardExpiry,
                                        cvv = cardCvv,
                                        cardholderName = cardholderName
                                    )
                                }
                                else -> {
                                    // Cash on delivery
                                    PaymentResult.Success(
                                        com.example.data.payment.PaymentTransaction(
                                            id = "txn_cod_${(1000..9999).random()}",
                                            orderId = tempOrderId,
                                            serviceId = service.id,
                                            serviceTitle = service.title,
                                            amountSsp = service.priceSsp,
                                            amountUsd = service.priceUsd,
                                            method = PaymentMethod.CASH_ON_DELIVERY,
                                            maskedAccount = "Cash upon arrival in Juba",
                                            status = com.example.data.payment.PaymentStatus.AUTHORIZED,
                                            gatewayReference = "COD-RECEIPT-VERIFIED"
                                        )
                                    )
                                }
                            }

                            when (result) {
                                is PaymentResult.Success -> {
                                    isProcessing = false
                                    onConfirmOrder(
                                        customerName,
                                        customerPhone,
                                        deliveryAddress,
                                        selectedPaymentMethod
                                    )
                                }
                                is PaymentResult.Error -> {
                                    isProcessing = false
                                    paymentErrorMessage = result.errorMessage
                                }
                            }
                        }
                    },
                    enabled = !isProcessing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("confirm_booking_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isProcessing) "Processing Gateway..." else "Pay $sspFormatted SSP & Confirm Booking",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }
    }
}
