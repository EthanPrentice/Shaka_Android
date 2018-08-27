package ethanprentice.com.partyplaylist.views

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.support.constraint.ConstraintLayout
import android.util.TypedValue
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.LinearLayout
import android.widget.TextView
import de.hdodenhof.circleimageview.CircleImageView
import ethanprentice.com.partyplaylist.R
import ethanprentice.com.partyplaylist.adt.Party



/**
 * Created by Ethan on 2018-07-18.
 */


public class NearbyPartyView(context : Context, val party : Party, lat : Double, lng : Double) : ConstraintLayout(context) {


    private var iconView: CircleImageView
    private var nameView : TextView
    private var charView : TextView
    private var distView : TextView

    public var distance : Float

    init {
        super.setId(View.generateViewId())
        super.setBackgroundColor(resources.getColor(R.color.colorPrimary, null))
        super.setPadding(toDP(15), toDP(2), toDP(15), toDP(0))
        super.setClipToPadding(false)
        super.setClickable(true)
        super.setFocusable(true)

        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, toDP(70))
        this.layoutParams = params

        iconView = getIconView()
        nameView = getNameView()

        charView = getCharView()

        distance = getDistance(lat, lng)
        distView = getDistView(distance)

        addView(iconView)
        addView(nameView)
        addView(charView)
        addView(distView)

    }

    private fun getIconView() : CircleImageView {
        val ownerView = CircleImageView(context)
        val clp = ConstraintLayout.LayoutParams(toDP(45), toDP(45))

        clp.startToStart = super.getId()
        clp.bottomToBottom = super.getId()
        clp.topToTop = super.getId()
        clp.marginStart = toDP(15)

        ownerView.layoutParams = clp
        ownerView.borderColor = resources.getColor(R.color.colorAccent, null)
        ownerView.borderWidth = toDP(1)
        ownerView.background = resources.getDrawable(R.drawable.circle, null)

        ownerView.outlineProvider = ViewOutlineProvider.BACKGROUND
        ownerView.elevation = toDP(5).toFloat()

        val drawable = ColorDrawable(getRandomColor())
        ownerView.setImageDrawable(drawable)

        ownerView.id = View.generateViewId()

        ownerView.isClickable = false
        ownerView.isFocusable = false

        return ownerView
    }

    private fun getNameView() : TextView {
        val nameView = TextView(context)
        val clp = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)

        clp.startToEnd = iconView.id
        clp.bottomToBottom = super.getId()
        clp.topToTop = super.getId()
        clp.marginStart = toDP(20)

        nameView.layoutParams = clp

        nameView.text = party.name
        nameView.setTypeface(nameView.typeface, Typeface.BOLD)
        nameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        nameView.setTextColor(resources.getColor(R.color.colorAccent, null))

        nameView.setShadowLayer(30f, 1f, 1f, resources.getColor(R.color.colorAccent35, null))
        nameView.id = View.generateViewId()

        nameView.isClickable = false
        nameView.isFocusable = false

        return nameView
    }

    private fun getCharView() : TextView {
        val charView = TextView(context)
        val clp = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)

        clp.startToStart = iconView.id
        clp.endToEnd = iconView.id
        clp.bottomToBottom = iconView.id
        clp.topToTop = iconView.id

        charView.layoutParams = clp

        charView.text = party.name!![0].toString()
        charView.setTypeface(charView.typeface, Typeface.BOLD)
        charView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25f)
        charView.setTextColor(resources.getColor(R.color.lightAccent85, null))

        charView.elevation = toDP(6).toFloat()

        charView.id = View.generateViewId()

        charView.isClickable = false
        charView.isFocusable = false

        return charView
    }

    private fun getDistView(distance : Float) : TextView {
        val distView = TextView(context)
        val clp = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)

        clp.endToEnd = super.getId()
        clp.topToTop = super.getId()
        clp.bottomToBottom = super.getId()
        clp.marginEnd = toDP(15)

        distView.layoutParams = clp

        val distMeters = distance.toInt().toString() + "m"
        distView.text = distMeters

        distView.setTypeface(distView.typeface, Typeface.BOLD)
        distView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        distView.setTextColor(resources.getColor(R.color.colorAccent50, null))

        distView.elevation = toDP(6).toFloat()

        distView.id = View.generateViewId()

        distView.isClickable = false
        distView.isFocusable = false

        return distView
    }

    private fun getDistance(lat : Double, lng : Double) : Float {
        // Check if the user accepted location permission
        val earthRadius = 6371000.0
        val dLat = Math.toRadians(lat - party.latitude)
        val dLng = Math.toRadians(lng - party.longitude)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(party.latitude)) * Math.cos(Math.toRadians(lat)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return (earthRadius * c).toFloat()
    }

    private fun toDP(dp : Int) : Int {
        val dpMod = context.resources.displayMetrics.density
        return (dp * dpMod).toInt()
    }

    private fun getRandomColor(): Int {
        val colorArr = intArrayOf(
                R.color.userIconBlue,
                R.color.userIconPurple,
                R.color.userIconRed,
                R.color.userIconDarkBlue,
                R.color.userIconOrange
        )
        val colorInt = colorArr[Math.abs(party.hashCode()) % colorArr.size]
        return resources.getColor(colorInt, null)
    }
}