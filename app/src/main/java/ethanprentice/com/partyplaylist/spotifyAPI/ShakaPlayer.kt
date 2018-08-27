package ethanprentice.com.partyplaylist.spotifyAPI

import android.content.Context
import com.spotify.sdk.android.player.*
import ethanprentice.com.partyplaylist.MainActivity
import com.spotify.sdk.android.player.SpotifyPlayer
import com.spotify.sdk.android.player.Spotify
import ethanprentice.com.partyplaylist.adt.User
import ethanprentice.com.partyplaylist.spotifyAPI.Authentication.Companion.CLIENT_ID


/**
 * Created by Ethan on 2018-07-17.
 */
class ShakaPlayer {

    lateinit var player : SpotifyPlayer

    constructor(context : Context, user : User, playerIO : SpotifyPlayer.InitializationObserver) {

        val playerConfig = Config(context, user.token, CLIENT_ID)
        player = Spotify.getPlayer(playerConfig, this, playerIO)

    }
}