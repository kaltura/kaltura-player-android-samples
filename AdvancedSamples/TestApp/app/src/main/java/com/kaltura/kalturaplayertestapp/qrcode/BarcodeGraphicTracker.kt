package com.kaltura.kalturaplayertestapp.qrcode

import android.content.Context
import androidx.annotation.UiThread
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.barcode.Barcode
import com.kaltura.kalturaplayertestapp.camera.GraphicOverlay

/**
 * Generic tracker which is used for tracking or reading a barcode (and can really be used for
 * any type of item).  This is used to receive newly detected items, add a graphical representation
 * to an overlay, update the graphics as the item changes, and remove the graphics when the item
 * goes away.
 */
class BarcodeGraphicTracker internal constructor(private val mOverlay: GraphicOverlay<BarcodeGraphic>, private val mGraphic: BarcodeGraphic,
                                                 context: Context) : Tracker<Barcode>() {

    private var mBarcodeUpdateListener: BarcodeUpdateListener? = null

    /**
     * Consume the item instance detected from an Activity or Fragment level by implementing the
     * BarcodeUpdateListener interface method onBarcodeDetected.
     */
    interface BarcodeUpdateListener {
        @UiThread
        fun onBarcodeDetected(barcode: Barcode?)
    }

    init {
        if (context is BarcodeUpdateListener) {
            this.mBarcodeUpdateListener = context
        } else {
            throw RuntimeException("Hosting activity must implement BarcodeUpdateListener")
        }
    }

    /**
     * Start tracking the detected item instance within the item overlay.
     */
    override fun onNewItem(id: Int, item: Barcode?) {
        mGraphic.id = id
        mBarcodeUpdateListener!!.onBarcodeDetected(item)
    }

    /**
     * Update the position/characteristics of the item within the overlay.
     */
    override fun onUpdate(detectionResults: Detector.Detections<Barcode>?, item: Barcode?) {
        mOverlay.add(mGraphic)
        mGraphic.updateItem(item!!)
    }

    /**
     * Hide the graphic when the corresponding object was not detected.  This can happen for
     * intermediate frames temporarily, for example if the object was momentarily blocked from
     * view.
     */
    override fun onMissing(detectionResults: Detector.Detections<Barcode>?) {
        mOverlay.remove(mGraphic)
    }

    /**
     * Called when the item is assumed to be gone for good. Remove the graphic annotation from
     * the overlay.
     */
    override fun onDone() {
        mOverlay.remove(mGraphic)
    }
}
