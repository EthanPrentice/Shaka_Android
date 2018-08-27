package ethanprentice.com.partyplaylist.frags

import android.support.v4.app.Fragment
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.volley.RequestQueue
import ethanprentice.com.partyplaylist.R

/**
 * Created by Ethan on 2018-07-21.
 */


class LibraryFragment : Fragment() {

    private var fragListener: LibraryFragment.OnFragmentInteractionListener? = null

    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_library, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacksAndMessages(null)
    }

    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }


    companion object {
        private val TAG = "LibraryFrag"
        lateinit var rQueue : RequestQueue

        fun newInstance (): LibraryFragment {
            val fragment = LibraryFragment()
            val args = Bundle()

            fragment.arguments = args
            return fragment
        }
    }

}