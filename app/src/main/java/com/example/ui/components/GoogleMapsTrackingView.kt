package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeliveryStatus
import com.example.model.OrderBooking
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch

/**
 * Real geographic coordinates in Juba, South Sudan for accurate live tracking.
 */
object JubaGeoCoordinates {
    val HAI_CINEMA_CUSTOMER = LatLng(4.8516, 31.5825)
    val CUSTOM_MARKET_STORE = LatLng(4.8389, 31.5658)
    val AIRPORT_ROAD_DEPOT = LatLng(4.8720, 31.6011)
    val MINISTRIES_ROAD_JUNCTION = LatLng(4.8580, 31.5950)
    val TONGPING_RESIDENTIAL = LatLng(4.8630, 31.5880)
    val JUBA_BRIDGE_OVERLOOK = LatLng(4.8290, 31.6110)

    /**
     * Compute pickup coordinates based on service title / location
     */
    fun getPickupLocation(order: OrderBooking): LatLng {
        return when {
            order.title.contains("Flight", ignoreCase = true) || order.title.contains("Air", ignoreCase = true) -> AIRPORT_ROAD_DEPOT
            order.title.contains("Safari", ignoreCase = true) || order.title.contains("Car", ignoreCase = true) -> MINISTRIES_ROAD_JUNCTION
            order.title.contains("Nile", ignoreCase = true) || order.title.contains("Fish", ignoreCase = true) -> JUBA_BRIDGE_OVERLOOK
            else -> CUSTOM_MARKET_STORE
        }
    }

    /**
     * Compute delivery destination coordinates
     */
    fun getDestinationLocation(order: OrderBooking): LatLng {
        return when {
            order.deliveryAddress.contains("Tongping", ignoreCase = true) -> TONGPING_RESIDENTIAL
            order.deliveryAddress.contains("Airport", ignoreCase = true) -> AIRPORT_ROAD_DEPOT
            else -> HAI_CINEMA_CUSTOMER
        }
    }

    /**
     * Build realistic waypoints between pickup and destination through Juba's street network
     */
    fun buildRouteWaypoints(pickup: LatLng, destination: LatLng): List<LatLng> {
        val mid1 = LatLng(
            pickup.latitude + (destination.latitude - pickup.latitude) * 0.35,
            pickup.longitude + 0.006 // Route curve along Juba central avenue
        )
        val mid2 = LatLng(
            pickup.latitude + (destination.latitude - pickup.latitude) * 0.70,
            destination.longitude - 0.003
        )
        return listOf(pickup, mid1, mid2, destination)
    }

    /**
     * Interpolate live driver position along the route using liveProgress (0.0 to 1.0)
     */
    fun getLiveDriverPosition(pickup: LatLng, destination: LatLng, progress: Float): LatLng {
        val clamped = progress.coerceIn(0.0f, 1.0f)
        val waypoints = buildRouteWaypoints(pickup, destination)

        return when {
            clamped <= 0.33f -> {
                val subProgress = clamped / 0.33f
                interpolate(waypoints[0], waypoints[1], subProgress)
            }
            clamped <= 0.66f -> {
                val subProgress = (clamped - 0.33f) / 0.33f
                interpolate(waypoints[1], waypoints[2], subProgress)
            }
            else -> {
                val subProgress = (clamped - 0.66f) / 0.34f
                interpolate(waypoints[2], waypoints[3], subProgress)
            }
        }
    }

    private fun interpolate(from: LatLng, to: LatLng, fraction: Float): LatLng {
        val lat = from.latitude + (to.latitude - from.latitude) * fraction
        val lng = from.longitude + (to.longitude - from.longitude) * fraction
        return LatLng(lat, lng)
    }
}

/**
 * Google Maps SDK integration in Jetpack Compose to display the live location of
 * delivery drivers on the tracking screen when an order is in transit.
 */
@Composable
fun GoogleMapsTrackingView(
    order: OrderBooking,
    isOffline: Boolean = false,
    onSimulateStep: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    val pickupLocation = remember(order.id) { JubaGeoCoordinates.getPickupLocation(order) }
    val destinationLocation = remember(order.id) { JubaGeoCoordinates.getDestinationLocation(order) }

    // Live driver position based on order.liveProgress
    val driverPosition = remember(order.liveProgress) {
        JubaGeoCoordinates.getLiveDriverPosition(pickupLocation, destinationLocation, order.liveProgress)
    }

    val routeWaypoints = remember(pickupLocation, destinationLocation) {
        JubaGeoCoordinates.buildRouteWaypoints(pickupLocation, destinationLocation)
    }

    // Google Map Camera State centered initially on driver location
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(driverPosition, 14.5f)
    }

    // Marker states
    val driverMarkerState = rememberMarkerState(position = driverPosition)
    val pickupMarkerState = rememberMarkerState(position = pickupLocation)
    val destinationMarkerState = rememberMarkerState(position = destinationLocation)

    // Smoothly update driver marker position as driver moves
    LaunchedEffect(driverPosition) {
        driverMarkerState.position = driverPosition
        if (order.status == DeliveryStatus.ON_THE_WAY) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLng(driverPosition),
                1000
            )
        }
    }

    // Map customization options
    var mapType by remember { mutableStateOf(MapType.NORMAL) }
    var showNileMapFallback by remember { mutableStateOf(isOffline) }

    val isInTransit = order.status == DeliveryStatus.ON_THE_WAY ||
            order.status == DeliveryStatus.PREPARING ||
            order.status == DeliveryStatus.CONFIRMED

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("google_maps_tracking_container")
    ) {
        if (showNileMapFallback) {
            // Stylized vector map canvas fallback (for offline or preference)
            JubaMapCanvas(
                order = order,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Google Maps SDK Live View
            GoogleMap(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("google_map_view"),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    mapType = mapType,
                    isMyLocationEnabled = false,
                    isTrafficEnabled = true
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    compassEnabled = true,
                    mapToolbarEnabled = true,
                    rotationGesturesEnabled = true,
                    scrollGesturesEnabled = true,
                    tiltGesturesEnabled = true,
                    zoomGesturesEnabled = true
                )
            ) {
                // 1. Pickup Marker (Store/Merchant in Juba)
                Marker(
                    state = pickupMarkerState,
                    title = "Pickup: ${order.providerName}",
                    snippet = "Order #${order.id} Dispatch Point",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                )

                // 2. Delivery Destination Marker (Customer location)
                Marker(
                    state = destinationMarkerState,
                    title = "Destination: ${order.customerName}",
                    snippet = order.deliveryAddress,
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                )

                // 3. Live Delivery Driver Marker
                Marker(
                    state = driverMarkerState,
                    title = "${order.courierName} (${order.courierVehicle})",
                    snippet = "ETA: ${order.etaMinutes} mins • Status: ${order.status.label}",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )

                // GPS Accuracy Halo around Delivery Driver
                Circle(
                    center = driverPosition,
                    radius = 85.0, // meters
                    fillColor = Color(0x330288D1),
                    strokeColor = Color(0xFF0288D1),
                    strokeWidth = 3f
                )

                // Planned Route Polyline (Pickup -> Destination)
                Polyline(
                    points = routeWaypoints,
                    color = Color(0xFF1E88E5),
                    width = 12f
                )
            }
        }

        // Top Status Header Overlay
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
            shadowElevation = 4.dp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .testTag("maps_live_telemetry_badge")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            if (isInTransit) Color(0xFF4CAF50) else Color(0xFFFF9800)
                        )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = if (isInTransit) "Live Courier GPS Active" else "Order ${order.status.label}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${order.courierName} • ${order.riderCurrentLocationName}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Right-Side Controls: Recenter on Driver, Zoom bounds, Satellite toggle, Offline toggle
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Toggle between Google Maps SDK and Stylized Nile Vector Map
            FilledTonalIconButton(
                onClick = { showNileMapFallback = !showNileMapFallback },
                modifier = Modifier
                    .size(40.dp)
                    .testTag("toggle_map_engine_button")
            ) {
                Icon(
                    imageVector = if (showNileMapFallback) Icons.Default.Map else Icons.Default.Navigation,
                    contentDescription = "Switch Map Engine",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (!showNileMapFallback) {
                // Toggle Map Type: Normal <-> Satellite
                FilledTonalIconButton(
                    onClick = {
                        mapType = if (mapType == MapType.NORMAL) MapType.HYBRID else MapType.NORMAL
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("toggle_map_satellite_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "Toggle Satellite",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Recenter Camera onto Delivery Driver
                FilledTonalIconButton(
                    onClick = {
                        coroutineScope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(driverPosition, 15f),
                                800
                            )
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("recenter_driver_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CenterFocusStrong,
                        contentDescription = "Recenter on Driver",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Zoom to Fit Entire Route Bounds (Pickup, Driver, Destination)
                FilledTonalIconButton(
                    onClick = {
                        coroutineScope.launch {
                            val bounds = LatLngBounds.builder()
                                .include(pickupLocation)
                                .include(destinationLocation)
                                .include(driverPosition)
                                .build()
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngBounds(bounds, 100),
                                800
                            )
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("fit_route_bounds_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Fit Entire Route",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Bottom Telemetry Bar: Live Progress Bar and Step Simulator (for realistic in-transit testing)
        Surface(
            shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            shadowElevation = 6.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .testTag("driver_live_coords_panel")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DeliveryDining,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Driver GPS: %.4f° N, %.4f° E".format(driverPosition.latitude, driverPosition.longitude),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Transit Progress: ${(order.liveProgress * 100).toInt()}% • ETA ${order.etaMinutes}m",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (onSimulateStep != null && isInTransit) {
                    Surface(
                        onClick = onSimulateStep,
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.testTag("simulate_driver_step_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Step GPS",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Advance GPS",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}
