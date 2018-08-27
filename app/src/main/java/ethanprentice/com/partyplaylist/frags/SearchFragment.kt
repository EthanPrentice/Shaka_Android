package ethanprentice.com.partyplaylist.frags

import android.content.Context
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.widget.SearchView
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import ethanprentice.com.partyplaylist.MainActivity
import ethanprentice.com.partyplaylist.R
import ethanprentice.com.partyplaylist.adt.SearchResult
import ethanprentice.com.partyplaylist.adt.Track
import ethanprentice.com.partyplaylist.adt.User
import ethanprentice.com.partyplaylist.adt.VolleyCallback
import ethanprentice.com.partyplaylist.utils.SearchUtils
import ethanprentice.com.partyplaylist.views.TrackView

class SearchFragment : Fragment() {

    private var fragListener: OnFragmentInteractionListener? = null

    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as MainActivity?)?.showSearchBar()

        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clearFocusSearchView()

        if (prevSearchResult?.tracks != null) {
            val resultsLayout = view.findViewById<LinearLayout>(R.id.searchResultsLL)
            resultsLayout?.removeAllViews()
            for (track in prevSearchResult?.tracks!!.items) {
                addTrackView(track, resultsLayout)
            }
        }

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
        fun onFragmentInteraction(uri: Uri)
    }

    fun addTrackView(track : Track, layout : LinearLayout) {
        val trackView = TrackView(context!!, (activity as MainActivity).rQueue, track)
        trackView.setOnClickListener {
            // TODO: Change to update server instead of local
//            MainActivity.getTracks()?.clear()
//            MainActivity.getTracks()?.add(track)
//            MainActivity.player.data.currTrack = 0
//            (activity as MainActivity).getPlayer().playUri(track.uri, 0, 0)
        }
        layout.addView( trackView )
    }

    fun performSearch(rQueue : RequestQueue, user : User, query : String) {
        SearchUtils.performSearch(rQueue, user, query, callback = object : VolleyCallback<SearchResult> {
            override fun onSuccess(result: SearchResult) {
                prevSearchResult = result

                val resultsLayout = view?.findViewById<LinearLayout>(R.id.searchResultsLL)!!
                resultsLayout.removeAllViews()
                for (track in result.tracks.items) {
                    addTrackView(track, resultsLayout)
                }
            }

            override fun onError(error: VolleyError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
    }


    companion object {
        private val TAG = "SearchFrag"
        private var prevSearchResult : SearchResult? = null

        fun newInstance (): SearchFragment {
            val fragment = SearchFragment()
            val args = Bundle()

            fragment.arguments = args
            return fragment
        }
    }
}