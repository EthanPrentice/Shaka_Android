package ethanprentice.com.partyplaylist.views

import android.content.Context
import android.graphics.Typeface
import android.support.constraint.ConstraintLayout
import android.support.v4.content.res.ResourcesCompat
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import ethanprentice.com.partyplaylist.R
import android.graphics.drawable.BitmapDrawable
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.util.Log
import android.view.ViewOutlineProvider
import de.hdodenhof.circleimageview.CircleImageView
import ethanprentice.com.partyplaylist.adt.User
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


/**
 * Created by Ethan on 2018-07-02.
 */

class UserView(context: Context, val user : User) : ConstraintLayout(context) {

    private var iconView: CircleImageView
    private var nameView : TextView
    private var crownView : ImageView
    private var charView : TextView

    private var useCharView = false

    init {
        super.setId(View.generateViewId())
        super.setBackground(resources.getDrawable(R.drawable.userview_shape, null))
        super.setPadding(toDP(15), toDP(2), toDP(15), toDP(0))
        super.setClipToPadding(false)
        super.setClipChildren(false)

        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, toDP(70))
//        params.topMargin = toDP(10)
//        params.marginStart = toDP(2)
//        params.marginEnd = toDP(2)
        this.layoutParams = params
        background = ResourcesCompat.getDrawable(resources, R.color.colorPrimary, null)

        Log.i("UV", "${user.username} : ${user.owner}")

        iconView = getIconView()
        nameView = getNameView()

        crownView = getCrownView()
        charView = getCharView()

        addView(crownView)
        addView(iconView)
        addView(nameView)

        if (useCharView) {
            addView(charView)
        }

    }

    private fun getCrownView() : ImageView {
        val crownView = ImageView(context)
        val clp = ConstraintLayout.LayoutParams(toDP(25), toDP(17))

        clp.endToEnd = super.getId()
        clp.bottomToBottom = super.getId()
        clp.topToTop = super.getId()
        clp.marginEnd = toDP(15)

        crownView.layoutParams = clp

        val drawable = resources.getDrawable(R.drawable.crown, null).mutate()
        if (user.owner) {
            drawable.setTint(resources.getColor(R.color.colorAccent, null))
        } else {
            drawable.setTint(resources.getColor(R.color.colorPrimaryDark, null))
        }

        crownView.setImageDrawable(drawable)
        crownView.id = View.generateViewId()

        return crownView
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

        if (user.images.isNotEmpty()) {
            context.cacheDir.listFiles().forEach { file -> Log.i("Files", file.name)}
            Log.i("PATH","${context.cacheDir}/${user.username}_icon")
            if (File(context.cacheDir, "${user.username}_icon").exists()) {
                try {
                    val bmp = BitmapFactory.decodeFile(File(context.cacheDir,"${user.username}_icon").absolutePath)
                    val drawable = BitmapDrawable(resources, bmp)

                    Log.i(user.displayName, "Setting from cache")

                    val mainHandler = Handler(context.mainLooper)
                    val r = Runnable {
                        ownerView.setImageDrawable(drawable)
                    }
                    mainHandler.post(r)

                } catch (e : Exception) {
                    e.printStackTrace()
                }
            } else {
                Thread({
                    Log.i(user.displayName, "Setting from URL")
                    // Get drawable then update the picture on the UI thread
                    val drawable = downloadUserDrawable(user.images[0].url)
                    val mainHandler = Handler(context.mainLooper)
                    val r = Runnable {
                        ownerView.setImageDrawable(drawable)
                    }
                    mainHandler.post(r)
                }).start()
            }
        } else {
            val drawable = ColorDrawable(getRandomColor())
            ownerView.setImageDrawable(drawable)
            useCharView = true
        }

        ownerView.id = View.generateViewId()

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

        nameView.text = user.displayName
        nameView.setTypeface(nameView.typeface, Typeface.BOLD)
        nameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        nameView.setTextColor(resources.getColor(R.color.colorAccent, null))

        nameView.setShadowLayer(30f, 1f, 1f, resources.getColor(R.color.colorAccent35, null))
        nameView.id = View.generateViewId()

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

        charView.text = user.displayName[0].toString()
        charView.setTypeface(charView.typeface, Typeface.BOLD)
        charView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25f)
        charView.setTextColor(resources.getColor(R.color.lightAccent85, null))

        charView.elevation = toDP(6).toFloat()

        charView.id = View.generateViewId()

        return charView
    }

    private fun getRandomColor(): Int {
        val colorArr = intArrayOf(
                R.color.userIconBlue,
                R.color.userIconPurple,
                R.color.userIconRed,
                R.color.userIconDarkBlue,
                R.color.userIconOrange
        )
        val colorInt = colorArr[Math.abs(user.hashCode()) % colorArr.size]
        return resources.getColor(colorInt, null)
    }

    @Throws(IOException::class)
    private fun downloadUserDrawable(url: String): Drawable {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connect()
        val input = connection.inputStream

        val bmp = BitmapFactory.decodeStream(input)

        val file = File(context.cacheDir,"${user.username}_icon")
        try {
            val fos = FileOutputStream(file)
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return BitmapDrawable(resources, bmp)
    }


    private fun toDP(dp : Int) : Int {
        val dpMod = context.resources.displayMetrics.density
        return (dp * dpMod).toInt()
    }

    companion object {
        private val TAG = "UserView"
    }
}
