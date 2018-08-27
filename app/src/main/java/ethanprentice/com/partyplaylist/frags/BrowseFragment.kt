package ethanprentice.com.partyplaylist.frags

import android.content.Context
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import com.android.volley.TimeoutError
import com.android.volley.VolleyError
import ethanprentice.com.partyplaylist.MainActivity
import ethanprentice.com.partyplaylist.R
import ethanprentice.com.partyplaylist.adt.Track
import ethanprentice.com.partyplaylist.adt.VolleyCallback
import ethanprentice.com.partyplaylist.utils.QueueUtils

/**
 * Created by Ethan on 2018-07-21.
 */
class BrowseFragment : Fragment() {

    private var fragListener: BrowseFragment.OnFragmentInteractionListener? = null

    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as MainActivity?)?.showSearchBar()
        return inflater.inflate(R.layout.fragment_browse, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clearFocusSearchView()

        Log.d ("", activity?.currentFocus?.javaClass?.canonicalName)

        // Unfocus search view on tap not in search view
        view.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                clearFocusSearchView(event)
            }
            true
        }
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacksAndMessages(null)
    }

    private fun clearFocusSearchView() {
        val v = activity?.currentFocus
        if (v is SearchView.SearchAutoComplete || v is SearchView) {
            v.clearFocus()
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

    private fun clearFocusSearchView(event : MotionEvent) {
        val v = activity?.currentFocus
        if (v is SearchView.SearchAutoComplete || v is SearchView) {
            val outRect = Rect()
            v.getGlobalVisibleRect(outRect)
            if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                v.clearFocus()
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
            }
        }
    }

    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }


    companion object {
        private val TAG = "BrowseFrag"

        fun newInstance (): BrowseFragment {
            val fragment = BrowseFragment()
            val args = Bundle()

            fragment.arguments = args
            return fragment
        }
    }
}