package com.isel.warpDelivery.dataAccess.dataClasses

import com.isel.warpDelivery.inputmodels.Size
import com.isel.warpDelivery.pubSub.DeliveryMessage
import java.sql.Timestamp

class Delivery (val deliveryId : String?,
                val warperUsername: String?,
                val clientUsername : String?,
                val storeId: String,
                val state : String,
                val clientPhone: String,
                val purchaseDate : Timestamp?,
                var deliverDate : Timestamp?,
                var rating : Int?,
                val deliverLatitude: Double,
                val deliverLongitude: Double,
                val deliverAddress: String,
                var reward: Float?,
                val type : Size,
                var transitions : List<StateTransition>?)