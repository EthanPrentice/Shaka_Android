package ethanprentice.com.partyplaylist

import android.content.Context
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.spotify.sdk.android.player.*
import ethanprentice.com.partyplaylist.adt.*
import ethanprentice.com.partyplaylist.requests.player.ChangeTrack
import ethanprentice.com.partyplaylist.spotifyAPI.Authentication
import ethanprentice.com.partyplaylist.utils.PlayerUtils

class ShakaPlayer(context : Context, user: User, val rQueue : RequestQueue) : Player.NotificationCallback {

    var data : PlayerData = PlayerData()
    lateinit var spotifyPlayer : SpotifyPlayer

    val defaultCB = object : Player.OperationCallback {
        override fun onSuccess() {
            print ("success!")
        }

        override fun onError(error: Error?) {
            print ("failure : " + error?.toString())
        }
    }

    init {
        initializeSpotify(context, user)
    }

    // Implemented from Player.NotificationCallback
    override fun onPlaybackEvent(playerEvent: PlayerEvent) {
        Log.d("MainActivity", "Playback event received: " + playerEvent.name)
        Log.i ("TESTER", playerEvent.toString() + "")
        when (playerEvent) {

            PlayerEvent.kSpPlaybackNotifyBecameActive -> {

            }

            PlayerEvent.kSpPlaybackNotifyBecameInactive -> {

            }

            PlayerEvent.kSpPlaybackNotifyAudioDeliveryDone -> {
                if (data.tracks.size > data.currTrack + 1) {
                   data.currTrack += 1
                } else {
                    data.currTrack = 0
                }
                if (MainActivity.party != null) {
                    PlayerUtils.Companion.UpdateTrackTask(rQueue, MainActivity.party!!, MainActivity.user, data.currTrack).execute()
                }
            }
            else -> {

            }
        }
    }

    override fun onPlaybackError(error: Error) {
        Log.d("MainActivity", "Playback error received: " + error.name)
        when (error) {
        // Handle error type as necessary
            Error.kSpErrorAlreadyInitialized -> {

            }
            else -> {

            }
        }
    }


    // Used to get data from the server
    fun updateData(rQueue: RequestQueue, lbm : LocalBroadcastManager) {
        synchronized(this, {
            updatePlayerData(rQueue, lbm)
        })
    }

    private fun updatePlayerData(rQueue : RequestQueue, lbm : LocalBroadcastManager) {
        if (MainActivity.party != null) {
            PlayerUtils.Companion.GetPlayerTask(rQueue, MainActivity.party!!, MainActivity.user, object : VolleyCallback<PlayerData?> {
                override fun onSuccess(result: PlayerData?) {
                    if (result != null) {

                        val playDiffTrack = data.currTrack != result.currTrack || spotifyPlayer.metadata.currentTrack == null || data.tracks[data.currTrack].uri != spotifyPlayer.metadata.currentTrack.uri
                        val statusChanged = data.status != result.status || (!spotifyPlayer.playbackState.isPlaying && result.status == PlayerStatus.PLAYING)

                        data = result

                        if (playDiffTrack) {
                            lbm.sendBroadcast( Intent(PlayerService.ACTION_TRACK_CHANGE) )
                            if (MainActivity.user.type == UserType.PLAYER) {
                                playTrackAt(result.currTrack)
                                if (data.status != PlayerStatus.PLAYING) {
                                    pause()
                                }
                            }
                        }

                        if (statusChanged) {
                            when (result.status) {
                                PlayerStatus.PLAYING -> {
                                    lbm.sendBroadcast( Intent(PlayerService.ACTION_PLAY) )
                                    if (MainActivity.user.type == UserType.PLAYER) {
                                        if (spotifyPlayer.metadata?.currentTrack != null) {
                                            resume()
                                        } else {
                                            playTrackAt(data.currTrack)
                                        }
                                    }
                                }
                                PlayerStatus.PAUSED -> {
                                    lbm.sendBroadcast( Intent(PlayerService.ACTION_PAUSE) )
                                    if (MainActivity.user.type == UserType.PLAYER) {
                                        pause()
                                    }
                                }
                                else -> {
                                    // Do nothing
                                }
                            }
                        }
                    } else {
                        Log.w("PlayerData", "Error getting spotifyPlayer data. Returned null.")
                    }
                }

                override fun onError(error: VolleyError) {
                    print ("HEY")
                }
            }).execute()
        }
    }


    // Spotify initialization / helpers
    private fun initializeSpotify(context : Context, user : User) : Boolean {
        val playerConfig = Config(context, user.token, Authentication.CLIENT_ID)
        var returnVal = false

        // Attempt to initialize spotifyPlayer
        Spotify.getPlayer(playerConfig, this, object : SpotifyPlayer.InitializationObserver {

            override fun onInitialized(spotifyPlayer: SpotifyPlayer) {
                this@ShakaPlayer.spotifyPlayer = spotifyPlayer
                this@ShakaPlayer.spotifyPlayer.addNotificationCallback(this@ShakaPlayer)
                Log.i(SplashActivity.TAG, "Initialized spotifyPlayer.")
                returnVal = true
            }

            override fun onError(throwable: Throwable) {
                Log.e(SplashActivity.TAG, "Could not initialize spotifyPlayer: " + throwable.message)
                returnVal = false
            }
        })

        return returnVal
    }

    fun getCurrTrack() : Track? {
        return if (data.tracks != null && data.tracks.size > data.currTrack) {
            data.tracks[data.currTrack]
        } else {
            null
        }
    }

    fun getCurrTrackNum() : Int {
        return data.currTrack
    }

    fun getStatus() : PlayerStatus {
        return data.status
    }

    // References to SpotifyPlayer methods
    fun playUri(uri : String, index : Int, positionInMs : Int) {
        spotifyPlayer.playUri(defaultCB, uri, index, positionInMs)
    }

    fun playTrackAt(index : Int) {
        playUri(data.tracks[index].uri, 0, 0)
    }

    fun pause() {
        spotifyPlayer.pause(null)
    }

    fun resume() {
        spotifyPlayer.resume(null)
    }

    fun changeTrack(trackNum : Int) {
        ChangeTrack(rQueue, trackNum).execute()
    }

    fun skipTrack() {
        if (data.tracks.size > data.currTrack + 1) {
            changeTrack(getCurrTrackNum() + 1)
        } else {
            changeTrack( 0 )
        }
    }

    fun prevTrack() {
        if (spotifyPlayer.playbackState.positionMs < 3000) {
            changeTrack(getCurrTrackNum() - 1)
        } else {
            playTrackAt( data.currTrack )
        }
    }

}