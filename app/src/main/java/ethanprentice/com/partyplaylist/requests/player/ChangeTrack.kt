package ethanprentice.com.partyplaylist.requests.player

import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import ethanprentice.com.partyplaylist.MainActivity
import ethanprentice.com.partyplaylist.adt.VolleyCallback
import ethanprentice.com.partyplaylist.constants.ClientConstants
import ethanprentice.com.partyplaylist.requests.BasicRequest

class ChangeTrack (rQueue : RequestQueue, newLocation : Int) : BasicRequest<String?>(rQueue) {

    init {
        callback = object : VolleyCallback<String?> {

            override fun onSuccess(result: String?) {
                // Do nothing
            }

            override fun onError(error: VolleyError) {
                defaultError("UpdateUsers", error)
            }
        }

        val url = ClientConstants.BASE_URL + "/rest/player/changeTrack?partyID=${MainActivity.party?.ID}"
        sRequest = object : StringRequest(Method.POST, url,
                Response.Listener { response ->
                    callback.onSuccess(response)
                },
                Response.ErrorListener { e ->
                    callback.onError(e)
                }
        ) {

            override fun getHeaders(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = MainActivity.user.shakaHeader
                params["Content-Type"] = "application/json"
                return params
            }

            override fun getBody(): ByteArray {
                return newLocation.toString().toByteArray()
            }

        }
    }

}