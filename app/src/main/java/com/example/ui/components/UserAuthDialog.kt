package com.example.ui.components

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.auth.UserAuthenticationProvider
import com.example.model.AuthState
import com.example.model.ServiceCategory
import com.example.model.UserProfile
import com.example.model.UserRole
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserAuthDialog(
    authProvider: UserAuthenticationProvider,
    currentUser: UserProfile?,
    authState: AuthState,
    onDismiss: () -> Unit,
    onSuccess: (UserProfile) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTabIndex by remember { mutableIntStateOf(if (currentUser != null) 2 else 0) } // 0: Sign In, 1: Sign Up, 2: Profile
    var selectedRole by remember { mutableStateOf(currentUser?.role ?: UserRole.CUSTOMER) }

    // Form inputs
    var emailInput by remember { mutableStateOf(currentUser?.email ?: "") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf(currentUser?.displayName ?: "") }
    var phoneInput by remember { mutableStateOf(currentUser?.phoneNumber ?: "") }
    var businessNameInput by remember { mutableStateOf(currentUser?.businessName ?: "") }
    var businessTypeInput by remember { mutableStateOf(currentUser?.businessType ?: "Restaurant & Catering") }
    var selectedCategory by remember { mutableStateOf(ServiceCategory.FOOD) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val businessTypes = listOf(
        "Restaurant & Catering",
        "Hotel & Accommodation",
        "Apartment / Short Stay",
        "4x4 & Vehicle Rental",
        "Retail Store & Supermarket",
        "Courier & Logistics Service"
    )
    var expandedBizType by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp)
                .clip(RoundedCornerShape(20.dp))
                .testTag("user_auth_dialog_surface"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (currentUser != null) "User Account" else "Welcome to JubaGo",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("auth_dialog_title")
                        )
                        Text(
                            text = if (currentUser != null) "Manage your secure profile" else "Sign in or register your customer or vendor profile",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("auth_dialog_close_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // If user is already authenticated
                if (currentUser != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (currentUser.role == UserRole.MERCHANT) Icons.Default.Storefront else Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = currentUser.displayName,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = currentUser.email.ifBlank { currentUser.phoneNumber },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Role: ${currentUser.role.label}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Verified",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            if (!currentUser.businessName.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "🏢 Business: ${currentUser.businessName} (${currentUser.businessType ?: "Vendor"})",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val newRole = if (currentUser.role == UserRole.CUSTOMER) UserRole.MERCHANT else UserRole.CUSTOMER
                                        authProvider.switchRole(newRole)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("auth_switch_role_button")
                                ) {
                                    Text(
                                        text = if (currentUser.role == UserRole.CUSTOMER) "Switch to Vendor" else "Switch to Customer",
                                        fontSize = 12.sp
                                    )
                                }

                                Button(
                                    onClick = {
                                        authProvider.signOut()
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("auth_signout_button")
                                ) {
                                    Text("Sign Out", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                    return@Column
                }

                // Role selector tabs (Customer vs Vendor)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (selectedRole == UserRole.CUSTOMER) MaterialTheme.colorScheme.primary
                                else Color.Transparent
                            )
                            .clickable { selectedRole = UserRole.CUSTOMER }
                            .padding(vertical = 10.dp)
                            .testTag("auth_select_role_customer"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = if (selectedRole == UserRole.CUSTOMER) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Customer View",
                                color = if (selectedRole == UserRole.CUSTOMER) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (selectedRole == UserRole.MERCHANT) MaterialTheme.colorScheme.primary
                                else Color.Transparent
                            )
                            .clickable { selectedRole = UserRole.MERCHANT }
                            .padding(vertical = 10.dp)
                            .testTag("auth_select_role_merchant"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Business,
                                contentDescription = null,
                                tint = if (selectedRole == UserRole.MERCHANT) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Vendor / Merchant",
                                color = if (selectedRole == UserRole.MERCHANT) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sign In / Sign Up Tabs
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("Sign In", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("auth_tab_signin")
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text("Register New Account", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("auth_tab_signup")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Google Sign-In Button (Mandatory via Credential Manager)
                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = null
                        scope.launch {
                            authProvider.signInWithGoogle(context, selectedRole) { success, error ->
                                isLoading = false
                                if (success) {
                                    authProvider.currentUser.value?.let(onSuccess)
                                    onDismiss()
                                } else {
                                    errorMessage = error
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("auth_google_signin_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "G",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (selectedRole == UserRole.MERCHANT) "Sign in as Vendor with Google" else "Sign in with Google",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
                    Text(
                        text = " OR WITH EMAIL / PHONE ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Error alert
                AnimatedVisibility(visible = errorMessage != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                // If Sign-Up Tab is active
                if (selectedTabIndex == 1) {
                    if (selectedRole == UserRole.MERCHANT) {
                        OutlinedTextField(
                            value = businessNameInput,
                            onValueChange = { businessNameInput = it },
                            label = { Text("Business / Hotel / Company Name *") },
                            leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_vendor_business_name_input"),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        ExposedDropdownMenuBox(
                            expanded = expandedBizType,
                            onExpandedChange = { expandedBizType = !expandedBizType }
                        ) {
                            OutlinedTextField(
                                value = businessTypeInput,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Business Category *") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBizType) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                                    .testTag("auth_vendor_type_dropdown"),
                                shape = RoundedCornerShape(10.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expandedBizType,
                                onDismissRequest = { expandedBizType = false }
                            ) {
                                businessTypes.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type) },
                                        onClick = {
                                            businessTypeInput = type
                                            expandedBizType = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text(if (selectedRole == UserRole.MERCHANT) "Owner / Manager Full Name *" else "Full Name *") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_name_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("Phone Number (+211 Juba / Int'l) *") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_phone_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Email & Password Fields
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text(if (selectedTabIndex == 0) "Email or Phone Number *" else "Email Address *") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_email_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Password *") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_password_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Submit Button
                Button(
                    onClick = {
                        if (emailInput.isBlank() || passwordInput.isBlank()) {
                            errorMessage = "Please enter both credentials"
                            return@Button
                        }
                        isLoading = true
                        errorMessage = null
                        scope.launch {
                            if (selectedTabIndex == 0) {
                                // Sign in
                                authProvider.login(emailInput, passwordInput, selectedRole) { success, err ->
                                    isLoading = false
                                    if (success) {
                                        authProvider.currentUser.value?.let(onSuccess)
                                        onDismiss()
                                    } else {
                                        errorMessage = err
                                    }
                                }
                            } else {
                                // Sign up
                                if (selectedRole == UserRole.MERCHANT) {
                                    val bName = businessNameInput.ifBlank { nameInput + " Services" }
                                    authProvider.signUpVendor(
                                        businessName = bName,
                                        ownerName = nameInput.ifBlank { "Vendor Owner" },
                                        email = emailInput,
                                        phone = phoneInput.ifBlank { "+211 920 000 000" },
                                        businessType = businessTypeInput,
                                        category = selectedCategory,
                                        cityCountry = "Juba, South Sudan",
                                        password = passwordInput
                                    ) { success, err ->
                                        isLoading = false
                                        if (success) {
                                            authProvider.currentUser.value?.let(onSuccess)
                                            onDismiss()
                                        } else {
                                            errorMessage = err
                                        }
                                    }
                                } else {
                                    authProvider.signUpCustomer(
                                        fullName = nameInput.ifBlank { "Juba Customer" },
                                        email = emailInput,
                                        phone = phoneInput.ifBlank { "+211 920 000 000" },
                                        password = passwordInput
                                    ) { success, err ->
                                        isLoading = false
                                        if (success) {
                                            authProvider.currentUser.value?.let(onSuccess)
                                            onDismiss()
                                        } else {
                                            errorMessage = err
                                        }
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("auth_submit_button"),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (selectedTabIndex == 0) "Sign In" else "Create Account",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // One-tap quick demo login options for seamless review
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            scope.launch {
                                authProvider.login("customer@jubago.ssd", "password123", UserRole.CUSTOMER) { success, _ ->
                                    if (success) {
                                        authProvider.currentUser.value?.let(onSuccess)
                                        onDismiss()
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("auth_quick_demo_customer")
                    ) {
                        Text("Demo Customer", fontSize = 11.sp)
                    }

                    TextButton(
                        onClick = {
                            scope.launch {
                                authProvider.login("merchant@jubago.ssd", "password123", UserRole.MERCHANT) { success, _ ->
                                    if (success) {
                                        authProvider.currentUser.value?.let(onSuccess)
                                        onDismiss()
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("auth_quick_demo_merchant")
                    ) {
                        Text("Demo Vendor", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
