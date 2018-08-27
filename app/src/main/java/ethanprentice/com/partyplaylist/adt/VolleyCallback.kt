package ethanprentice.com.partyplaylist.adt

import android.util.Log
import com.android.volley.TimeoutError
import com.android.volley.VolleyError

/**
 * Created by Ethan on 2018-06-03.
 */

interface VolleyCallback <in T> {
    fun onSuccess(result : T)
    fun onError(error : VolleyError)

    fun defaultError(tag : String, error: VolleyError) {
        Log.e(tag, error.toString())
        if (error !is TimeoutError) {
            try {
                Log.e(tag, String(error.networkResponse.data, charset("UTF-8")))
            } catch (e : NullPointerException) {
                Log.e(tag, "Unknown exception type")
            }
        }
    }
}


