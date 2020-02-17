package com.kaltura.playkit.samples.fulldemo

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ListView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.AsyncTaskLoader
import androidx.loader.content.Loader
import com.kaltura.playkit.samples.fulldemo.Consts.ADS_JSON_FILE_URL
import com.kaltura.playkit.samples.fulldemo.Consts.AD_LOAD_TIMEOUT
import com.kaltura.playkit.samples.fulldemo.Consts.AUTO_PLAY
import com.kaltura.playkit.samples.fulldemo.Consts.COMPANION_AD_HEIGHT
import com.kaltura.playkit.samples.fulldemo.Consts.COMPANION_AD_WIDTH
import com.kaltura.playkit.samples.fulldemo.Consts.LIC_URL1
import com.kaltura.playkit.samples.fulldemo.Consts.MIME_TYPE
import com.kaltura.playkit.samples.fulldemo.Consts.MIN_AD_DURATION_FOR_SKIP_BUTTON
import com.kaltura.playkit.samples.fulldemo.Consts.PREFERRED_BITRATE
import com.kaltura.playkit.samples.fulldemo.Consts.SOURCE_URL1
import com.kaltura.playkit.samples.fulldemo.Consts.START_FROM
import com.kaltura.playkit.samples.fulldemo.R.id.*
import com.kaltura.playkit.samples.fulldemo.utilities.NetworkUtils
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.util.*


class VideoListFragment : Fragment(), LoaderManager.LoaderCallbacks<String> {

    private val ADS_LOADER = 22
    private var mSelectedCallback: OnVideoSelectedListener? = null
    internal var mInflater: LayoutInflater? = null
    internal var mContainer: ViewGroup? = null

    private var minAdDurationForSkipButton: Int = 0
    private var isAutoPlay: Boolean = false
    private var startPosition: Long? = null
    private var adLoadTimeOut: Int = 0
    private var videoMimeType: String? = null
    private var videoBitrate: Int = 0
    private var companionAdWidth: Int = 0
    private var companionAdHeight: Int = 0
    private var loadingIndicator: ProgressBar? = null
    private var rootView: View? = null
    private var listView: ListView? = null

    private var mResumeCallback: OnVideoListFragmentResumedListener? = null

    private val videoItems: List<VideoItem> = VideoMetadata.defaultVideoList

    /**
     * Listener called when the user selects a video from the list.
     * Container activity must implement this interface.
     */
    interface OnVideoSelectedListener {
        fun onVideoSelected(videoItem: VideoItem)
    }

    /**
     * Listener called when the video list fragment resumes.
     */
    interface OnVideoListFragmentResumedListener {
        fun onVideoListFragmentResumed()
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        try {
            mSelectedCallback = activity as OnVideoSelectedListener?
        } catch (e: ClassCastException) {
            throw ClassCastException(activity?.toString()
                    + " must implement " + OnVideoSelectedListener::class.java.name)
        }

        try {
            mResumeCallback = activity as OnVideoListFragmentResumedListener?
        } catch (e: ClassCastException) {
            throw ClassCastException(activity?.toString()
                    + " must implement " + OnVideoListFragmentResumedListener::class.java.name)
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mInflater = inflater
        mContainer = container

        arguments?.let {
            isAutoPlay = it.getBoolean(AUTO_PLAY)
            startPosition = it.getLong(START_FROM)
            minAdDurationForSkipButton = it.getInt(MIN_AD_DURATION_FOR_SKIP_BUTTON)
            adLoadTimeOut = it.getInt(AD_LOAD_TIMEOUT)
            videoMimeType = it.getString(MIME_TYPE)
            videoBitrate = it.getInt(PREFERRED_BITRATE)
            companionAdWidth = it.getInt(COMPANION_AD_WIDTH)
            companionAdHeight = it.getInt(COMPANION_AD_HEIGHT)
        }


        rootView = inflater.inflate(R.layout.fragment_video_list, container, false)
        listView = rootView?.findViewById(R.id.videoListView)
        loadingIndicator = rootView?.findViewById(R.id.pb_loading_indicator)

        activity?.supportLoaderManager?.initLoader(ADS_LOADER, null, this)

        val loaderManager = activity?.supportLoaderManager
        val adsLoader = loaderManager?.getLoader<String>(ADS_LOADER)
        val ads = Bundle()
        if (adsLoader == null) {
            loaderManager?.initLoader(ADS_LOADER, ads, this)
        } else {
            loaderManager.restartLoader(ADS_LOADER, ads, this)
        }

        return rootView
    }

    private fun getCustomAdTag(originalVideoItem: VideoItem) {
        val dialogView = mInflater?.inflate(R.layout.custom_ad_tag, mContainer, false)

        val videoUrl = dialogView?.findViewById<EditText>(mediaUrl)
        videoUrl?.hint = "Media URL"

        val licUrl = dialogView?.findViewById<EditText>(mediaLic)
        licUrl?.hint = "Media Lic URL"

        val adUrl = dialogView?.findViewById<EditText>(customTag)
        adUrl?.hint = "Ad Tag URL/XML"

        AlertDialog.Builder(this.activity)
                .setTitle("Custom Ad Tag URL/XML(plaint text)")
                .setView(dialogView)
                .setPositiveButton("OK") { dialog, whichButton ->
                    val customMediaUrl = if (!TextUtils.isEmpty(videoUrl?.text.toString())) videoUrl?.text.toString() else SOURCE_URL1
                    val customMediaLicUrl = if (!TextUtils.isEmpty(licUrl?.text)) licUrl?.text.toString() else ""
                    //                        String vmap = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    //                                "<vmap:VMAP xmlns:vmap=\"http://www.iab.net/videosuite/vmap\" version=\"1.0\">\n" +
                    //                                "  <vmap:AdBreak timeOffset=\"start\" breakType=\"linear\" breakId=\"preroll\">\n" +
                    //                                "    <vmap:AdSource id=\"preroll-ad\" allowMultipleAds=\"false\" followRedirects=\"true\">\n" +
                    //                                "      <AdTagURI templateType=\"vast3\"><![CDATA[ http://demo.jwplayer.com/advertising/assets/support/preroll.xml]]></AdTagURI>\n" +
                    //                                "    </vmap:AdSource>\n" +
                    //                                "  </vmap:AdBreak>\n" +
                    //                                "</vmap:VMAP>";
                    val customAdTagUrl = adUrl?.text.toString()

                    val customAdTagVideoItem = VideoItem(originalVideoItem.title, customMediaUrl, customMediaLicUrl, customAdTagUrl, originalVideoItem.imageResource)

                    if (mSelectedCallback != null) {
                        mSelectedCallback?.onVideoSelected(customAdTagVideoItem)
                    }
                }
                .setNegativeButton("Cancel") { dialog, whichButton -> }
                .show()
    }

    override fun onResume() {
        super.onResume()
        if (mResumeCallback != null) {
            mResumeCallback?.onVideoListFragmentResumed()
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<String> {
        return object : AsyncTaskLoader<String>(context!!) {

            override fun onStartLoading() {

                //                if (args == null) {
                //                    return;
                //                }


                loadingIndicator?.visibility = View.VISIBLE
                forceLoad()
            }

            override fun loadInBackground(): String? {
                try {
                    val adsUrl = URL(ADS_JSON_FILE_URL)
                    return NetworkUtils.getResponseFromHttpUrl(adsUrl)
                } catch (e: IOException) {
                    e.printStackTrace()
                    return null
                }

            }
        }
    }

    override fun onLoadFinished(loader: Loader<String>, data: String?) {
        loadingIndicator?.visibility = View.INVISIBLE
        if (null == data) {
            val videoItemAdapter = VideoItemAdapter(rootView?.context!!,
                    R.layout.video_item, VideoMetadata.defaultVideoList)
            listView?.adapter = videoItemAdapter

            listView?.onItemClickListener = AdapterView.OnItemClickListener { parent, v, position, id ->
                if (mSelectedCallback != null) {
                    val selectedVideo = listView?.getItemAtPosition(position) as VideoItem

                    // If applicable, prompt the user to input a custom ad tag.
                    if (selectedVideo.adTagUrl == getString(
                                    R.string.custom_ad_tag_value)) {
                        getCustomAdTag(selectedVideo)
                    } else {
                        mSelectedCallback?.onVideoSelected(selectedVideo)
                    }
                }
            }
        } else {
            var jObject: JSONObject? = null
            try {
                jObject = JSONObject(data)
                val title = jObject.getString("name")
                // Get the Array named translations that contains all the translations
                val jsonArray = jObject.getJSONArray("samples")
                if (jsonArray != null) {
                    val samples = ArrayList<VideoItem>()
                    samples.add(VideoItem(
                            "Custom Ad Tag / Custom XML Ad Tag",
                            SOURCE_URL1,
                            LIC_URL1,
                            "custom",
                            R.drawable.k_image))
                    for (i in 0 until jsonArray.length()) {

                        val sample = jsonArray.getJSONObject(i)

                        val streamName = sample.getString("name")
                        val streamUri = sample.getString("uri")
                        val streamLic = if (sample.has("lic")) sample.getString("lic") else ""
                        val adtagUri = sample.getString("ad_tag_uri")
                        samples.add(VideoItem(streamName, streamUri, streamLic, adtagUri, R.drawable.k_image))
                        //String videoUrl, String videoLic, String title, String adTagUrl, int thumbnail
                    }
                    val sampleGroup = SampleGroup(title, samples)

                    val videoItemAdapter = VideoItemAdapter(rootView?.context!!,
                            R.layout.video_item, sampleGroup.samples)
                    listView?.adapter = videoItemAdapter

                    listView?.onItemClickListener = AdapterView.OnItemClickListener { parent, v, position, id ->
                        if (mSelectedCallback != null) {
                            val selectedVideo = listView?.getItemAtPosition(position) as VideoItem

                            // If applicable, prompt the user to input a custom ad tag.
                            if (selectedVideo.adTagUrl == getString(
                                            R.string.custom_ad_tag_value)) {
                                getCustomAdTag(selectedVideo)
                            } else {
                                mSelectedCallback?.onVideoSelected(selectedVideo)
                            }
                        }
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }
    }

    override fun onLoaderReset(loader: Loader<String>) {

    }

    private class SampleGroup(val title: String, val samples: List<VideoItem>)


    private class AdsSample(var name: String, var uri: String, var lic: String, var adTagUri: String, var image: Int)

}