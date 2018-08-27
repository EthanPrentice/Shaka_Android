package ethanprentice.com.partyplaylist.requests.user

import android.widget.LinearLayout
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import ethanprentice.com.partyplaylist.MainActivity
import ethanprentice.com.partyplaylist.R
import ethanprentice.com.partyplaylist.adt.User
import ethanprentice.com.partyplaylist.adt.VolleyCallback
import ethanprentice.com.partyplaylist.constants.ClientConstants
import ethanprentice.com.partyplaylist.frags.PartyFragment
import ethanprentice.com.partyplaylist.requests.BasicRequest
import ethanprentice.com.partyplaylist.views.UserView
import org.codehaus.jackson.map.ObjectMapper

class UpdateUsers(rQueue : RequestQueue, frag : PartyFragment) : BasicRequest<Array<User>>(rQueue) {
    init {
        callback = object : VolleyCallback<Array<User>> {

            override fun onSuccess(result: Array<User>) {
                var update = false
                val sortedResult = PartyFragment.sortUsers(result)

                MainActivity.user = sortedResult.first { it.username == MainActivity.user.username }

                if (PartyFragment.users == null || PartyFragment.users!!.isEmpty() || result.size != PartyFragment.users!!.size) {
                    update = true
                } else {
                    for (i in 0 until result.size) {
                        if (sortedResult[i] != PartyFragment.users!![i]) {
                            update = true
                            break
                        }
                    }
                }

                if (update) {
                    PartyFragment.users = sortedResult
                    frag.view?.findViewById<LinearLayout>(R.id.usersLL)?.removeAllViews()
                    for (user in PartyFragment.sortUsers(result)) {
                        frag.view?.findViewById<LinearLayout>(R.id.usersLL)?.addView(UserView(frag.context!!, user))
                    }
                }
            }

            override fun onError(error: VolleyError) {
                defaultError("UpdateUsers", error)
            }
        }

        val url = ClientConstants.BASE_URL + "/rest/users/getUsers?partyID=${MainActivity.party?.ID}"
        sRequest = object : StringRequest(Method.GET, url,
            Response.Listener { response ->
                val users = ObjectMapper().readValue(response, Array<User>::class.java)
                callback.onSuccess(users)
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

        }

    }

}