package ethanprentice.com.partyplaylist.spotifyAPI

import android.content.Intent
import android.util.Base64
import android.util.Log
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.google.gson.annotations.SerializedName
import com.android.volley.AuthFailureError
import com.google.gson.JsonParser
import com.spotify.sdk.android.authentication.AuthenticationClient
import com.spotify.sdk.android.authentication.AuthenticationResponse
import ethanprentice.com.partyplaylist.adt.User
import ethanprentice.com.partyplaylist.adt.SpotifyImage
import ethanprentice.com.partyplaylist.adt.VolleyCallback
import org.codehaus.jackson.map.ObjectMapper
import java.util.*


/**
 * Created by Ethan on 2018-06-03.
 */

class Authentication {

    companion object {
        val TAG = "Authentication"
        var user: User? = null

        val REDIRECT_URI = "http://ektura.com"
        val CLIENT_ID = "c167ad7f914344f8ad5895078ff6374c"
        val CLIENT_SECRET = "53d94887575140de8796941c43999843"

        // REQUEST_CODE must be within 0-65535 (16-bit)
        val REQUEST_CODE = 3615

        public var authenticated = false

        fun onSignIn(rQueue : RequestQueue, requestCode : Int, resultCode: Int, intent: Intent, callback : VolleyCallback<User>) : AuthenticationResponse? {

            if (requestCode == REQUEST_CODE) {
                val response = AuthenticationClient.getResponse(resultCode, intent)

                return when (response.type) {

                    AuthenticationResponse.Type.CODE -> {
                        Log.d(TAG, "Authentication successful.")
                        // TODO: Send a request to server to add this user to database
                        getAuthCredentials(rQueue, response.code, callback)

                        response
                    }

                    AuthenticationResponse.Type.ERROR -> {
                        Log.e(TAG, "Authentication error.")
                        null
                    }
                    else -> {
                        Log.e(TAG, "Wrong authentication type.")
                        null
                    }
                }
            } else {
                Log.e(TAG, "Invalid request code.")
                return null
            }

        }

        public fun getAuthCredentials(rQueue : RequestQueue, code : String, callback : VolleyCallback<User>) {

            val url = "https://accounts.spotify.com/api/token"
            val requestBody = TokenRequestBody(code, "authorization_code", REDIRECT_URI, CLIENT_ID, CLIENT_SECRET)

            // request to get token from SpotifyAPI
            val sRequest = object : StringRequest(Request.Method.POST, url,

                Response.Listener { response ->
                    Log.d(TAG, response)
                    Authentication.setUserCredentials(rQueue, response, callback)
                },
                Response.ErrorListener { error ->
                    Log.e(TAG, error.toString())
                }) {

                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("Authorization", "Basic " + Base64.encodeToString((requestBody.clientID + ":" + requestBody.clientSecret).toByteArray(), Base64.NO_WRAP))
                    return params
                }

                @Throws(AuthFailureError::class)
                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("grant_type", requestBody.grantType)
                    params.put("code", requestBody.code)
                    params.put("redirect_uri", requestBody.redirectURI)
                    return params
                }
            }

            rQueue.add(sRequest)
        }

        private fun setUserCredentials(rQueue : RequestQueue, response : String, callback : VolleyCallback<User>) : User? {
            val json = JsonParser().parse(response).asJsonObject
            Log.d(TAG, json.toString())
            val user : User = ObjectMapper().readValue<User>(response, User::class.java)

            val url = "https://api.spotify.com/v1/me"

            val sRequest = object : StringRequest(Request.Method.GET, url,

                    Response.Listener { response ->
                        val jsonResponse = JsonParser().parse(response).asJsonObject

                        Log.i(TAG, jsonResponse["images"].toString())

                        user.username = jsonResponse["id"].asString
                        user.displayName = jsonResponse["display_name"].asString
                        user.images = ObjectMapper().readValue(jsonResponse["images"].toString(), Array<SpotifyImage>::class.java)

                        this.user = user

                        callback.onSuccess(user)
                    },
                    Response.ErrorListener { error ->
                        Log.e(TAG, error.toString())
                    }) {

                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("Authorization", "${user.tokenType} ${user.token}")
                    return params
                }
            }

            rQueue.add(sRequest)

            return user
        }


    }

    data class TokenRequestBody (
        val code : String,

        @SerializedName("grant_type")
        val grantType : String,

        @SerializedName("redirect_uri")
        val redirectURI : String,

        @SerializedName("client_id")
        val clientID : String,

        @SerializedName("client_secret")
        val clientSecret : String
    )

}