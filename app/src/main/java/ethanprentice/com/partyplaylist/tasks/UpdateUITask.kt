package ethanprentice.com.partyplaylist.tasks

import android.content.Context
import android.os.Handler
import android.support.annotation.CallSuper
import android.support.v4.app.Fragment
import android.view.View
import android.widget.ProgressBar
import com.android.volley.RequestQueue
import ethanprentice.com.partyplaylist.R

/**
 * Created by Ethan on 2018-07-20.
 */

public abstract class UpdateUITask(protected val frag : Fragment, private val updatePB : Boolean) {

    private var finished = false

    protected abstract fun preExec()

    abstract fun execute(context : Context?, view : View?, rQueue : RequestQueue)

    @CallSuper
    protected open fun postExec() {
        finished = true
    }


    private fun setProgressBarVisibility(visibility : Int) {
        if (frag.context?.mainLooper != null) {
            val mainHandler = Handler(frag.context?.mainLooper)
            mainHandler.post({
                frag.activity?.window?.decorView?.findViewById<ProgressBar>(R.id.mainProgressBar)?.visibility = visibility
            })
        }
    }

    protected fun hideProgressBar() {
        setProgressBarVisibility(View.GONE)
    }

    protected fun showProgressBar(delay : Long) {
        if (updatePB) {
            // If getting users is unfinished / unsuccessful after 250ms show the progress bar
            Thread({
                try {
                    Thread.sleep(delay)
                    if (!finished) {
                        setProgressBarVisibility(View.VISIBLE)
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }).start()
        }
    }

}