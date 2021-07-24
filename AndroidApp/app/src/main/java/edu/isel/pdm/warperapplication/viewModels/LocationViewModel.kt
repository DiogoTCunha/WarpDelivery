package edu.isel.pdm.warperapplication.viewModels


import android.app.Application
import android.content.IntentSender
import android.os.Looper
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import edu.isel.pdm.warperapplication.WarperApplication
import org.osmdroid.util.GeoPoint


class LocationViewModel(app: Application) : AndroidViewModel(app) {
    var currentLocation = MutableLiveData<GeoPoint>()
    var pickupLocation = MutableLiveData<GeoPoint>()
    var deliveryLocation = MutableLiveData<GeoPoint>()


    private val app: WarperApplication by lazy {
        getApplication<WarperApplication>()
    }

    private val locationRequest = LocationRequest.create().apply {
        interval = 10000
        fastestInterval = 5000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private val builder = LocationSettingsRequest.Builder()
        .addLocationRequest(locationRequest)

    /*
    private val client: SettingsClient = LocationServices.getSettingsClient(app)
    val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        .addOnSuccessListener {   }
        .addOnFailureListener {  exception ->
            if (exception is ResolvableApiException){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(,
                        REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            } }

*/



    private val locationCallback: LocationCallback? = null



    fun initFirestore(){
        app.initFirestore(onStateChanged = {
            updateMapData(it)
        }, onSubscriptionError = {
            Toast.makeText(getApplication(), "Couldn't subscribe to map updates", Toast.LENGTH_LONG)
                .show()
        })
    }

    private fun updateMapData(data: Map<String, Any>) {
        var delivery = data["deliveryLoc"] as ArrayList<Double>
        deliveryLocation.postValue(GeoPoint(delivery[0], delivery[1]))
        //var pickup = data["pickupLoc"] as Array<String>
        //var current = data["currentLoc"] as Array<String>

    }



}



