package ethanprentice.com.partyplaylist.frags

import android.net.Uri
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ethanprentice.com.partyplaylist.MainActivity
import ethanprentice.com.partyplaylist.R

class MinimizedPlayerFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_minimized_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        private val TAG = "MinimizedPlayerFrag"

        fun newInstance (): MinimizedPlayerFragment {
            val fragment = MinimizedPlayerFragment()
            val args = Bundle()

            fragment.arguments = args
            return fragment
        }
    }

}