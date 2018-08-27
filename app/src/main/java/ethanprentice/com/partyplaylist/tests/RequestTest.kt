package ethanprentice.com.partyplaylist.tests

import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.NetworkResponse
import com.android.volley.VolleyLog
import com.android.volley.AuthFailureError
import com.google.gson.Gson
import ethanprentice.com.partyplaylist.adt.User
import java.io.UnsupportedEncodingException


/**
 * Created by Ethan on 2018-06-03.
 */
class RequestTest {

    companion object {
        public fun sendTestRequest(rQueue : RequestQueue) {

            var result : String? = null
            val url = "http://10.0.2.2:8080/PlaylistShare/rest/auth/test"

            // Request a string response from the provided URL.
            val stringRequest = StringRequest(Request.Method.GET, url,

                    Response.Listener<String> { response ->
                        // Display the first 500 characters of the response string.
                        result = "Response is: $response"
                    },
                    Response.ErrorListener {
                        result = "That didn't work!"
                    })

            // Add the request to the RequestQueue.
            rQueue.add(stringRequest)
        }

        public fun getUserData(token : String) : String? {
            val url = "https://api.spotify.com/v1/me"

            var returnVal : String? = null

            val sRequest = object : StringRequest(Request.Method.GET, url,
                Response.Listener { response ->
                    returnVal = response
                    Log.i("VOLLEY", response)
                },
                Response.ErrorListener { error ->
                    Log.e("VOLLEY", error.toString())
                }) {

                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("Authorization", "Bearer:$token")
                    params.put("Content-Type", "application/json")

                    return params
                }
            }

            return returnVal
        }

        public fun addParty(rQueue : RequestQueue, userCreds : User) {

            val url = "http://10.0.2.2:8080/PlaylistShare/rest/parties/add"

            val jsonBody = Gson().toJson(userCreds)
            val requestBody = jsonBody.toString()

            val sRequest = object : StringRequest(Request.Method.POST, url,

                    Response.Listener { response ->
                        Log.i("VOLLEY", response)
                    },
                    Response.ErrorListener { error ->
                        Log.e("VOLLEY", error.toString())
                    }) {

                    override fun getBodyContentType(): String {
                        return "application/json; charset=utf-8"
                    }

                    @Throws(AuthFailureError::class)
                    override fun getBody(): ByteArray? {
                        return try {
                            requestBody.toByteArray(Charsets.UTF_8)
                        } catch (uee: UnsupportedEncodingException) {
                            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8")
                            null
                        }
                    }

                    override fun parseNetworkResponse(response: NetworkResponse): Response<String> {
                        val responseString = response.statusCode.toString()
                        return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response))
                    }
                }

            rQueue.add(sRequest)

        }
    }
}