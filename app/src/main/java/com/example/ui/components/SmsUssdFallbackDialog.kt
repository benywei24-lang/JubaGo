package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.AppLanguage
import com.example.model.OrderBooking
import java.text.NumberFormat
import java.util.Locale

/**
 * Offline SMS / USSD Fallback Dialog.
 * Allows customers across South Sudan to dispatch orders directly via SMS Gateway or USSD
 * when mobile internet data is unavailable or along regional corridors (Nimule, Yei).
 */
@Composable
fun SmsUssdFallbackDialog(
    order: OrderBooking,
    currentLanguage: AppLanguage = AppLanguage.ENGLISH,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isArabic = currentLanguage == AppLanguage.JUBA_ARABIC
    val sspFormatted = NumberFormat.getNumberInstance(Locale.US).format(order.totalPriceSsp)

    val gatewayPhone = "+211921234567"
    val smsBody = "JUBAGO ORDER #${order.id} | ${order.title} | SSP $sspFormatted | ${order.customerName} (${order.customerPhone}) | Loc: ${order.deliveryAddress} | Pay: ${order.paymentMethod.title}"
    val ussdCode = "*123*${order.id.takeLast(4)}*${order.totalPriceSsp}#"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(22.dp))
                .testTag("sms_ussd_fallback_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.errorContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WifiOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isArabic) "إرسال الطلب بدون نت (SMS / USSD)" else "Offline SMS & USSD Gateway",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isArabic) "شغال في كل شبكات زين، إم تي إن، وديجيتل" else "Works on Zain, MTN & Digitel without data",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Info banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (isArabic) "الإنترنت ضعيف أو مقطوع؟" else "No Data Connection in Juba?",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isArabic)
                                "تقدر ترسل تفاصيل حجزك برسالة SMS عادية لمركز خدمة جوبا قو، والمندوب حيوصلك مباشرة."
                            else
                                "Send this booking directly through cellular SMS to our central dispatch gateway (+211 92 123 4567). Our nearest rider will head to your location immediately.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // SMS Payload Preview Box
                Text(
                    text = if (isArabic) "نص الرسالة المجهز للإرسال:" else "Prepared SMS Gateway Text:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = smsBody,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action 1: Send via SMS App
                Button(
                    onClick = {
                        val smsUri = Uri.parse("smsto:$gatewayPhone")
                        val intent = Intent(Intent.ACTION_SENDTO, smsUri).apply {
                            putExtra("sms_body", smsBody)
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Fallback
                            val alt = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("sms:$gatewayPhone?body=${Uri.encode(smsBody)}")
                            }
                            context.startActivity(alt)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("sms_fallback_send_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isArabic) "إرسال عبر رسالة SMS لـ $gatewayPhone" else "Send via SMS to $gatewayPhone",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action 2: Direct USSD Dial Code
                OutlinedButton(
                    onClick = {
                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(ussdCode)))
                        context.startActivity(dialIntent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("ussd_fallback_dial_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isArabic) "إتصال سريع عبر كود الـ USSD ($ussdCode)" else "Dial USSD Shortcode ($ussdCode)",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
