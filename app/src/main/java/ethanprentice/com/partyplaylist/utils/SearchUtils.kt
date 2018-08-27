package ethanprentice.com.partyplaylist.utils

import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.TimeoutError
import com.android.volley.toolbox.StringRequest
import ethanprentice.com.partyplaylist.adt.SearchResult
import ethanprentice.com.partyplaylist.adt.User
import ethanprentice.com.partyplaylist.adt.VolleyCallback
import org.codehaus.jackson.map.ObjectMapper

class SearchUtils {

    companion object {

        val TAG = "SearchUtils"

        public fun performSearch (rQueue : RequestQueue, user : User, query : String, offset : Int = 0, types : Array<String> = arrayOf("artist","album","playlist","track"), callback : VolleyCallback<SearchResult>) {
            val formattedQuery = query.replace(" ", "%20")
            val formattedTypes = types.joinToString { s -> s }.replace(" ","")

            val url = "https://api.spotify.com/v1/search?q=$formattedQuery&type=$formattedTypes&offset=$offset"

            val sRequest = object : StringRequest(Method.GET, url,

                    Response.Listener<String> { response ->
                        val searchResult = ObjectMapper().readValue(response, SearchResult::class.java)
                        callback.onSuccess(searchResult)
                    },
                    Response.ErrorListener { error ->
                        Log.e(SearchUtils.TAG, error.toString())
                        if (error !is TimeoutError) {
                            Log.e("HEY", String(error.networkResponse.data, charset("UTF-8")))
                        }
                    }) {

                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params["Authorization"] = user.spotifyHeader
                    return params
                }
            }

            rQueue.add(sRequest)
        }

    }
}