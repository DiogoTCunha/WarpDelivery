package com.isel.warpDelivery.controllers

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.isel.warpDelivery.authentication.USER_ATTRIBUTE_KEY
import com.isel.warpDelivery.authentication.UserInfo
import com.isel.warpDelivery.authentication.WarperResource
import com.isel.warpDelivery.common.ISSUER
import com.isel.warpDelivery.common.KeyPair
import com.isel.warpDelivery.common.WARPERS_PATH
import com.isel.warpDelivery.common.encodePassword
import com.isel.warpDelivery.dataAccess.dataClasses.Delivery
import com.isel.warpDelivery.dataAccess.dataClasses.Vehicle
import com.isel.warpDelivery.dataAccess.dataClasses.WarperEdit
import com.isel.warpDelivery.dataAccess.mappers.*
import com.isel.warpDelivery.errorHandling.ApiException
import com.isel.warpDelivery.inputmodels.*
import com.isel.warpDelivery.model.ActiveWarper
import com.isel.warpDelivery.model.ActiveWarperRepository
import com.isel.warpDelivery.model.Location
import com.isel.warpDelivery.outputmodels.WarperOutputModel
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.util.*
import javax.servlet.http.HttpServletRequest


@RestController
@RequestMapping(WARPERS_PATH)
class WarperController(
    val warperMapper: WarperMapper, val deliveryMapper: DeliveryMapper, val vehicleMapper: VehicleMapper,
    val activeWarpers: ActiveWarperRepository, val keys: KeyPair
)
{

    var logger: Logger = LoggerFactory.getLogger(WarperController::class.java)

    companion object{
        private const val EXPIRATION_TIME = 60*30 //in seconds
    }


    @GetMapping("/{username}")
    fun getWarper(@PathVariable username: String): WarperOutputModel {
        val warper = warperMapper.read(username) ?: throw ApiException("Warper Not Found",HttpStatus.NOT_FOUND)
        return warper.toOutputModel()
    }

    @PostMapping
    fun addWarper(@RequestBody warper: WarperInputModel): ResponseEntity<Any> {
        warper.password = encodePassword(warper.password)
        val warperCreated = warperMapper.create(warper.toDao())
        return ResponseEntity.created(URI("$WARPERS_PATH/$warperCreated")).build()
    }


    @PostMapping("/Login")
    fun login(@RequestBody warperCredentials : WarperLoginInputModel): Map<String,String>{

        val warper = warperMapper.read(warperCredentials.username) ?:
            throw ApiException("Invalid Credentials" , HttpStatus.UNAUTHORIZED)

        val (salt, password) = warper.password.split(":")

        val encodedPasswordInput = encodePassword(warperCredentials.password,salt)

        if(password != encodedPasswordInput)
            throw ApiException("Invalid Credentials" , HttpStatus.UNAUTHORIZED)

        val token = JWT.create().withIssuer(ISSUER)
            .withClaim("id", warperCredentials.username)
            .withClaim("usertype", "WARPER")
            .withIssuedAt(Date(System.currentTimeMillis())) // Expiration time of 15 Minutes
            .withExpiresAt(Date(System.currentTimeMillis()+EXPIRATION_TIME*1000))
            .sign(Algorithm.RSA256(keys.publicKey,keys.privateKey))

        return mapOf("token" to token)
    }



    @DeleteMapping("/{username}")
    fun deleteWarper(@PathVariable username: String): ResponseEntity<String> {
        warperMapper.delete(username)
        return ResponseEntity.status(204).build()
    }

    @WarperResource
    @GetMapping("/vehicles")
    fun getVehicles(req: HttpServletRequest): ResponseEntity<List<Vehicle>> {
        val user = req.getAttribute(USER_ATTRIBUTE_KEY) as UserInfo
        val warperExists = warperMapper.read(user.id) ?: return ResponseEntity.notFound().build()
        val vehicles = vehicleMapper.readAll(user.id)
        return ResponseEntity.ok().body(vehicles)
    }

    @WarperResource
    @PutMapping()
    fun updateWarper(req: HttpServletRequest, @RequestBody warper: WarperEdit): ResponseEntity<Any> {
        //TODO: Handle sql exception on email
        val user = req.getAttribute(USER_ATTRIBUTE_KEY) as UserInfo
        val warperExists = warperMapper.read(user.id) ?: return ResponseEntity.notFound().build()
        warperMapper.update(warper, user.id)
        return ResponseEntity.ok().build()
    }

    //-------------------------------------------------------------------------------
/*
    @GetMapping("/{username}/vehicles/{vehicleRegistration}")
    fun getVehicle(@PathVariable username: String, vehicleRegistration: String): ResponseEntity<Vehicle> {
        val vehicle = vehicleMapper.read(VehicleKey(username,vehicleRegistration))
        return ResponseEntity.ok().body(vehicle)
    }




    @DeleteMapping("/{username}/vehicles/{vehicleRegistration}")
    fun deleteVehicle(@PathVariable username: String, vehicleRegistration: String): ResponseEntity<String>{
        vehicleMapper.delete(VehicleKey(username,vehicleRegistration))
        return ResponseEntity.status(204).build()
    }

      @GetMapping("/{username}/deliveries/{deliveryId}/transitions")
    fun getDeliveryTransitions(@PathVariable deliveryId: Long): ResponseEntity<List<StateTransition>> {
        val transitions = deliveryMapper.getTransitions(deliveryId)
        return ResponseEntity.ok().body(transitions)
    }


    //-------------------------------------------------------------------------------
*/

    @PutMapping("/{username}/vehicles")
    fun addVehicle(
        @PathVariable username: String,
        @RequestBody vehicle: VehicleInputModel
    ): ResponseEntity<String> {
        val vehicleDao = Vehicle(username, vehicle.type, vehicle.registration)
        vehicleMapper.create(vehicleDao)
        return ResponseEntity.status(200).build()
    }


    /// NEED TESTING STILL
    @GetMapping("/{username}/deliveries/{deliveryId}")
    fun getDelivery(@PathVariable deliveryId: String): ResponseEntity<Delivery> {
        val delivery = deliveryMapper.read(deliveryId)
        return ResponseEntity.status(200).body(delivery)
    }

    @GetMapping("/{username}/deliveries")
    fun getDeliveries(@PathVariable username: String): ResponseEntity<List<Delivery>> {
        val deliveries = deliveryMapper.getDeliveriesByWarperUsername(username)
        return ResponseEntity.status(200).body(deliveries)
    }

    /* ROUTING RELATED ENDPOINTS */
    @WarperResource
    @PostMapping("/SetActive")
    fun addActiveWarper(req: HttpServletRequest, @RequestBody warperReq: ActiveWarperInputModel) : ResponseEntity<Any>{
        val warper = req.getAttribute(USER_ATTRIBUTE_KEY) as UserInfo
        val warperInfo = warperMapper.read(warper.id) ?: throw ApiException("Warper doesn't exist",HttpStatus.NOT_FOUND)

        val warperVehicle = warperInfo.vehicles.find { it.registration == warperReq.vehicle} ?:
        throw ApiException("Vehicle Not Found",HttpStatus.NOT_FOUND)

        val size = Size.fromText(warperVehicle.type)?:throw ApiException("Vehicle Not Found",HttpStatus.NOT_FOUND)

        print("ACTIVEEEE")
        activeWarpers.add(ActiveWarper(warper.id, warperReq.location, size ,warperReq.notificationToken))

        return ResponseEntity.status(200).build()
    }

    @WarperResource
    @PostMapping("/confirmDelivery")
    fun confirmDelivery(req: HttpServletRequest){
        val warper = req.getAttribute(USER_ATTRIBUTE_KEY) as UserInfo
        val activeWarper = activeWarpers.removeActiveWarper(warper.id)
        if(activeWarper != null)
            deliveryMapper.updateState(activeWarper.delivery.id, DeliveryState.DELIVERED)
    }

    @WarperResource
    @PutMapping("/location")
    fun updateWarperLocation(req: HttpServletRequest, @RequestBody location: Location){
        val warper = req.getAttribute(USER_ATTRIBUTE_KEY) as UserInfo
        activeWarpers.updateLocation(warper.id,location)
    }

    @WarperResource
    @PutMapping("/SetInactive")
    fun removeActiveWarper(req: HttpServletRequest){
        val warper = req.getAttribute(USER_ATTRIBUTE_KEY) as UserInfo
        activeWarpers.remove(warper.id)
    }



}


