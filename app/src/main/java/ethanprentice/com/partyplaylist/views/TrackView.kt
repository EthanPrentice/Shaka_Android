package ethanprentice.com.partyplaylist.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.PopupMenu
import android.text.TextUtils
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.android.volley.RequestQueue
import ethanprentice.com.partyplaylist.MainActivity
import ethanprentice.com.partyplaylist.R
import ethanprentice.com.partyplaylist.adt.Track
import ethanprentice.com.partyplaylist.utils.QueueUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Created by Ethan on 2018-07-22.
 */

open class TrackView(context : Context, val rQueue : RequestQueue, val track : Track) : ConstraintLayout(context) {

    internal var nameView : TextView
    private var descView : TextView
    private var overflowView : ImageButton

    init {
        super.setId(View.generateViewId())
        super.setBackgroundColor(resources.getColor(R.color.colorPrimary, null))
        super.setPadding(toDP(15), toDP(2), toDP(15), toDP(0))
        super.setClipToPadding(false)
        super.setClickable(true)
        super.setFocusable(true)

        super.setBackgroundResource(R.drawable.track_bg)

        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, toDP(70))
        this.layoutParams = params

        overflowView = getOverflowView()
        nameView = getNameView()
        descView = getDescView()

        this.addView(overflowView)
        this.addView(nameView)
        this.addView(descView)

    }


    private fun getOverflowView() : ImageButton {
        val overflowView = ImageButton(context)
        val clp = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)

        clp.endToEnd = super.getId()
        clp.topToTop = super.getId()
        clp.bottomToBottom = super.getId()

        clp.marginEnd = toDP(5)

        overflowView.layoutParams = clp

        overflowView.id = View.generateViewId()

        overflowView.setImageDrawable(resources.getDrawable(R.drawable.ic_overflow_vert, null))
        overflowView.background = null

        overflowView.setOnClickListener {
            showTracksPopup(this)
        }

        return overflowView
    }

    private fun getNameView() : TextView {
        val nameView = TextView(context)
        val clp = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.WRAP_CONTENT)

        clp.startToStart = super.getId()
        clp.endToEnd = overflowView.id
        clp.horizontalBias = 0f
        clp.topToTop = super.getId()

        clp.marginStart = toDP(20)
        clp.marginEnd = toDP(20)
        clp.topMargin = toDP(4)

        nameView.setPadding(0,0, toDP(20), 0)

        nameView.layoutParams = clp

        nameView.text = track.name
        nameView.maxLines = 1
        nameView.ellipsize = TextUtils.TruncateAt.END

        nameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        nameView.setTextColor(resources.getColor(R.color.colorAccent, null))

        nameView.setShadowLayer(4f, 1f, 1f, resources.getColor(R.color.colorAccent35, null))
        nameView.id = View.generateViewId()

        nameView.isClickable = false
        nameView.isFocusable = false

        return nameView
    }

    private fun getDescView() : TextView {
        val descView = TextView(context)
        val clp = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.WRAP_CONTENT)

        clp.startToStart = nameView.id
        clp.endToEnd = nameView.id
        clp.horizontalBias = 0f
        clp.topToBottom = nameView.id

        clp.marginEnd = toDP(10)

        descView.setPadding(0,0, toDP(30), 0)

        descView.layoutParams = clp

        val desc = track.artistName + " • " + track.albumName
        descView.text = desc
        descView.maxLines = 1
        descView.ellipsize = TextUtils.TruncateAt.END

        descView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        descView.setTextColor(resources.getColor(R.color.colorAccent50, null))

        descView.elevation = toDP(6).toFloat()

        descView.id = View.generateViewId()

        descView.isClickable = false
        descView.isFocusable = false

        return descView
    }

    private fun showTracksPopup(view : View) {
        val popup = PopupMenu(context, view)
        val inflater = popup.menuInflater
        popup.setOnMenuItemClickListener({
            MainActivity.onMenuItemClick( rQueue, track, it )
        })
        inflater.inflate(R.menu.track_actions, popup.menu)
        popup.show()
    }

    protected fun toDP(dp : Int) : Int {
        val dpMod = context.resources.displayMetrics.density
        return (dp * dpMod).toInt()
    }

}