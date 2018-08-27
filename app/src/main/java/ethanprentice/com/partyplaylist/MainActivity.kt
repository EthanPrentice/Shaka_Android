package ethanprentice.com.partyplaylist

import android.app.ActivityManager
import android.app.SearchManager
import android.app.Service
import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.support.constraint.ConstraintLayout
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import de.hdodenhof.circleimageview.CircleImageView
import ethanprentice.com.partyplaylist.adt.*
import ethanprentice.com.partyplaylist.frags.*
import ethanprentice.com.partyplaylist.utils.LocationUtils
import ethanprentice.com.partyplaylist.utils.PlayerUtils
import ethanprentice.com.partyplaylist.utils.QueueUtils
import kotlinx.android.synthetic.main.activity_main.*
import org.codehaus.jackson.map.ObjectMapper
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity(),
        PartyFragment.OnFragmentInteractionListener, LoginFragment.OnFragmentInteractionListener, LibraryFragment.OnFragmentInteractionListener,
        BrowseFragment.OnFragmentInteractionListener, MinimizedPlayerFragment.OnFragmentInteractionListener {


    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener NISL@{ item ->
        when (item.itemId) {
            R.id.navigation_search -> {
                openFragment(SearchFragment.newInstance())
                return@NISL true
            }

            R.id.navigation_party -> {
                if (party?.ID == null) {
                    openFragment(LoginFragment.newInstance())
                } else {
                    openFragment(PartyFragment.newInstance())
                }
                return@NISL true
            }

            R.id.navigation_library -> {
                openFragment(LibraryFragment.newInstance())
                return@NISL true
            }
        }
        false
    }

    var pService: PlayerService? = null
    var pBound = false

    /* Defines callbacks for service binding, passed to bindService() */
    private val sConnection = object : ServiceConnection {

        override fun onServiceConnected(className : ComponentName, service : IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as PlayerService.PlayerBinder
            pService = binder.service
            pBound = true
        }

        override fun onServiceDisconnected(arg0 : ComponentName ) {
            pBound = false
        }
    }

    private var pReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, i : Intent) {
            Log.d("BR", i.action)
            when( i.action ) {
                PlayerService.ACTION_PLAY -> {
                    findViewById<ImageView>(R.id.minimizedTogglePB).setImageDrawable(resources.getDrawable(R.drawable.ic_pause_outline, null))
                }
                PlayerService.ACTION_PAUSE -> {
                    findViewById<ImageView>(R.id.minimizedTogglePB).setImageDrawable(resources.getDrawable(R.drawable.ic_play_outline, null))
                }
                PlayerService.ACTION_TRACK_CHANGE -> {
                    updatePlayerUI()
                }

                else -> {

                }
            }
        }
    }

    lateinit var rQueue: RequestQueue
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.topNav))

        rQueue = Volley.newRequestQueue(this)
        user = ObjectMapper().readValue<User>(intent.getStringExtra("User"), User::class.java)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        findViewById<BottomNavigationView>(R.id.navigation).selectedItemId = R.id.navigation_party
        disableNavBtns()
        openFragment(LoginFragment.newInstance())

        findViewById<TextView>(R.id.mainPartyName)?.text = MainActivity.party?.name
        findViewById<TextView>(R.id.mainPartyID)?.text = MainActivity.party?.ID

        findViewById<ConstraintLayout>(R.id.minimizedPlayer).setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java)
            startActivity(intent)
        }

        Thread({
            if (user.images.isNotEmpty()) {
                val drawable = downloadUserDrawable(user.images[0].url)
                runOnUiThread {
                    findViewById<CircleImageView>(R.id.mainHeaderIcon).setImageDrawable(drawable)
                }
            }
        }).start()

        // Instantiate search view
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = findViewById<SearchView>(R.id.searchBar)
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))

        if (!isServiceRunning(PlayerService::class.java)) {
            startService(Intent(this, PlayerService::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        LocationUtils.requestLocation(this, rQueue, user)

        // Bind to LocalService
        val intent = Intent(this, PlayerService::class.java)
        bindService(intent, sConnection, Context.BIND_AUTO_CREATE)

        val iFilter = IntentFilter(PlayerService.ACTION_PLAY)
        iFilter.addAction(PlayerService.ACTION_PAUSE)
        iFilter.addAction(PlayerService.ACTION_TRACK_CHANGE)
        LocalBroadcastManager.getInstance(this).registerReceiver(pReceiver, iFilter)
    }

    override fun onResume() {
        super.onResume()
        updatePlayerUI()
    }

    override fun onStop() {
        super.onStop()
        Handler(mainLooper).removeCallbacksAndMessages(null)
        handler.removeCallbacksAndMessages(null)
        LocationUtils.endLocation()

        unbindService(sConnection)
        pBound = false
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        handleIntent(intent)
    }

    override fun onBackPressed() {
        val botNav = findViewById<BottomNavigationView>(R.id.navigation)

        if (botNav.selectedItemId != R.id.navigation_party && isInParty()) {
            botNav.selectedItemId = R.id.navigation_party

            openFragment(PartyFragment.newInstance())

        } else if (isInParty()) {
            // Sign out from current party
            Handler(mainLooper).removeCallbacksAndMessages(null)
            disableNavBtns()
            leftParty()

            openFragment(LoginFragment.newInstance())

        } else {
            super.onBackPressed()
        }
    }


    // Implemented from misc fragments
    override fun onFragmentInteraction(uri: Uri) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    private fun isInParty(): Boolean {
        return party?.ID != null
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        when (intent.action) {
            Intent.ACTION_SEARCH -> {
                val query = intent.getStringExtra(SearchManager.QUERY)
                Log.d("Search", query)

                // Pass this search to the fragment
                val frag = supportFragmentManager.findFragmentByTag("SHAKA")
                if (frag is SearchFragment) {
                    frag.performSearch(rQueue, user, query)
                }
            }
        }
    }

    private fun openFragment(fragment: Fragment) {
        val fm = supportFragmentManager
        val transaction = fm.beginTransaction()

        // If keyboard open close on fragment change
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)

        if (fm.findFragmentByTag("SHAKA") != null) {

            if (fm.findFragmentByTag("SHAKA") !is LoginFragment && fragment !is LoginFragment) {
                transaction.setCustomAnimations(R.anim.frag_in, R.anim.frag_out)
            }
            else if (fragment is LoginFragment) {
                findViewById<View>(R.id.minimizedPlayer).visibility = View.GONE
                transaction.setCustomAnimations(R.anim.slide_down, R.anim.frag_out)
            }
            else {
                findViewById<View>(R.id.minimizedPlayer).visibility = View.VISIBLE
                transaction.setCustomAnimations(R.anim.frag_in, R.anim.slide_up)
            }
        }

        transaction.replace(R.id.fragContainerMain, fragment, "SHAKA")
        transaction.commit()
    }


    @Throws(IOException::class)
    private fun downloadUserDrawable(url: String): Drawable {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connect()
        val input = connection.inputStream

        val bmp = BitmapFactory.decodeStream(input)

        val file = File(cacheDir,"${user.username}_icon")
        try {
            val fos = FileOutputStream(file)
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return BitmapDrawable(resources, bmp)
    }


    // Methods called from views / onClicks
    fun togglePlayback(view : View) {
        if (party != null) {
            if (pService?.player?.data?.status == PlayerStatus.PLAYING) {
                PlayerUtils.Companion.UpdateStatusTask(rQueue, party!!, user, PlayerStatus.PAUSED).execute()
            } else {
                PlayerUtils.Companion.UpdateStatusTask(rQueue, party!!, user, PlayerStatus.PLAYING).execute()
            }
        }
    }

    fun updatePlayerUI() {
        thread (start = true) {
            while (pService?.player?.getCurrTrack() == null) {
                Thread.sleep(100)
            }
            try {
                val albumArtSmall = pService?.player?.getCurrTrack()?.getAlbumArtSmall(cacheDir, resources)
                runOnUiThread {
                    if (findViewById<View>(R.id.minimizedPlayer) != null) {
                        findViewById<ImageView>(R.id.minimizedAlbumArt).setImageDrawable(albumArtSmall)

                        findViewById<TextView>(R.id.minimizedTrackTitle).text = pService?.getCurrTrack()?.name
                        findViewById<TextView>(R.id.minimizedArtistName).text = pService?.getCurrTrack()?.artistName
                    }
                }

            } catch (e : Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun enableNavBtns() {
        findViewById<BottomNavigationView>(R.id.navigation).visibility = View.VISIBLE
    }

    private fun disableNavBtns() {
        findViewById<BottomNavigationView>(R.id.navigation).visibility = View.GONE
    }

    fun showSearchBar() {
        findViewById<ImageView>(R.id.mainShakaText)?.visibility = View.GONE
        findViewById<TextView>(R.id.mainPartyName)?.visibility = View.GONE
        findViewById<CircleImageView>(R.id.mainHeaderIcon)?.visibility = View.GONE

        val searchBar = findViewById<SearchView>(R.id.searchBar)
        searchBar?.visibility = View.VISIBLE
        searchBar.isIconified = false
    }

    fun showPartyID() {
        findViewById<ImageView>(R.id.mainShakaText)?.visibility = View.GONE
        findViewById<SearchView>(R.id.searchBar)?.visibility = View.GONE

        findViewById<CircleImageView>(R.id.mainHeaderIcon)?.visibility = View.VISIBLE
        findViewById<TextView>(R.id.mainPartyName)?.visibility = View.VISIBLE
    }

    fun showShakaText() {
        findViewById<SearchView>(R.id.searchBar)?.visibility = View.GONE
        findViewById<CircleImageView>(R.id.mainHeaderIcon)?.visibility = View.GONE
        findViewById<TextView>(R.id.mainPartyName)?.visibility = View.GONE

        findViewById<ImageView>(R.id.mainShakaText)?.visibility = View.VISIBLE
    }

    fun joinedParty(party : Party) {
        MainActivity.party = party

        findViewById<TextView>(R.id.mainPartyName)?.text = MainActivity.party?.name
        findViewById<TextView>(R.id.mainPartyID)?.text = MainActivity.party?.ID

        enableNavBtns()
        openFragment(PartyFragment.newInstance())
    }

    private fun leftParty() {
        pService?.leftParty()
        findViewById<TextView>(R.id.mainPartyName)!!.text = ""
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

    companion object {
        lateinit var user : User
        var party : Party? = null

        const val TAG = "MainActivity"

        fun onMenuItemClick(rQueue : RequestQueue, track : Track, item : MenuItem) : Boolean {
            when (item.itemId) {
                R.id.track_action_play_now -> {
                    return true
                }
                R.id.track_action_play_next -> {
                    if (party != null) {
                        QueueUtils.playNext(rQueue, party!!.ID, user, track)
                    }
                    return true
                }
                R.id.track_action_add -> {
                    if (party != null) {
                        QueueUtils.addToQueue(rQueue, party!!.ID, user, track)
                    }
                    return true
                }
                else -> {
                    return false
                }
            }
        }

    }

}
