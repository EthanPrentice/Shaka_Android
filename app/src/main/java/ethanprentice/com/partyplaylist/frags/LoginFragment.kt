package ethanprentice.com.partyplaylist.frags

import android.Manifest
import android.support.v4.app.Fragment
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.InputFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.android.volley.TimeoutError
import com.android.volley.VolleyError
import com.spotify.sdk.android.player.*
import ethanprentice.com.partyplaylist.MainActivity
import ethanprentice.com.partyplaylist.R
import ethanprentice.com.partyplaylist.adt.Party
import ethanprentice.com.partyplaylist.adt.VolleyCallback
import ethanprentice.com.partyplaylist.tasks.UpdateNearbyTask
import ethanprentice.com.partyplaylist.utils.PartyUtils
import ethanprentice.com.partyplaylist.views.NearbyPartyView

/**
 * Created by Ethan on 2018-07-17.
 */

class LoginFragment : Fragment(), Player.NotificationCallback {

    private var fragListener : LoginFragment.OnFragmentInteractionListener? = null
    private var fragContext : Context? = null

    private val handler = Handler()

    private lateinit var sharedPrefs : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPrefs = activity!!.getPreferences(Context.MODE_PRIVATE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as MainActivity?)?.showShakaText()
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handler.post(object : Runnable {
            override fun run() {
                if (activity != null) {
                    initNearbyTask()
                    initCreateBtn(view)
                    initJoinBtn(view)
                } else {
                    handler.postDelayed(this, 100)
                }
            }
        })

        view.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                clearFocusEditText(event)
            }
            true
        }
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacksAndMessages(null)
    }


    private fun initCreateBtn(view : View) {
        view.findViewById<Button>(R.id.partyCreateBtn).setOnClickListener {
            PartyUtils.Companion.CreatePartyTask((activity as MainActivity).rQueue, MainActivity.user, object : VolleyCallback<Party?> {
                override fun onSuccess(result: Party?) {
                    // Switch fragments to party
                    (activity as MainActivity).joinedParty(result!!)
                }

                override fun onError(error: VolleyError) {
                    if (error !is TimeoutError)
                        Log.e ("LoginFrag", String(error.networkResponse.data, charset("UTF-8")))
                    else
                        Log.e("LoginFrag", "TimeoutError")
                }
            }).execute()
        }
    }

    private fun initJoinBtn(view : View) {
        val editText = view.findViewById<EditText>(R.id.partyJoinBtn)

        editText.filters = editText.filters.plus(InputFilter InFilter@ { seq, _, _, _, _, _ ->
            if (seq == "") {
                return@InFilter seq
            }
            if (seq.matches(Regex( "[a-zA-Z0-9]+") )) {
                return@InFilter seq.toString().toUpperCase()
            }
            return@InFilter ""
        })

        editText.setOnFocusChangeListener( {_, b ->
            if (b) {
                editText.hint = ""
                editText.isSelected = true
            } else {
                editText.hint = "Join Party"
                editText.isSelected = false
            }
        })

        editText.setOnEditorActionListener { _ , actionID, _ ->
            if (actionID == EditorInfo.IME_ACTION_GO) {
                val errMessage = view.findViewById<TextView>(R.id.partyErrorMessage)

                if (editText.text.length == 6) {
                    PartyUtils.Companion.JoinPartyTask((activity as MainActivity).rQueue, MainActivity.user, editText!!.text.toString(), object : VolleyCallback<Party?> {
                        override fun onSuccess(result: Party?) {
                            (activity as MainActivity).joinedParty(result!!)
                        }

                        override fun onError(error: VolleyError) {
                            if (error !is TimeoutError)
                                Log.e ("Users", String(error.networkResponse.data, charset("UTF-8")))
                            else
                                Log.e("Users", "TimeoutError")
                        }
                    }).execute()
                } else {
                    errMessage.text = "Party ID must be 6 characters."
                    errMessage.animate().alpha(1f).setDuration(500).withEndAction({
                        Thread({
                            Thread.sleep(2000)
                            handler.post {
                                errMessage.animate().alpha(0f).setDuration(750).start()
                            }
                        }).start()
                    }).start()
                }
                return@setOnEditorActionListener true
            } else {
                return@setOnEditorActionListener false
            }
        }
    }

    private fun initNearbyTask() {

        var updatePB = true
        val delay = 3000L // millisecond

        val r = object : Runnable {
            override fun run() {
                if (activity != null) {
                    UpdateNearbyTask(this@LoginFragment, updatePB).execute(context, view, (activity as MainActivity).rQueue)
                }
                updatePB = false
                handler.postDelayed(this, delay)
            }
        }

        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            handler.post(r)
        } else {
            ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),0)
            if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                handler.post(r)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is LoginFragment.OnFragmentInteractionListener) {
            fragListener = context
            fragContext = context
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

    fun onButtonPressed(uri: Uri) {
        if (fragListener != null) {
            fragListener!!.onFragmentInteraction(uri)
        }
    }

    /* Spotify Implemented Methods */
    override fun onPlaybackError(p0: Error?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPlaybackEvent(p0: PlayerEvent?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    private fun clearFocusEditText(event : MotionEvent) {
        val v = activity?.currentFocus
        if (v is EditText) {
            val outRect = Rect()
            v.getGlobalVisibleRect(outRect)
            if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                v.clearFocus()
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
            }
        }
    }

    companion object {
        private val TAG = "LoginFrag"

        fun newInstance (): LoginFragment {
            val fragment = LoginFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }

        fun sortNearby(parties : Array<NearbyPartyView?>) : Array<NearbyPartyView?> {
            parties.sortWith(compareBy({ it?.distance }, { it?.party!!.name }))
            return parties
        }
    }
}