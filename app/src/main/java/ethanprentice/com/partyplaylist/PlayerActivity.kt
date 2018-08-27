package ethanprentice.com.partyplaylist

import android.app.ActivityManager
import android.app.Service
import android.content.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.Fragment
import android.support.v4.content.LocalBroadcastManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import ethanprentice.com.partyplaylist.adt.PlayerStatus
import ethanprentice.com.partyplaylist.frags.LoginFragment
import ethanprentice.com.partyplaylist.frags.PlayerFragment
import ethanprentice.com.partyplaylist.frags.QueueFragment
import ethanprentice.com.partyplaylist.utils.PlayerUtils
import kotlin.concurrent.thread


class PlayerActivity : AppCompatActivity() {

    lateinit var rQueue : RequestQueue

    var pService: PlayerService? = null
    var pBound = false

    /* Defines callbacks for service binding, passed to bindService() */
    private val sConnection = object : ServiceConnection {
        override fun onServiceConnected(className : ComponentName, service : IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as PlayerService.PlayerBinder
            pService = binder.service
            pBound = true

            // Update pb state in fragment
            val currFrag = supportFragmentManager.findFragmentByTag("SHAKA")
            if (currFrag is PlayerFragment) {
                currFrag.updatePlaybackStatus()
            }
        }

        override fun onServiceDisconnected(arg0 : ComponentName) {
            pBound = false
        }
    }

    private var pReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, i : Intent) {
            when (i.action) {
                PlayerService.ACTION_PLAY -> {
                    findViewById<ImageView>(R.id.maximizedTogglePB)?.setImageDrawable(resources.getDrawable(R.drawable.ic_pause_outline, null))
                }
                PlayerService.ACTION_PAUSE -> {
                    findViewById<ImageView>(R.id.maximizedTogglePB)?.setImageDrawable(resources.getDrawable(R.drawable.ic_play_outline, null))
                }
                PlayerService.ACTION_TRACK_CHANGE -> {
                    updateCurrTrack()
                }

                else -> {

                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        rQueue = Volley.newRequestQueue(this)

        if (!isServiceRunning(PlayerService::class.java)) {
            startService(Intent(this, PlayerService::class.java))
        }

        openFragment(PlayerFragment.newInstance())

    }

    override fun onStart() {
        super.onStart()

        val iFilter = IntentFilter(PlayerService.ACTION_PLAY)
        iFilter.addAction(PlayerService.ACTION_PAUSE)
        iFilter.addAction(PlayerService.ACTION_TRACK_CHANGE)
        LocalBroadcastManager.getInstance(this).registerReceiver(pReceiver, iFilter)
    }

    override fun onResume() {
        super.onResume()

        // Bind to LocalService
        val intent = Intent(this, PlayerService::class.java)
        bindService(intent, sConnection, Context.BIND_AUTO_CREATE)

        updateCurrTrack()
    }

    override fun onStop() {
        super.onStop()
        unbindService(sConnection)
        pBound = false
    }

    override fun onBackPressed() {
        if (supportFragmentManager.findFragmentByTag("SHAKA") !is PlayerFragment) {
            openFragment(PlayerFragment.newInstance())
        } else {
            super.onBackPressed()
        }
    }

    fun handleIntent() {

    }

    fun openQueue(view : View) {
        openFragment(QueueFragment.newInstance())
    }

    private fun openFragment(fragment: Fragment) {
        val fm = supportFragmentManager
        val transaction = fm.beginTransaction()

        // If keyboard open close on fragment change
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)

        if (fm.findFragmentByTag("SHAKA") != null) {
            if (fragment is QueueFragment) {
                transaction.setCustomAnimations(R.anim.slide_down, R.anim.frag_out)
            }
            else {
                transaction.setCustomAnimations(R.anim.frag_in, R.anim.slide_up)
            }
        }

        transaction.replace(R.id.fragContainerPlayer, fragment, "SHAKA")
        transaction.commit()
    }

    fun togglePlayback(view : View) {
        if (pService?.player?.data?.status == PlayerStatus.PLAYING) {
            PlayerUtils.Companion.UpdateStatusTask(rQueue, MainActivity.party!!, MainActivity.user, PlayerStatus.PAUSED).execute()
        } else {
            PlayerUtils.Companion.UpdateStatusTask(rQueue, MainActivity.party!!, MainActivity.user, PlayerStatus.PLAYING).execute()
        }
    }

    private fun updateCurrTrack() {
        thread(start = true) {
            while (pService?.player?.getCurrTrack() == null) {
                Thread.sleep(100)
            }
            val albumArtSmall = pService?.player?.getCurrTrack()?.getAlbumArtSmall(cacheDir, resources)
            val albumArtLarge = pService?.player?.getCurrTrack()?.getAlbumArtLarge(cacheDir, resources)
            runOnUiThread {
                findViewById<ImageView>(R.id.maximizedHeaderAlbumArt).setImageDrawable(albumArtSmall)
                findViewById<ImageView>(R.id.maximizedMainAlbumArt).setImageDrawable(albumArtLarge)

                findViewById<TextView>(R.id.maximizedTrackTitle).text = pService?.player?.getCurrTrack()?.name
                findViewById<TextView>(R.id.maximizedArtistName).text = pService?.player?.getCurrTrack()?.artistName
            }
        }
    }

    fun skipTrack(view : View) {
        pService?.player?.skipTrack()
    }

    fun prevTrack(view : View) {
        pService?.player?.prevTrack()
    }


    private fun <T : Service> isServiceRunning(serviceClass : Class<T>) : Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}
