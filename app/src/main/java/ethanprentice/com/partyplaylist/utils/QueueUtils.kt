package ethanprentice.com.partyplaylist.utils

import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson
import ethanprentice.com.partyplaylist.adt.Track
import ethanprentice.com.partyplaylist.adt.User
import ethanprentice.com.partyplaylist.adt.VolleyCallback
import ethanprentice.com.partyplaylist.constants.ClientConstants
import org.codehaus.jackson.map.ObjectMapper
import org.json.JSONObject

/**
 * Created by Ethan on 2018-06-10.
 */

class QueueUtils {

    companion object {

        private val TAG = "PartyUtils"

        fun addToQueue(rQueue: RequestQueue, partyID : String, user : User, track: Track) {

            val url = ClientConstants.BASE_URL + "/rest/queue/add?partyID=$partyID"

            val jsonObj = JSONObject(Gson().toJson(track))

            val jsonRequest = object : JsonObjectRequest(Method.POST, url, jsonObj,

                    Response.Listener { response ->
                        Log.d(TAG, response.toString(2))
                    },
                    Response.ErrorListener { error ->
                        Log.e(TAG, error.toString())
                    }) {

                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params["Content-Type"] = "application/json"
                    params["Authorization"] = user.shakaHeader
                    return params
                }
            }

            rQueue.add(jsonRequest)
        }

        fun playNext(rQueue : RequestQueue, partyID : String, user : User, track: Track) {
            val url = ClientConstants.BASE_URL + "/rest/queue/playNext?partyID=$partyID"

            val jsonBytes = ObjectMapper().writeValueAsBytes(track)

            val sRequest = object : StringRequest(Method.POST, url,

                    Response.Listener { response ->
                        Log.d(TAG, response)
                    },
                    Response.ErrorListener { error ->
                        Log.e(TAG, error.toString())
                    }) {

                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params["Content-Type"] = "application/json"
                    params["Authorization"] = user.shakaHeader
                    return params
                }

                override fun getBody() : ByteArray {
                    return jsonBytes
                }
            }

            rQueue.add(sRequest)
        }

        fun insertIntoQueue(rQueue : RequestQueue, partyID : String, user : User, track: Track, queuePos : Int) {

            val url = ClientConstants.BASE_URL + "/rest/queue/insert?partyID=$partyID&queuePos=$queuePos"

            val jsonBytes = ObjectMapper().writeValueAsBytes(track)

            val sRequest = object : StringRequest(Method.POST, url,

                    Response.Listener { response ->
                        Log.d(TAG, response)
                    },
                    Response.ErrorListener { error ->
                        Log.e(TAG, error.toString())
                    }) {

                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params["Content-Type"] = "application/json"
                    params["Authorization"] = user.shakaHeader
                    return params
                }

                override fun getBody() : ByteArray {
                    return jsonBytes
                }
            }

            rQueue.add(sRequest)
        }

        fun removeFromQueue(rQueue : RequestQueue, partyID : String, user : User, track: Track) {
            val url = ClientConstants.BASE_URL + "/rest/queue/remove?partyID=$partyID"

            val jsonObj = JSONObject(Gson().toJson(track))

            val jsonRequest = object : JsonObjectRequest(Method.POST, url, jsonObj,

                    Response.Listener { response ->
                        Log.d(TAG, response.toString(2))
                    },
                    Response.ErrorListener { error ->
                        Log.e(TAG, error.toString())
                    }) {

                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("Content-Type", "application/json")
                    params.put("Authorization", user.shakaHeader)
                    return params
                }
            }

            rQueue.add(jsonRequest)
        }


        fun getQueue(rQueue : RequestQueue, partyID : String, user : User, callback: VolleyCallback<Array<Track>>) {
            val url = ClientConstants.BASE_URL + "/rest/queue/get?partyID=$partyID"

            val sRequest = object : StringRequest(Method.GET, url,

                    Response.Listener { response ->
                        val tracks = ObjectMapper().readValue(response, Array<Track>::class.java)
                        callback.onSuccess(tracks)
                    },
                    Response.ErrorListener { error ->
                        callback.onError(error)
                    }) {

                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params["Authorization"] = user.shakaHeader
                    return params
                }
            }

            rQueue.add(sRequest)
        }
    }


}


