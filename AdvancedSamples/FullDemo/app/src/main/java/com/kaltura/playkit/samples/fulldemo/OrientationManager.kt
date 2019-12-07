package com.kaltura.playkit.samples.fulldemo

import android.content.Context
import android.view.OrientationEventListener

class OrientationManager : OrientationEventListener {

    var screenOrientation: ScreenOrientation? = null
    private var listener: OrientationListener? = null

    enum class ScreenOrientation {
        REVERSED_LANDSCAPE, LANDSCAPE, PORTRAIT, REVERSED_PORTRAIT
    }

    constructor(context: Context, rate: Int, listener: OrientationListener) : super(context, rate) {
        setListener(listener)
    }

    constructor(context: Context, rate: Int) : super(context, rate) {}

    constructor(context: Context) : super(context) {}

    override fun onOrientationChanged(orientation: Int) {
        if (orientation == -1) {
            return
        }
        val newOrientation: ScreenOrientation
        if (orientation >= 60 && orientation <= 140) {
            newOrientation = ScreenOrientation.REVERSED_LANDSCAPE
        } else if (orientation >= 140 && orientation <= 220) {
            newOrientation = ScreenOrientation.REVERSED_PORTRAIT
        } else if (orientation >= 220 && orientation <= 300) {
            newOrientation = ScreenOrientation.LANDSCAPE
        } else {
            newOrientation = ScreenOrientation.PORTRAIT
        }
        if (newOrientation != screenOrientation) {
            screenOrientation = newOrientation
            if (listener != null) {
                listener!!.onOrientationChange(screenOrientation!!)
            }
        }
    }

    fun setListener(listener: OrientationListener) {
        this.listener = listener
    }

    interface OrientationListener {

        fun onOrientationChange(screenOrientation: ScreenOrientation)
    }
}
