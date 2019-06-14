package com.kaltura.playkit.samples.fulldemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.kaltura.playkit.samples.fulldemo.utilities.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.kaltura.playkit.samples.fulldemo.Consts.*;

import static com.kaltura.playkit.samples.fulldemo.R.id.customTag;
import static com.kaltura.playkit.samples.fulldemo.R.id.mediaLic;
import static com.kaltura.playkit.samples.fulldemo.R.id.mediaUrl;


public class VideoListFragment extends Fragment implements LoaderManager.LoaderCallbacks<String>{

    private static final int ADS_LOADER = 22;

    private OnVideoSelectedListener mSelectedCallback;
    LayoutInflater mInflater;
    ViewGroup mContainer;

    private int minAdDurationForSkipButton;
    private boolean isAutoPlay;
    private Long startPosition;
    private int adLoadTimeOut;
    private String videoMimeType;
    private int videoBitrate;
    private int companionAdWidth;
    private int companionAdHeight;
    private ProgressBar loadingIndicator;
    private View rootView;
    private ListView listView;
    /**
     * Listener called when the user selects a video from the list.
     * Container activity must implement this interface.
     */
    public interface OnVideoSelectedListener {
        void onVideoSelected(VideoItem videoItem);
    }

    private OnVideoListFragmentResumedListener mResumeCallback;

    /**
     * Listener called when the video list fragment resumes.
     */
    public interface OnVideoListFragmentResumedListener {
        void onVideoListFragmentResumed();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mSelectedCallback = (OnVideoSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement " + OnVideoSelectedListener.class.getName());
        }

        try {
            mResumeCallback = (OnVideoListFragmentResumedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement " + OnVideoListFragmentResumedListener.class.getName());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mInflater = inflater;
        mContainer = container;

        isAutoPlay    = getArguments().getBoolean(AUTO_PLAY);
        startPosition = getArguments().getLong(START_FROM);
        minAdDurationForSkipButton = getArguments().getInt(MIN_AD_DURATION_FOR_SKIP_BUTTON);
        adLoadTimeOut = getArguments().getInt(AD_LOAD_TIMEOUT);
        videoMimeType = getArguments().getString(MIME_TYPE);
        videoBitrate  = getArguments().getInt(PREFERRED_BITRATE);
        companionAdWidth  = getArguments().getInt(COMPANION_AD_WIDTH);
        companionAdHeight = getArguments().getInt(COMPANION_AD_HEIGHT);

        rootView = inflater.inflate(R.layout.fragment_video_list, container, false);
        listView = rootView.findViewById(R.id.videoListView);
        loadingIndicator = rootView.findViewById(R.id.pb_loading_indicator);

        getActivity().getSupportLoaderManager().initLoader(ADS_LOADER, null, this);

        LoaderManager loaderManager = getActivity().getSupportLoaderManager();
        Loader<String> adsLoader = loaderManager.getLoader(ADS_LOADER);
        Bundle ads = new Bundle();
        if (adsLoader == null) {
            loaderManager.initLoader(ADS_LOADER, ads, this);
        } else {
            loaderManager.restartLoader(ADS_LOADER, ads, this);
        }

        return rootView;
    }

    private void getCustomAdTag(VideoItem originalVideoItem) {
        View dialogView = mInflater.inflate(R.layout.custom_ad_tag, mContainer, false);

        final EditText videoUrl = dialogView.findViewById(mediaUrl);
        videoUrl.setHint("Media URL");

        final EditText licUrl = dialogView.findViewById(mediaLic);
        licUrl.setHint("Media Lic URL");

        final EditText adUrl = dialogView.findViewById(customTag);
        adUrl.setHint("Ad Tag URL/XML");
        final VideoItem videoItem = originalVideoItem;

        new AlertDialog.Builder(this.getActivity())
                .setTitle("Custom Ad Tag URL/XML(plaint text)")
                .setView(dialogView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String customMediaUrl = (!TextUtils.isEmpty(videoUrl.getText().toString())) ? videoUrl.getText().toString() : SOURCE_URL1;
                        String customMediaLicUrl = (!TextUtils.isEmpty(licUrl.getText())) ? licUrl.getText().toString() : "";
//                        String vmap = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
//                                "<vmap:VMAP xmlns:vmap=\"http://www.iab.net/videosuite/vmap\" version=\"1.0\">\n" +
//                                "  <vmap:AdBreak timeOffset=\"start\" breakType=\"linear\" breakId=\"preroll\">\n" +
//                                "    <vmap:AdSource id=\"preroll-ad\" allowMultipleAds=\"false\" followRedirects=\"true\">\n" +
//                                "      <AdTagURI templateType=\"vast3\"><![CDATA[ http://demo.jwplayer.com/advertising/assets/support/preroll.xml]]></AdTagURI>\n" +
//                                "    </vmap:AdSource>\n" +
//                                "  </vmap:AdBreak>\n" +
//                                "</vmap:VMAP>";
                        String customAdTagUrl = adUrl.getText().toString();

                        VideoItem customAdTagVideoItem = new VideoItem(videoItem.getTitle(), customMediaUrl, customMediaLicUrl, customAdTagUrl, videoItem.getImageResource());

                        if (mSelectedCallback != null) {
                            mSelectedCallback.onVideoSelected(customAdTagVideoItem);
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .show();
    }

    private List<VideoItem> getVideoItems() {
        return  VideoMetadata.getDefaultVideoList();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mResumeCallback != null) {
            mResumeCallback.onVideoListFragmentResumed();
        }
    }
    @Override
    public Loader<String> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<String>(getContext()) {

            @Override
            protected void onStartLoading() {

//                if (args == null) {
//                    return;
//                }


                loadingIndicator.setVisibility(View.VISIBLE);
                forceLoad();
            }

            @Override
            public String loadInBackground() {
                try {
                    URL adsUrl = new URL(ADS_JSON_FILE_URL);
                    String adsResult = NetworkUtils.getResponseFromHttpUrl(adsUrl);
                    return adsResult;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        loadingIndicator.setVisibility(View.INVISIBLE);
        if (null == data) {
            VideoItemAdapter videoItemAdapter = new VideoItemAdapter(rootView.getContext(),
                    R.layout.video_item, VideoMetadata.getDefaultVideoList());
            listView.setAdapter(videoItemAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    if (mSelectedCallback != null) {
                        VideoItem selectedVideo = (VideoItem) listView.getItemAtPosition(position);

                        // If applicable, prompt the user to input a custom ad tag.
                        if (selectedVideo.getAdTagUrl().equals(getString(
                                R.string.custom_ad_tag_value))) {
                            getCustomAdTag(selectedVideo);
                        } else {
                            mSelectedCallback.onVideoSelected(selectedVideo);
                        }
                    }
                }
            });
        } else {
            JSONObject jObject = null;
            try {
                jObject = new JSONObject(data);
                String title = jObject.getString("name");
                // Get the Array named translations that contains all the translations
                JSONArray jsonArray = jObject.getJSONArray("samples");
                if (jsonArray != null) {
                    List<VideoItem> samples = new ArrayList<>();
                    samples.add(new VideoItem(
                            "Custom Ad Tag / Custom XML Ad Tag",
                            SOURCE_URL1,
                            LIC_URL1,
                            "custom",
                            R.drawable.k_image));
                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject sample = jsonArray.getJSONObject(i);

                        String streamName = sample.getString("name");
                        String streamUri = sample.getString("uri");
                        String streamLic = sample.has("lic") ? sample.getString("lic") : "";
                        String adtagUri = sample.getString("ad_tag_uri");
                        samples.add(new VideoItem(streamName, streamUri, streamLic, adtagUri,R.drawable.k_image));
                        //String videoUrl, String videoLic, String title, String adTagUrl, int thumbnail
                    }
                    SampleGroup sampleGroup = new SampleGroup(title, samples);

                    VideoItemAdapter videoItemAdapter = new VideoItemAdapter(rootView.getContext(),
                            R.layout.video_item, sampleGroup.samples);
                    listView.setAdapter(videoItemAdapter);

                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                            if (mSelectedCallback != null) {
                                VideoItem selectedVideo = (VideoItem) listView.getItemAtPosition(position);

                                // If applicable, prompt the user to input a custom ad tag.
                                if (selectedVideo.getAdTagUrl().equals(getString(
                                        R.string.custom_ad_tag_value))) {
                                    getCustomAdTag(selectedVideo);
                                } else {
                                    mSelectedCallback.onVideoSelected(selectedVideo);
                                }
                            }
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }

    private static final class SampleGroup {

        public final String title;
        public final List<VideoItem> samples;

        public SampleGroup(String title, List<VideoItem> samples) {
            this.title = title;
            this.samples = samples;
        }
    }



    private static final class AdsSample {

        public String name;
        public String uri;
        public String lic;
        public String adTagUri;
        public int image;

        public AdsSample(String name, String uri, String lic, String adTagUri, int image) {
            this.name = name;
            this.uri = uri;
            this.lic = lic;
            this.adTagUri = adTagUri;
            this.image = image;

        }
    }
}