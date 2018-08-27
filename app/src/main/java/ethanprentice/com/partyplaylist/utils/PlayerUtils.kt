package ethanprentice.com.partyplaylist.utils

import android.os.AsyncTask
import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.TimeoutError
import com.android.volley.toolbox.StringRequest
import ethanprentice.com.partyplaylist.adt.*
import ethanprentice.com.partyplaylist.constants.ClientConstants
import org.codehaus.jackson.map.ObjectMapper

class PlayerUtils {

    companion object {

        private val TAG = "PlayerUtils"

        class GetPlayerTask(private val rQueue: RequestQueue, private val party : Party, private val user: User, private val callback: VolleyCallback<PlayerData?>) : AsyncTask<Void, Void, String?>() {
            override fun doInBackground(vararg p0: Void?): String? {
                getPlayer(rQueue, party, user, callback)
                return null
            }
        }

        class UpdateStatusTask(private val rQueue: RequestQueue, private val party : Party,
                                      private val user: User, private val status : PlayerStatus) : AsyncTask<Void, Void, String?>() {

            override fun doInBackground(vararg p0: Void?): String? {
                updateStatus(rQueue, party, user, status)
                return null
            }
        }

        class UpdateTrackTask(private val rQueue: RequestQueue, private val party : Party,
                              private val user: User, private val trackNum : Int) : AsyncTask<Void, Void, String?>() {

            override fun doInBackground(vararg p0: Void?): String? {
                updateTrack(rQueue, party, user, trackNum)
                return null
            }
        }

        private fun getPlayer(rQueue : RequestQueue, party : Party, user : User, callback: VolleyCallback<PlayerData?>) {
            val url = ClientConstants.BASE_URL + "/rest/player/get?partyID=${party.ID}"

            val sRequest = object : StringRequest(Method.GET, url,

                Response.Listener<String> { response ->
                    val playerData = ObjectMapper().readValue(response, PlayerData::class.java)
                    callback.onSuccess(playerData)
                },

                Response.ErrorListener { error ->
                    print (error)
                }) {

                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Authorization"] = user.shakaHeader
                    return headers
                }

            }

            rQueue.add(sRequest)
        }

        private fun updateStatus(rQueue : RequestQueue, party : Party, user : User, status : PlayerStatus) {
            val url = ClientConstants.BASE_URL + "/rest/player/updateStatus?partyID=${party.ID}"

            val jsonBytes = ObjectMapper().writeValueAsBytes(status)

            val sRequest = object : StringRequest(Method.POST, url,

                    Response.Listener<String> { response ->
                        print (response)
                    },

                    Response.ErrorListener { error ->
                        Log.e(TAG, error.toString())
                        if (error !is TimeoutError) {
                            Log.e(TAG, String(error.networkResponse.data, charset("UTF-8")))
                        }
                    }) {

                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-Type"] ="application/json"
                    headers["Authorization"] = user.shakaHeader
                    return headers
                }

                override fun getBody(): ByteArray {
                    return jsonBytes
                }

            }

            rQueue.add(sRequest)
        }

        private fun updateTrack(rQueue : RequestQueue, party : Party, user : User, trackNum : Int) {
            val url = ClientConstants.BASE_URL + "/rest/player/updateTrack?partyID=${party.ID}"

            val jsonBytes = ObjectMapper().writeValueAsBytes(trackNum)

            val sRequest = object : StringRequest(Method.POST, url,

                    Response.Listener<String> { response ->
                        print (response)
                    },

                    Response.ErrorListener { error ->
                        Log.e(TAG, error.toString())
                        if (error !is TimeoutError) {
                            Log.e(TAG, String(error.networkResponse.data, charset("UTF-8")))
                        }
                    }) {

                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-Type"] ="application/json"
                    headers["Authorization"] = user.shakaHeader
                    return headers
                }

                override fun getBody(): ByteArray {
                    return jsonBytes
                }

            }

            rQueue.add(sRequest)
        }

    }
}