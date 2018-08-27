package ethanprentice.com.partyplaylist.spotifyAPI

import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.JsonParser
import ethanprentice.com.partyplaylist.adt.Track
import ethanprentice.com.partyplaylist.adt.User
import ethanprentice.com.partyplaylist.adt.VolleyCallback
import org.codehaus.jackson.map.ObjectMapper

/**
 * Created by Ethan on 2018-06-13.
 */

public class GetTrackInfo {
    // https://api.spotify.com/v1/me/tracks

    companion object {

        private val TAG = "GetTrackInfo"

        public fun getTracks(rQueue: RequestQueue, userCreds : User, limit: Int = 20, offset: Int = 0, callback : VolleyCallback<Array<Track>>) {

            val url = "https://api.spotify.com/v1/me/tracks?limit=$limit&offset=$offset"

            val jsonRequest = object : StringRequest(Method.GET, url,

                    Response.Listener { response ->
                        Log.d(TAG, response)
                        val tracks = parseTracks(response)
                        callback.onSuccess(tracks)
                    },
                    Response.ErrorListener { error ->
                       error.stackTrace.forEach { Log.e(TAG, it.methodName.toString()) }
                    }) {

                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("Authorization", userCreds.getSpotifyHeader())
                    Log.d(TAG, params["Authorization"])
                    return params
                }
            }

            rQueue.add(jsonRequest)
        }


        private fun parseTracks(jsonStr : String) : Array<Track> {
            val objMapper = ObjectMapper()
            val trackArr = Array(12, {_ -> Track() })
            val tracks = JsonParser().parse(jsonStr).asJsonObject["items"].asJsonArray

            for ((i, track) in tracks.withIndex()) {
                trackArr[i] = objMapper.readValue(track.asJsonObject["track"].toString(), Track::class.java)
            }

            return trackArr
        }



    }

}