package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OrderBooking

@Composable
fun JubaMapCanvas(
    order: OrderBooking?,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )

    val progress = order?.liveProgress ?: 0.5f
    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFE8ECE9))
            .testTag("juba_map_canvas")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Draw River Nile (Flows diagonally through Juba from South to North)
            val riverPath = Path().apply {
                moveTo(width * 0.72f, 0f)
                cubicTo(
                    width * 0.76f, height * 0.3f,
                    width * 0.65f, height * 0.65f,
                    width * 0.70f, height
                )
                lineTo(width * 0.88f, height)
                cubicTo(
                    width * 0.82f, height * 0.65f,
                    width * 0.92f, height * 0.3f,
                    width * 0.86f, 0f
                )
                close()
            }
            drawPath(
                path = riverPath,
                color = Color(0xFF64B5F6) // Nile Blue
            )

            // 2. Draw Juba Bridge across the Nile
            val bridgeY = height * 0.55f
            drawLine(
                color = Color(0xFF455A64),
                start = Offset(width * 0.64f, bridgeY),
                end = Offset(width * 0.83f, bridgeY),
                strokeWidth = 14f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFFFFFFFF),
                start = Offset(width * 0.64f, bridgeY),
                end = Offset(width * 0.83f, bridgeY),
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )

            // 3. Main Roads Network in Juba
            // Airport Road (North to Central)
            drawLine(
                color = Color(0xFFCBD5E1),
                start = Offset(width * 0.40f, height * 0.08f),
                end = Offset(width * 0.45f, height * 0.42f),
                strokeWidth = 18f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFFFAFBFB),
                start = Offset(width * 0.40f, height * 0.08f),
                end = Offset(width * 0.45f, height * 0.42f),
                strokeWidth = 10f,
                cap = StrokeCap.Round
            )

            // Ministries Road (Cross town east-west to bridge)
            drawLine(
                color = Color(0xFFCBD5E1),
                start = Offset(width * 0.08f, height * 0.45f),
                end = Offset(width * 0.75f, height * 0.55f),
                strokeWidth = 20f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFFFAFBFB),
                start = Offset(width * 0.08f, height * 0.45f),
                end = Offset(width * 0.75f, height * 0.55f),
                strokeWidth = 12f,
                cap = StrokeCap.Round
            )

            // Custom Market to Gudele Road
            drawLine(
                color = Color(0xFFCBD5E1),
                start = Offset(width * 0.25f, height * 0.43f),
                end = Offset(width * 0.12f, height * 0.85f),
                strokeWidth = 16f,
                cap = StrokeCap.Round
            )

            // 4. City Landmarks Points & Labels
            // Airport (JUB)
            val airportPos = Offset(width * 0.40f, height * 0.10f)
            drawCircle(Color(0xFF0277BD), radius = 10f, center = airportPos)
            drawText(
                textMeasurer = textMeasurer,
                text = "✈ Juba Int. Airport (JUB)",
                topLeft = Offset(airportPos.x + 14f, airportPos.y - 12f),
                style = TextStyle(color = Color(0xFF1E293B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            )

            // Custom Market
            val customPos = Offset(width * 0.25f, height * 0.43f)
            drawCircle(Color(0xFFE65100), radius = 8f, center = customPos)
            drawText(
                textMeasurer = textMeasurer,
                text = "Custom Market",
                topLeft = Offset(customPos.x - 70f, customPos.y - 20f),
                style = TextStyle(color = Color(0xFF334155), fontSize = 10.sp, fontWeight = FontWeight.Medium)
            )

            // Konyo Konyo
            val konyoPos = Offset(width * 0.58f, height * 0.62f)
            drawCircle(Color(0xFF2E7D32), radius = 8f, center = konyoPos)
            drawText(
                textMeasurer = textMeasurer,
                text = "Konyo Konyo Market",
                topLeft = Offset(konyoPos.x - 50f, konyoPos.y + 12f),
                style = TextStyle(color = Color(0xFF334155), fontSize = 10.sp, fontWeight = FontWeight.Medium)
            )

            // Nile River Label
            drawText(
                textMeasurer = textMeasurer,
                text = "WHITE NILE RIVER",
                topLeft = Offset(width * 0.74f, height * 0.20f),
                style = TextStyle(color = Color(0xFF0D47A1), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            )

            // 5. Delivery Trajectory & Rider Live Positioning
            // Store / Pickup point (e.g. Nile Breeze / Airport / Konyo)
            val pickupPoint = Offset(width * 0.58f, height * 0.60f)
            // Customer destination point (e.g. Hai Cinema)
            val destPoint = Offset(width * 0.28f, height * 0.35f)

            // Route trajectory path
            val routePath = Path().apply {
                moveTo(pickupPoint.x, pickupPoint.y)
                quadraticTo(
                    width * 0.42f, height * 0.48f,
                    destPoint.x, destPoint.y
                )
            }

            // Draw dotted/dashed route line
            drawPath(
                path = routePath,
                color = Color(0xFF0D5C3A),
                style = Stroke(
                    width = 6f,
                    cap = StrokeCap.Round
                )
            )

            // Draw destination marker (Customer) with live pulse
            drawCircle(
                color = Color(0xFF0D5C3A).copy(alpha = 0.3f),
                radius = pulseRadius,
                center = destPoint
            )
            drawCircle(
                color = Color(0xFF0D5C3A),
                radius = 12f,
                center = destPoint
            )
            drawCircle(
                color = Color.White,
                radius = 5f,
                center = destPoint
            )
            drawText(
                textMeasurer = textMeasurer,
                text = "📍 Your Location (Hai Cinema)",
                topLeft = Offset(destPoint.x - 60f, destPoint.y - 28f),
                style = TextStyle(color = Color(0xFF0D5C3A), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
            )

            // Draw Store Pickup pin
            drawCircle(
                color = Color(0xFFD4AF37),
                radius = 10f,
                center = pickupPoint
            )
            drawCircle(
                color = Color.White,
                radius = 4f,
                center = pickupPoint
            )
            drawText(
                textMeasurer = textMeasurer,
                text = "🏪 ${order?.providerName ?: "Merchant"}",
                topLeft = Offset(pickupPoint.x + 12f, pickupPoint.y - 8f),
                style = TextStyle(color = Color(0xFF6B4E00), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            )

            // Calculate Rider current coordinate along the path according to liveProgress
            val riderX = pickupPoint.x + (destPoint.x - pickupPoint.x) * progress
            val riderY = pickupPoint.y + (destPoint.y - pickupPoint.y) * progress
            val riderPos = Offset(riderX, riderY)

            // Draw Courier Icon & Halo
            drawCircle(
                color = Color(0xFF1E88E5).copy(alpha = 0.35f),
                radius = 24f,
                center = riderPos
            )
            drawCircle(
                color = Color(0xFF1E88E5),
                radius = 15f,
                center = riderPos
            )
            drawCircle(
                color = Color.White,
                radius = 7f,
                center = riderPos
            )

            // Rider Tag
            drawText(
                textMeasurer = textMeasurer,
                text = "🏍 ${order?.courierName ?: "Boda Rider"}",
                topLeft = Offset(riderPos.x - 35f, riderPos.y + 18f),
                style = TextStyle(color = Color(0xFF0D47A1), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            )
        }

        // Overlay status pill in bottom-left
        Card(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f)),
            shape = RoundedCornerShape(10.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Text(
                text = "GPS Live: Juba, South Sudan • ~${order?.etaMinutes ?: 8} mins away",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF0D5C3A),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}
