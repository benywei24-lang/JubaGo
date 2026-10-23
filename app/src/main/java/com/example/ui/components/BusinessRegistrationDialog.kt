package com.example.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.BusinessRegistrationRequest

/**
 * Modal dialog wrapping the VendorRegistrationForm for business and vendor onboarding
 * with direct Firestore persistence and nationwide/global service activation.
 */
@Composable
fun BusinessRegistrationDialog(
    onDismiss: () -> Unit,
    onRegister: (BusinessRegistrationRequest) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("business_registration_dialog"),
            color = MaterialTheme.colorScheme.background
        ) {
            VendorRegistrationForm(
                onDismiss = onDismiss,
                onSuccess = onRegister
            )
        }
    }
}
