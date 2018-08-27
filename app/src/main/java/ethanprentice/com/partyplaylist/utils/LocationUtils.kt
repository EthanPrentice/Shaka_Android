package ethanprentice.com.partyplaylist.utils

import android.content.Context
import android.location.LocationManager
import android.content.Context.LOCATION_SERVICE
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.util.Log
import com.android.volley.RequestQueue
import ethanprentice.com.partyplaylist.adt.User


/**
 * Created by Ethan on 2018-07-20.
 */
class LocationUtils {

    companion object {

        private var isGPSEnabled = false
        private var isNetworkEnabled = false

        private var location : Location? = null

        private val updateDistance = 5F
        private val updateTime = (1000 * 30 * 1).toLong()

        private var locationManager: LocationManager? = null
        private var locationListener : LocationListener? = null

        @Throws (SecurityException::class)
        public fun getLastLocation() : Location? {
            try {
                isGPSEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
                isNetworkEnabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

                if (isGPSEnabled) {
                    return locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)

                } else if (isNetworkEnabled) {
                    return locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

            return null
        }

        @Throws(SecurityException::class)
        public fun requestLocation(context : Context?, rQueue : RequestQueue, user : User): Location? {

            if (context == null) {
                return null
            }

            locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location?) {
                    if (location != null) {
                        PartyUtils.updateThisLocation(rQueue, user, location)
                        Log.i("TEST", "You are at : ${location.latitude}, ${location.latitude}")
                    }
                }

                override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
                    // Do nothing
                }

                override fun onProviderEnabled(p0: String?) {
                    // Do nothing
                }

                override fun onProviderDisabled(p0: String?) {
                    // Do nothing
                }
            }

            try {
                locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager

                isGPSEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
                isNetworkEnabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

                if (isGPSEnabled) {
                    locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, updateTime, updateDistance, locationListener)

                } else if (isNetworkEnabled) {
                    locationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, updateTime, updateDistance, locationListener)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

            return location
        }

        public fun endLocation() {
            try {
                locationManager?.removeUpdates(locationListener)
                locationManager = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}