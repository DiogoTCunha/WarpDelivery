package edu.isel.pdm.warperapplication.web.entities

data class Warper (
    val username : String?,
    val firstname : String?,
    val lastname : String?,
    val phonenumber : String?,
    val email : String?,
    val vehicles : List<Vehicle>?
)

data class WarperEdit(
    val firstname: String? = null,
    val lastname: String? = null,
    val phonenumber: String? = null,
    val email: String? = null,
    val password: String? = null,
)

data class ActiveWarper(
    val vehicle : String,
    val location : LocationEntity,
    val notificationToken: String
)
