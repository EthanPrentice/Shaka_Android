package ethanprentice.com.partyplaylist.frags

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import ethanprentice.com.partyplaylist.MainActivity
import ethanprentice.com.partyplaylist.PlayerActivity
import ethanprentice.com.partyplaylist.R
import ethanprentice.com.partyplaylist.adt.PlayerStatus
import ethanprentice.com.partyplaylist.utils.PlayerUtils
import kotlin.concurrent.thread


class PlayerFragment : Fragment() {

    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updatePlaybackStatus()
    }

    override fun onResume() {
        super.onResume()
        updatePlaybackStatus()
    }

    fun updatePlaybackStatus() {
        val pService = (activity as PlayerActivity).pService
        thread(start = true) {
            if (pService?.player?.getStatus() != null) {
                Handler(Looper.getMainLooper()).post({
                    if (pService.player.getStatus() == PlayerStatus.PLAYING) {
                        view?.findViewById<ImageView>(R.id.maximizedTogglePB)?.setImageDrawable(resources.getDrawable(R.drawable.ic_pause_outline, null))
                    } else {
                        view?.findViewById<ImageView>(R.id.maximizedTogglePB)?.setImageDrawable(resources.getDrawable(R.drawable.ic_play_outline, null))
                    }
                })
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }


    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {

        fun newInstance(): PlayerFragment {
            val fragment = PlayerFragment()
            val args = Bundle()

            fragment.arguments = args
            return fragment
        }
    }
}
