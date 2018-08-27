package ethanprentice.com.partyplaylist

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import ethanprentice.com.partyplaylist.adt.PlayerData
import ethanprentice.com.partyplaylist.adt.Track
import ethanprentice.com.partyplaylist.adt.UserType


class PlayerService : Service() {

    private val pBinder = PlayerBinder()

    lateinit var player : ShakaPlayer
    lateinit var rQueue : RequestQueue

    val handler = Handler()

    lateinit var lbm : LocalBroadcastManager

    inner class PlayerBinder : Binder() {
        internal val service: PlayerService
            get() = this@PlayerService
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        rQueue = Volley.newRequestQueue(this)
        lbm = LocalBroadcastManager.getInstance(this)
        player = ShakaPlayer(this, MainActivity.user, rQueue)

        startUpdating()

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(i : Intent?) : IBinder {
        return pBinder
    }


    private fun startUpdating() {
        // TODO : Convert this to a class w/ callback so it runs 1000ms after success / fail
        handler.post(object : Runnable {
            override fun run() {
                try {
                    player.updateData(rQueue, lbm)
                } catch (e : Exception) {
                    Log.e(TAG, e.message + "")
                }
                handler.postDelayed(this, 1000)
            }
        })
    }

    fun leftParty() {
      player.data = PlayerData()
      MainActivity.party = null
      player.data.tracks?.clear()

      if (MainActivity.user.type == UserType.PLAYER) {
          player.pause()
      }
    }

    fun getCurrTrack() : Track? {
        return player.getCurrTrack()
    }



    companion object {

        val TAG = "PlayerService"

        const val ACTION_PLAY = "ethanprentice.com.partyplaylist.action.PLAY"
        const val ACTION_PAUSE = "ethanprentice.com.partyplaylist.action.PAUSE"
        const val ACTION_TRACK_CHANGE = "ethanprentice.com.partyplaylist.action.TRACK_CHANGE"
    }
}
