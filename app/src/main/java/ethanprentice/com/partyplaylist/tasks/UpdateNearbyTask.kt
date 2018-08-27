package ethanprentice.com.partyplaylist.tasks

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import ethanprentice.com.partyplaylist.MainActivity
import ethanprentice.com.partyplaylist.R
import ethanprentice.com.partyplaylist.adt.Party
import ethanprentice.com.partyplaylist.adt.VolleyCallback
import ethanprentice.com.partyplaylist.frags.LoginFragment
import ethanprentice.com.partyplaylist.utils.LocationUtils
import ethanprentice.com.partyplaylist.utils.PartyUtils
import ethanprentice.com.partyplaylist.views.NearbyPartyView

/**
 * Created by Ethan on 2018-07-20.
 */
class UpdateNearbyTask(frag : LoginFragment, private val updatePB : Boolean) : UpdateUITask(frag, updatePB) {

    override fun preExec() {
        showProgressBar(250)
    }

    override fun execute(context: Context?, view : View?, rQueue: RequestQueue) {
        try {
            preExec()
            setNearbyParties(context, view, rQueue)
        } catch ( e : Exception ) {
            e.printStackTrace()
        }
    }

    override fun postExec() {
        super.postExec()
        hideProgressBar()
    }

    @Throws(SecurityException::class)
    private fun setNearbyParties(context : Context?, view : View?, rQueue : RequestQueue) {
        val location = LocationUtils.getLastLocation()
        if (context == null || location == null) return

        val activity = frag.activity as MainActivity

        PartyUtils.getNearbyParties(rQueue, location.latitude, location.longitude, object : VolleyCallback<Array<Party>> {

            override fun onSuccess(result: Array<Party>) {
                postExec()

                view?.findViewById<LinearLayout>(R.id.nearbyPartiesLL)?.removeAllViews()

                val pViews = arrayOfNulls<NearbyPartyView>(result.size)

                for ( i in 0 until result.size ) {
                    val pView = NearbyPartyView(context, result[i], location.latitude, location.longitude)
                    pView.setOnClickListener {
                        PartyUtils.Companion.JoinPartyTask(rQueue, MainActivity.user, pView.party.ID, object : VolleyCallback<Party?> {
                            override fun onSuccess(result: Party?) {
                                if (result != null) {
                                    activity.joinedParty(result)
                                }
                            }

                            override fun onError(error: VolleyError) {

                            }
                        }).execute()

                    }
                    pViews[i] = pView
                }
                for (pView in LoginFragment.sortNearby(pViews)) {
                    view?.findViewById<LinearLayout>(R.id.nearbyPartiesLL)?.addView(pView)
                }

            }

            override fun onError(error: VolleyError) {
                Log.e(TAG, error.networkResponse?.data?.toString()+"")
            }
        })
    }

    companion object {
        val TAG = "UpdateNearbyTask"
    }
}