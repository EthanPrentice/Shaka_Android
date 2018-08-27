package ethanprentice.com.partyplaylist.requests

import android.os.AsyncTask
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import ethanprentice.com.partyplaylist.adt.VolleyCallback

abstract class BasicRequest<T>(val rQueue : RequestQueue) : AsyncTask<Void?, Void?, Void?>() {
    var sRequest : StringRequest? = null
    lateinit var callback : VolleyCallback<T>
    override fun doInBackground(vararg p0: Void?) : Void? {
        rQueue.add(sRequest)
        return null
    }
}