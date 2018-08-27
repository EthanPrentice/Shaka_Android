package ethanprentice.com.partyplaylist.frags

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import ethanprentice.com.partyplaylist.adt.User
import android.os.Handler
import android.widget.LinearLayout
import android.widget.TextView
import ethanprentice.com.partyplaylist.MainActivity
import ethanprentice.com.partyplaylist.R
import ethanprentice.com.partyplaylist.requests.user.UpdateUsers
import ethanprentice.com.partyplaylist.views.UserView


class PartyFragment : Fragment() {

    private var fragListener: OnFragmentInteractionListener? = null

    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rQueue = Volley.newRequestQueue(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_party, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity?)?.showPartyID()

        view.findViewById<TextView>(R.id.mainPartyID)?.text = MainActivity.party?.ID

        if (users != null && users!!.isNotEmpty()) {
            view.findViewById<LinearLayout>(R.id.usersLL)?.removeAllViews()
            for (user in sortUsers(users!!)) {
                view.findViewById<LinearLayout>(R.id.usersLL)?.addView(UserView(context!!, user))
            }
        }
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacksAndMessages(null)
    }

    override fun onResume() {
        super.onResume()
        var updatePB = true
        val delay = 3000L // milliseconds

        handler.post(object : Runnable {
            override fun run() {
                UpdateUsers(rQueue, this@PartyFragment).execute()
                updatePB = false
                handler.postDelayed(this, delay)
            }
        })
    }

    fun onButtonPressed(uri: Uri) {
        if (fragListener != null) {
            fragListener!!.onFragmentInteraction(uri)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            fragListener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        fragListener = null
    }

    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {

        lateinit var rQueue : RequestQueue
        var users : Array<User>? = null

        // TODO: Rename and change types and number of parameters
        fun newInstance (): PartyFragment {
            val fragment = PartyFragment()
            val args = Bundle()

            fragment.arguments = args
            return fragment
        }

        fun sortUsers(users : Array<User>) : Array<User> {
            users.sortWith(compareBy({ !it.owner }, { it.displayName }))
            return users
        }

    }
}
