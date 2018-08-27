package ethanprentice.com.partyplaylist.views

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.view.View
import android.widget.ImageView
import com.android.volley.RequestQueue
import ethanprentice.com.partyplaylist.R
import ethanprentice.com.partyplaylist.adt.Track
import kotlin.concurrent.thread

class ImagedTrackView(context : Context, rQueue : RequestQueue, track : Track) : TrackView(context, rQueue, track) {

    private var aArtView : ImageView
    private var dragView : ImageView

    init {

        dragView = getDragView()
        aArtView = getAlbumArtView()

        super.addView(dragView)
        super.addView(aArtView)

        val cSet = ConstraintSet()
        cSet.clone(this)
        cSet.clear(nameView.id, ConstraintSet.START)
        cSet.applyTo(this)

        val clp = nameView.layoutParams as ConstraintLayout.LayoutParams
        clp.startToEnd = aArtView.id
        clp.marginStart = toDP(10)
        nameView.layoutParams = clp
    }

    private fun getDragView() : ImageView {
        val dragView = ImageView(context)
        val clp = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.MATCH_PARENT)

        clp.startToStart = super.getId()
        clp.topToTop = super.getId()
        clp.bottomToBottom = super.getId()
        clp.marginStart = toDP(15)
        dragView.layoutParams = clp

        dragView.adjustViewBounds = true
        dragView.scaleType = ImageView.ScaleType.FIT_XY

        dragView.id = View.generateViewId()

        val drawable = context.resources.getDrawable(R.drawable.ic_drag_handle, null)
        drawable.setTint(resources.getColor(R.color.colorAccent, null))
        dragView.setImageDrawable(drawable)

        dragView.background = null

        return dragView
    }

    private fun getAlbumArtView() : ImageView {
        val aArtView = ImageView(context)
        val clp = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.MATCH_PARENT)

        clp.startToEnd = dragView.id
        clp.topToTop = super.getId()
        clp.bottomToBottom = super.getId()
        clp.marginStart = toDP(10)
        aArtView.layoutParams = clp

        aArtView.setPadding(0, toDP(5), 0, toDP(5))

        aArtView.adjustViewBounds = true
        aArtView.scaleType = ImageView.ScaleType.FIT_XY

        aArtView.id = View.generateViewId()
        thread(start=true) {
            val drawable = track.getAlbumArtSmall(context.cacheDir, context.resources)
            Handler(Looper.getMainLooper()).post({
                aArtView.setImageDrawable(drawable)
            })
        }

        aArtView.background = null

        return aArtView
    }

}