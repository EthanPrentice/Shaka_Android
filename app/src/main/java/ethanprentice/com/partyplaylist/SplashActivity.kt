/**
 * @author: Ethan Prentice
 *
 */

package ethanprentice.com.partyplaylist

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v7.app.AppCompatActivity
import android.transition.Slide
import android.util.Log
import android.view.*
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

import com.spotify.sdk.android.authentication.AuthenticationClient
import com.spotify.sdk.android.authentication.AuthenticationRequest
import com.spotify.sdk.android.authentication.AuthenticationResponse
import com.spotify.sdk.android.player.ConnectionStateCallback
import com.spotify.sdk.android.player.Error
import ethanprentice.com.partyplaylist.adt.VolleyCallback
import ethanprentice.com.partyplaylist.spotifyAPI.Authentication
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.android.volley.VolleyError
import ethanprentice.com.partyplaylist.adt.User
import org.apache.commons.io.FileUtils
import org.codehaus.jackson.map.ObjectMapper


class SplashActivity : AppCompatActivity(), ConnectionStateCallback {

    private lateinit var rQueue : RequestQueue
    private lateinit var sharedPrefs : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupWindowAnimations()
        setContentView(R.layout.activity_splash)

        FileUtils.deleteQuietly(cacheDir)

        // Initialize queue once context exists
        rQueue = Volley.newRequestQueue(this)
        sharedPrefs = getPreferences(Context.MODE_PRIVATE)

        // Show content behind navbar and status bar and offset the spotify logo
        setUIOptions()
        setPoweredBySpotifyMargin()

        login()
    }

    override fun dispatchTouchEvent(event : MotionEvent) : Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            clearFocusEditText(event)
        }
        return super.dispatchTouchEvent(event)
    }

    // Handle login from activity
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent) {
        super.onActivityResult(requestCode, resultCode, intent)

        val response = Authentication.onSignIn(rQueue, requestCode, resultCode, intent, object : VolleyCallback<User> {
            override fun onSuccess(result: User) {
                onSuccessfulLogin(result)
            }

            override fun onError(error: VolleyError) {
                // Do nothing
            }
        })

        if (response == null) {
            Log.e(TAG, "Error receiving authentication info.")
        }
    }

    override fun onNewIntent(intent : Intent) {
        super.onNewIntent(intent)

        // Handle login from browser intent
        val uri = intent.data
        if (uri != null) {
            val response = AuthenticationResponse.fromUri(uri)

            Log.d(TAG, uri.toString())

            when (response.type) {
                AuthenticationResponse.Type.CODE -> {
                    Log.d(TAG, "Authentication code recieved.")
                    Authentication.getAuthCredentials(rQueue, response.code, object : VolleyCallback<User> {
                        override fun onSuccess(result: User) {
                            onSuccessfulLogin(result)
                        }

                        override fun onError(error: VolleyError) {
                            // Do nothing
                        }
                    })
                }

                AuthenticationResponse.Type.ERROR -> {
                    Log.e(TAG, "Error processing intent to log user in.")
                }

                else -> {
                    Log.e(TAG, "Wrong authentication type.  Use CODE.")
                    // Do nothing
                }
            }
        }
    }

    override fun onLoggedIn() {
        Log.d(TAG, "User logged in")
    }

    override fun onLoggedOut() {
        Log.d(TAG, "User logged out")
    }

    override fun onLoginFailed(var1: Error) {
        Log.d(TAG, "Login failed")
        Log.e(TAG, "Login error: $var1")
    }

    override fun onTemporaryError() {
        Log.d(TAG, "Temporary error occurred")
    }

    override fun onConnectionMessage(message: String) {
        Log.d(TAG, "Received connection message: " + message)
    }

    private fun login() {
        val builder = AuthenticationRequest.Builder(Authentication.CLIENT_ID, AuthenticationResponse.Type.CODE, Authentication.REDIRECT_URI)
        builder.setScopes(arrayOf("user-read-private", "user-library-read", "streaming"))
        val request = builder.build()

        try {
            // SpotifyAuth does not save credentials when using activity on API 26+
            if (Build.VERSION.SDK_INT < 26 || sharedPrefs.getBoolean("credsStored", false)) {
                AuthenticationClient.openLoginActivity(this, Authentication.REQUEST_CODE, request)
            } else {
                AuthenticationClient.openLoginInBrowser(this, request)
            }
            Log.d(TAG, "Sending info to ${Authentication.REDIRECT_URI}")
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Could not open LoginActivity: " + e.localizedMessage)
        }
    }

    private fun onSuccessfulLogin(user: User) {
        Authentication.authenticated = true
        with (sharedPrefs.edit()) {
            putBoolean("credsStored", true)
            apply()
        }

        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("User", ObjectMapper().writeValueAsString(user))
        startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle())
    }

    // Adds the navbar height to the bottom margin of PoweredBySpotify
    private fun setPoweredBySpotifyMargin() {
        val resources = resources
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0) {
            val navBarHeight = resources.getDimensionPixelSize(resourceId)
            val imageView = findViewById<ImageView>(R.id.splashPoweredBySpotify)
            val mlp = imageView.layoutParams as ViewGroup.MarginLayoutParams

            mlp.setMargins(0, 0, 0, mlp.bottomMargin + navBarHeight)
        }
    }

    private fun setUIOptions() {
        Log.i(TAG, "Setting view options")
        val decorView = window.decorView
        val uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        // window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        decorView.systemUiVisibility = uiOptions
    }

    private fun clearFocusEditText(event : MotionEvent) {
        val v = currentFocus
        if (v is EditText) {
            val outRect = Rect()
            v.getGlobalVisibleRect(outRect)
            if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                v.clearFocus()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
            }
        }
    }


    private fun setupWindowAnimations() {
        with(window) {
            requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)

            val slide = Slide(Gravity.TOP)
            slide.duration = 2000
            slide.interpolator = FastOutSlowInInterpolator()
            exitTransition = slide
        }
    }



    companion object {
        val TAG = "SplashActivity"
    }
}