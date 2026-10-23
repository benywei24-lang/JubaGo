package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.DeliveryStatus
import com.example.model.OrderBooking
import com.example.model.PaymentMethod
import com.example.model.ServiceCategory

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey
    val id: String,
    val serviceId: String,
    val title: String,
    val categoryName: String,
    val providerName: String,
    val customerName: String,
    val customerPhone: String,
    val deliveryAddress: String,
    val totalPriceSsp: Long,
    val totalPriceUsd: Double,
    val paymentMethodName: String,
    val paymentReference: String,
    val statusName: String,
    val courierName: String,
    val courierPhone: String,
    val courierVehicle: String,
    val courierRating: Double,
    val orderedAt: String,
    val etaMinutes: Int,
    val liveProgress: Float,
    val riderLocation: String,
    val isSynced: Boolean = false,
    val updatedAtTimestamp: Long = System.currentTimeMillis()
) {
    fun toDomainModel(): OrderBooking {
        val cat = try {
            ServiceCategory.valueOf(categoryName)
        } catch (e: Exception) {
            ServiceCategory.FOOD
        }

        val pay = try {
            PaymentMethod.valueOf(paymentMethodName)
        } catch (e: Exception) {
            PaymentMethod.M_GURUSH
        }

        val stat = try {
            DeliveryStatus.valueOf(statusName)
        } catch (e: Exception) {
            DeliveryStatus.CONFIRMED
        }

        return OrderBooking(
            id = id,
            serviceId = serviceId,
            title = title,
            category = cat,
            providerName = providerName,
            customerName = customerName,
            customerPhone = customerPhone,
            deliveryAddress = deliveryAddress,
            totalPriceSsp = totalPriceSsp,
            totalPriceUsd = totalPriceUsd,
            paymentMethod = pay,
            paymentReference = paymentReference,
            status = stat,
            courierName = courierName,
            courierPhone = courierPhone,
            courierVehicle = courierVehicle,
            courierRating = courierRating,
            orderedAt = orderedAt,
            etaMinutes = etaMinutes,
            liveProgress = liveProgress,
            riderCurrentLocationName = riderLocation
        )
    }

    companion object {
        fun fromDomainModel(model: OrderBooking, isSynced: Boolean = false): OrderEntity {
            return OrderEntity(
                id = model.id,
                serviceId = model.serviceId,
                title = model.title,
                categoryName = model.category.name,
                providerName = model.providerName,
                customerName = model.customerName,
                customerPhone = model.customerPhone,
                deliveryAddress = model.deliveryAddress,
                totalPriceSsp = model.totalPriceSsp,
                totalPriceUsd = model.totalPriceUsd,
                paymentMethodName = model.paymentMethod.name,
                paymentReference = model.paymentReference,
                statusName = model.status.name,
                courierName = model.courierName,
                courierPhone = model.courierPhone,
                courierVehicle = model.courierVehicle,
                courierRating = model.courierRating,
                orderedAt = model.orderedAt,
                etaMinutes = model.etaMinutes,
                liveProgress = model.liveProgress,
                riderLocation = model.riderCurrentLocationName,
                isSynced = isSynced,
                updatedAtTimestamp = System.currentTimeMillis()
            )
        }
    }
}
