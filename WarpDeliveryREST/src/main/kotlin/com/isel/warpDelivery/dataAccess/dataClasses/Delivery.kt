package com.isel.warpDelivery.dataAccess.dataClasses

import com.fasterxml.jackson.annotation.JsonInclude
import com.isel.warpDelivery.inputmodels.Size
import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.StatementContext
import java.lang.IllegalStateException
import java.sql.ResultSet
import java.sql.Timestamp

enum class DeliveryState(val text : String) {
    LOOKING_FOR_WARPER("Looking for Warper"),
    DELIVERING("Delivering"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled");

    companion object {
        fun fromText(text: String): DeliveryState? {
            for (value in values()) {
                if (value.text.equals(text, ignoreCase = true)) {
                    return value
                }
            }
            return null
        }
    }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
class Delivery (val deliveryId : String?,
                val warperUsername: String?,
                val storeId: String,
                val state : DeliveryState,
                val clientPhone: String,
                val purchaseDate : Timestamp?,
                var deliverDate : Timestamp?,
                val deliverLatitude: Double,
                val deliverLongitude: Double,
                val deliverAddress: String,
                var storeName: String?,
                val type : Size)


data class DeliveryEdit(
    val warperUsername: String?,
    val state : String?,
    val rating : Int ?,
    val reward: Float ?
)




class DeliveryStateColumnMapper : ColumnMapper<DeliveryState>{
    override fun map(r: ResultSet, columnNumber: Int, ctx: StatementContext): DeliveryState {
        return DeliveryState.fromText(r.getString(columnNumber)) ?:
            throw IllegalStateException("DeliveryState has an invalid value")
    }
}
