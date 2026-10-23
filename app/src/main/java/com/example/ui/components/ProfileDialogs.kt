package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.DeliveryAddress
import com.example.model.PaymentMethod
import com.example.model.SavedPaymentMethod
import com.example.model.UserProfile
import java.util.UUID

@Composable
fun EditAccountDetailsDialog(
    currentUser: UserProfile,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, city: String) -> Unit
) {
    var name by remember { mutableStateOf(currentUser.displayName) }
    var phone by remember { mutableStateOf(currentUser.phoneNumber) }
    var city by remember { mutableStateOf(currentUser.cityAndCountry) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("edit_account_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
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
                            Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Account Details",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Firebase Auth & Cloud Profile",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Firebase Auth Immutable Info (UID & Email)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Firebase UID: ${currentUser.uid}",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (currentUser.email.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Auth Email: ${currentUser.email}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name / Full Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number (+211 ...)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_phone_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("City & Country") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_city_input")
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            onSave(name.trim(), phone.trim(), city.trim())
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_account_button")
                    ) {
                        Text("Save Profile")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAddressDialog(
    initialAddress: DeliveryAddress? = null,
    onDismiss: () -> Unit,
    onSave: (DeliveryAddress) -> Unit
) {
    var label by remember { mutableStateOf(initialAddress?.label ?: "Home") }
    var streetAddress by remember { mutableStateOf(initialAddress?.streetAddress ?: "") }
    var area by remember { mutableStateOf(initialAddress?.area ?: "Hai Cinema") }
    var landmark by remember { mutableStateOf(initialAddress?.landmark ?: "") }
    var contactPhone by remember { mutableStateOf(initialAddress?.contactPhone ?: "") }
    var isDefault by remember { mutableStateOf(initialAddress?.isDefault ?: false) }

    val popularAreas = listOf(
        "Hai Cinema",
        "Munuki",
        "Hai Malakal",
        "Konyo Konyo",
        "Gudele",
        "Juba Town",
        "Ministries Road",
        "Tongping",
        "Airport Road",
        "Hai Referendum",
        "Atlabara",
        "Ghabat (Port)"
    )
    var areaExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("add_edit_address_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
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
                            Icons.Default.Place,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (initialAddress != null) "Edit Delivery Address" else "Add Delivery Address",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Address Label (e.g. Home, Office, Store)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("address_label_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = streetAddress,
                    onValueChange = { streetAddress = it },
                    label = { Text("Street / House / Block (e.g. Block 3, House 14)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("address_street_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Area / District Dropdown
                ExposedDropdownMenuBox(
                    expanded = areaExpanded,
                    onExpandedChange = { areaExpanded = !areaExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = area,
                        onValueChange = { area = it },
                        label = { Text("Area / District in Juba") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = areaExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                            .testTag("address_area_input")
                    )

                    ExposedDropdownMenu(
                        expanded = areaExpanded,
                        onDismissRequest = { areaExpanded = false }
                    ) {
                        popularAreas.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    area = item
                                    areaExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = landmark,
                    onValueChange = { landmark = it },
                    label = { Text("Landmark / Special Instructions (Optional)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("address_landmark_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = contactPhone,
                    onValueChange = { contactPhone = it },
                    label = { Text("Recipient Phone Number") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("address_phone_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isDefault = !isDefault }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = isDefault,
                        onCheckedChange = { isDefault = it },
                        modifier = Modifier.testTag("address_default_checkbox")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Set as default delivery address",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (streetAddress.isNotBlank()) {
                                onSave(
                                    DeliveryAddress(
                                        id = initialAddress?.id ?: UUID.randomUUID().toString(),
                                        label = label.ifBlank { "Delivery Point" },
                                        streetAddress = streetAddress.trim(),
                                        area = area.ifBlank { "Juba" },
                                        city = "Juba, South Sudan",
                                        landmark = landmark.trim(),
                                        contactPhone = contactPhone.trim(),
                                        isDefault = isDefault
                                    )
                                )
                                onDismiss()
                            }
                        },
                        enabled = streetAddress.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_address_button")
                    ) {
                        Text("Save Address")
                    }
                }
            }
        }
    }
}

@Composable
fun AddPaymentMethodDialog(
    onDismiss: () -> Unit,
    onSave: (SavedPaymentMethod) -> Unit
) {
    var selectedMethod by remember { mutableStateOf(PaymentMethod.M_GURUSH) }
    var title by remember { mutableStateOf("m-GURUSH Wallet") }
    var accountIdentifier by remember { mutableStateOf("") }
    var accountHolder by remember { mutableStateOf("Deng Majok") }
    var expiryDate by remember { mutableStateOf("12/28") }
    var isDefault by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("add_payment_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
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
                            Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Add Payment Method",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Select Payment Provider",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Selectable methods
                val methods = listOf(
                    PaymentMethod.M_GURUSH,
                    PaymentMethod.MTN_MOMO,
                    PaymentMethod.DIGITEL_PAY,
                    PaymentMethod.BANK_CARD
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    methods.forEach { method ->
                        val isSelected = selectedMethod == method
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedMethod = method
                                    title = when (method) {
                                        PaymentMethod.M_GURUSH -> "m-GURUSH Wallet"
                                        PaymentMethod.MTN_MOMO -> "MTN MoMo Express"
                                        PaymentMethod.DIGITEL_PAY -> "Digitel Telecommunication Pay"
                                        PaymentMethod.BANK_CARD -> "Visa / Mastercard Card"
                                        else -> method.title
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (method == PaymentMethod.BANK_CARD) Icons.Default.CreditCard else Icons.Default.PhoneAndroid,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = method.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = method.subtitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = accountIdentifier,
                    onValueChange = { accountIdentifier = it },
                    label = {
                        Text(
                            if (selectedMethod == PaymentMethod.BANK_CARD) "Card Number (16 digits)"
                            else "Mobile Money Number (e.g. 092XXXXXXX)"
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_identifier_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = accountHolder,
                    onValueChange = { accountHolder = it },
                    label = { Text("Account Holder Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_holder_input")
                )

                if (selectedMethod == PaymentMethod.BANK_CARD) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = expiryDate,
                        onValueChange = { expiryDate = it },
                        label = { Text("Expiry (MM/YY)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("payment_expiry_input")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isDefault = !isDefault }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = isDefault,
                        onCheckedChange = { isDefault = it },
                        modifier = Modifier.testTag("payment_default_checkbox")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Set as default payment method",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (accountIdentifier.isNotBlank()) {
                                // Mask identifier for privacy if desired
                                val masked = if (selectedMethod == PaymentMethod.BANK_CARD) {
                                    val digits = accountIdentifier.filter { it.isDigit() }
                                    if (digits.length >= 4) "**** **** **** ${digits.takeLast(4)}" else accountIdentifier
                                } else {
                                    accountIdentifier
                                }

                                onSave(
                                    SavedPaymentMethod(
                                        id = UUID.randomUUID().toString(),
                                        method = selectedMethod,
                                        title = title,
                                        accountIdentifier = masked,
                                        accountHolder = accountHolder.ifBlank { "Juba Account Holder" },
                                        isDefault = isDefault,
                                        expiryDate = if (selectedMethod == PaymentMethod.BANK_CARD) expiryDate else null
                                    )
                                )
                                onDismiss()
                            }
                        },
                        enabled = accountIdentifier.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_payment_button")
                    ) {
                        Text("Save Payment")
                    }
                }
            }
        }
    }
}
