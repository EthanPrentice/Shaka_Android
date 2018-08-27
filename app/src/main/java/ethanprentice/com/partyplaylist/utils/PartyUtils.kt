package ethanprentice.com.partyplaylist.utils


import android.location.Location
import android.os.AsyncTask
import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.TimeoutError
import com.android.volley.toolbox.StringRequest
import ethanprentice.com.partyplaylist.adt.Party
import ethanprentice.com.partyplaylist.adt.User
import ethanprentice.com.partyplaylist.adt.VolleyCallback
import ethanprentice.com.partyplaylist.constants.ClientConstants
import org.codehaus.jackson.map.ObjectMapper


/**
 * Created by Ethan on 2018-06-09.
 */
class PartyUtils {

    companion object {

        private val TAG = "PartyUtils"

        public class CreatePartyTask (private val rQueue : RequestQueue, private val user : User, private val callback : VolleyCallback<Party?>) : AsyncTask<Void, Void, String?>() {
            override fun doInBackground(vararg p0: Void?): String? {
                createParty(rQueue, user, callback)
                return null
            }
        }

        public class JoinPartyTask (private val rQueue : RequestQueue, private val user : User, private val partyID : String, private val callback : VolleyCallback<Party?>) : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg p0: Void?): Void? {
                joinParty(rQueue, user, partyID, callback)
                return null
            }
        }

        private fun createParty(rQueue : RequestQueue, userCreds : User, callback : VolleyCallback<Party?>) {
            val url = ClientConstants.BASE_URL + "/rest/parties/create"

            val jsonStr = ObjectMapper().writeValueAsString(userCreds)

            val sRequest = object : StringRequest(Method.POST, url,

                    Response.Listener<String> { response ->
                        val party = ObjectMapper().readValue(response, Party::class.java)
                        callback.onSuccess(party)
                    },
                    Response.ErrorListener { error ->
                        Log.e(TAG, error.toString())
                        if (error !is TimeoutError) {
                            Log.e("HEY", String(error.networkResponse.data, charset("UTF-8")))
                        }
                    }) {

                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params["Content-Type"] = "application/json"
                    return params
                }

                override fun getBody(): ByteArray {
                    return jsonStr.toByteArray()
                }
            }

            rQueue.add(sRequest)
        }

        private fun joinParty(rQueue : RequestQueue, user : User, partyID : String, callback: VolleyCallback<Party?>) {
            val url = ClientConstants.BASE_URL + "/rest/parties/join?partyID=$partyID"

            val jsonStr = ObjectMapper().writeValueAsString(user)

            val sRequest = object : StringRequest(Method.POST, url,

                    Response.Listener<String> { response ->
                        val party = ObjectMapper().readValue(response, Party::class.java)
                        callback.onSuccess(party)
                    },
                    Response.ErrorListener { error ->
                        Log.e(TAG, error.toString())
                        if (error !is TimeoutError) {
                            Log.e("HEY", String(error.networkResponse.data, charset("UTF-8")))
                        }
                    }) {

                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("Content-Type", "application/json")
                    return params
                }

                override fun getBody(): ByteArray {
                    return jsonStr.toByteArray()
                }
            }

            rQueue.add(sRequest)
        }

        private fun getParty(rQueue : RequestQueue, partyID : String, user : User, callback : VolleyCallback<Party>) {
            val url = ClientConstants.BASE_URL + "/rest/parties/get"

            val sRequest = object : StringRequest(Method.POST, url,

                    Response.Listener { response ->
                        callback.onSuccess( ObjectMapper().readValue(response, Party::class.java) )
                    },
                    Response.ErrorListener { error ->
                        callback.onError(error)
                    }
            ) {
                override fun getHeaders(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params.put("Authorization", user.shakaHeader)
                    return params
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("partyID", partyID)
                    return params
                }
            }

            rQueue.add(sRequest)
        }

        public fun getNearbyParties(rQueue : RequestQueue, lat : Double, lng : Double, callback: VolleyCallback<Array<Party>>) {
            val url = ClientConstants.BASE_URL + "/rest/parties/nearby?lat=$lat&lng=$lng"

            val sRequest = object : StringRequest(Method.GET, url,
                    Response.Listener { response ->
                        val party = ObjectMapper().readValue(response, Array<Party>::class.java)
                        callback.onSuccess(party)
                    },
                    Response.ErrorListener { error ->
                        callback.onError(error)
                    }
            ) { }

            rQueue.add(sRequest)
        }

        public fun updateThisLocation(rQueue : RequestQueue, user : User, location : Location) {
            val url = ClientConstants.BASE_URL + "/rest/parties/updateLocation?lat=${location.latitude}&lng=${location.longitude}"

            val sRequest = object : StringRequest(Method.GET, url,
                Response.Listener { _ -> },
                Response.ErrorListener { _ -> }
            ) {

                override fun getHeaders(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params.put("Authorization", user.shakaHeader)
                    return params
                }

            }

            rQueue.add(sRequest)
        }
    }
}