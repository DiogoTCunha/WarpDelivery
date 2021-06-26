package com.isel.warpDelivery.inputmodels

import com.isel.warpDelivery.dataAccess.DAO.Delivery
import com.isel.warpDelivery.model.Location


data class RequestDeliveryInputModel(
    val storeId : Long,
    val userPhone : String,
    val deliverySize : Size,
    val address : String,
    val deliveryLocation : Location
) {
    fun toDelivery(warperUsername: String, clientUsername: String): Delivery {
        return Delivery(null, warperUsername, clientUsername, storeId, "Delivering", userPhone, null,
        null, null, deliveryLocation.latitude, deliveryLocation.longitude, address, null, deliverySize, emptyList())
    }
}

enum class Size(val text: String) {
    SMALL("small"), MEDIUM("medium"), LARGE("large");

    companion object {
        fun fromText(text: String): Size?{
            for (value in values()) {
                if (value.text.equals(text,ignoreCase = true)) {
                    return value
                }
            }
            return null
        }
    }
}

data class ActiveWarperInputModel(
    val username : String,
    val vehicleRegistration : String,
    val location : Location,
    val notificationToken: String
)