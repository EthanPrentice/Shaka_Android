package ethanprentice.com.partyplaylist.utils

import android.os.AsyncTask
import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson
import ethanprentice.com.partyplaylist.adt.User
import ethanprentice.com.partyplaylist.adt.VolleyCallback
import ethanprentice.com.partyplaylist.constants.ClientConstants
import org.codehaus.jackson.map.ObjectMapper
import org.json.JSONArray
import org.json.JSONObject

/**
 * Created by Ethan on 2018-06-10.
 */
class UserUtils {

    companion object {

        public fun getUsers(rQueue : RequestQueue, partyID : String, user : User, callback: VolleyCallback<Array<User>>) {
            val url = ClientConstants.BASE_URL + "/rest/users/getUsers?partyID=$partyID"

            val jsonRequest = object : JsonArrayRequest(Method.GET, url, null,

                    Response.Listener<JSONArray> { response ->
                        callback.onSuccess(ObjectMapper().readValue(response.toString(), Array<User>::class.java))
                    },
                    Response.ErrorListener { error ->
                        callback.onError(error)
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



        val TAG = "UserUtils"
    }
}