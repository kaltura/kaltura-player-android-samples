package com.kaltura.playkit.samples.tracksselection;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.kaltura.playkit.PKSubtitleFormat;
import com.kaltura.playkit.PlayerEvent;
import com.kaltura.playkit.player.AudioTrack;
import com.kaltura.playkit.player.PKExternalSubtitle;
import com.kaltura.playkit.player.PKTracks;
import com.kaltura.playkit.player.SubtitleStyleSettings;
import com.kaltura.playkit.player.TextTrack;
import com.kaltura.playkit.player.VideoTrack;
import com.kaltura.playkit.providers.api.phoenix.APIDefines;
import com.kaltura.playkit.providers.ott.PhoenixMediaProvider;
import com.kaltura.playkit.samples.tracksselection.tracks.TrackItem;
import com.kaltura.playkit.samples.tracksselection.tracks.TrackItemAdapter;
import com.kaltura.tvplayer.KalturaPlayer;
import com.kaltura.tvplayer.OTTMediaOptions;
import com.kaltura.tvplayer.PlayerConfigManager;
import com.kaltura.tvplayer.PlayerInitOptions;
import com.kaltura.tvplayer.TVPlayerType;
import com.kaltura.tvplayer.config.PhoenixConfigurationsResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "MainActivity";

    private static final Long START_POSITION = 0L; // position tp start playback in msec.

    private static final String SERVER_URL = "https://rest-us.ott.kaltura.com/v4_5/api_v3/";
    private static final String ASSET_ID = "548576";
    private static final int PARTNER_ID = 3009;

    private KalturaPlayer player;
    private PlayerInitOptions playerInitOptions;
    private Button playPauseButton;

    //Android Spinner view, that will actually hold and manipulate tracks selection.
    private Spinner videoSpinner, audioSpinner, textSpinner, ccStyleSpinner;
    private TextView tvSpinnerTitle;
    private boolean userIsInteracting;
    private boolean isFullScreen;
    private View tracksSelectionMenu;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadPlaykitPlayer();

        (findViewById(R.id.activity_main)).setOnClickListener(v -> {
            if (isFullScreen) {
                tracksSelectionMenu.animate().translationY(0);
                isFullScreen = false;
            } else {
                tracksSelectionMenu.animate().translationY(-200);
                isFullScreen = true;
            }
        });

    }

    /**
     * Just add a simple button which will start/pause playback.
     */
    private void addPlayPauseButton() {
        //Get reference to the play/pause button.
        playPauseButton = (Button) this.findViewById(R.id.play_pause_button);
        //Add clickListener.
        playPauseButton.setOnClickListener(v -> {
            if (player.isPlaying()) {
                //If player is playing, change text of the button and pause.
                playPauseButton.setText(R.string.play_text);
                player.pause();
            } else {
                //If player is not playing, change text of the button and play.
                playPauseButton.setText(R.string.pause_text);
                player.play();
            }
        });
    }

    /**
     * Here we are getting access to the Android Spinner views,
     * and set OnItemSelectedListener.
     */
    private void initializeTrackSpinners() {
        tracksSelectionMenu = (View) this.findViewById(R.id.tracks_selection_menu);
        videoSpinner = (Spinner) this.findViewById(R.id.videoSpinner);
        audioSpinner = (Spinner) this.findViewById(R.id.audioSpinner);
        textSpinner = (Spinner) this.findViewById(R.id.textSpinner);
        ccStyleSpinner = (Spinner) this.findViewById(R.id.ccStyleSpinner);
        tvSpinnerTitle = (TextView) this.findViewById(R.id.tvSpinnerTitle);
        tvSpinnerTitle.setVisibility(View.INVISIBLE);
        ccStyleSpinner.setVisibility(View.INVISIBLE);

        textSpinner.setOnItemSelectedListener(this);
        audioSpinner.setOnItemSelectedListener(this);
        videoSpinner.setOnItemSelectedListener(this);

        ArrayList<String> stylesStrings = new ArrayList<>();
        stylesStrings.add(getDefaultPositionDefault().getStyleName());
        stylesStrings.add(getStyleForPositionOne().getStyleName());
        stylesStrings.add(getStyleForPositionTwo().getStyleName());
        ArrayAdapter<String> ccStyleAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, stylesStrings);
        ccStyleSpinner.setAdapter(ccStyleAdapter);
        ccStyleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!userIsInteracting) {
                    return;
                }

                if (position == 0) {
                    player.updateSubtitleStyle(getDefaultPositionDefault());
                } else if(position == 1) {
                    player.updateSubtitleStyle(getStyleForPositionOne());
                } else {
                    player.updateSubtitleStyle(getStyleForPositionTwo());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private SubtitleStyleSettings getDefaultPositionDefault() {
        return new SubtitleStyleSettings("DefaultStyle");
    }

    private SubtitleStyleSettings getStyleForPositionOne() {
        return new SubtitleStyleSettings("KidsStyle")
                .setBackgroundColor(Color.BLUE)
                .setTextColor(Color.WHITE)
                .setTextSizeFraction(SubtitleStyleSettings.SubtitleTextSizeFraction.SUBTITLE_FRACTION_50)
                .setWindowColor(Color.YELLOW)
                .setEdgeColor(Color.BLUE)
                .setTypeface(SubtitleStyleSettings.SubtitleStyleTypeface.MONOSPACE)
                .setEdgeType(SubtitleStyleSettings.SubtitleStyleEdgeType.EDGE_TYPE_DROP_SHADOW);
    }

    private SubtitleStyleSettings getStyleForPositionTwo() {
        return new SubtitleStyleSettings("AdultsStyle")
                .setBackgroundColor(Color.WHITE)
                .setTextColor(Color.BLUE)
                .setTextSizeFraction(SubtitleStyleSettings.SubtitleTextSizeFraction.SUBTITLE_FRACTION_100)
                .setWindowColor(Color.BLUE)
                .setEdgeColor(Color.BLUE)
                .setTypeface(SubtitleStyleSettings.SubtitleStyleTypeface.SANS_SERIF)
                .setEdgeType(SubtitleStyleSettings.SubtitleStyleEdgeType.EDGE_TYPE_DROP_SHADOW);
    }

    /**
     * Subscribe to the TRACKS_AVAILABLE event. This event will be sent
     * every time new source have been loaded and it tracks data is obtained
     * by the player.
     */
    private void subscribeToTracksAvailableEvent() {

        player.addListener(this, PlayerEvent.tracksAvailable, event -> {
            Log.d(TAG, "Event TRACKS_AVAILABLE");

            //Cast event to the TracksAvailable object that is actually holding the necessary data.
            PlayerEvent.TracksAvailable tracksAvailable = (PlayerEvent.TracksAvailable) event;

            //Obtain the actual tracks info from it. Default track index values are coming from manifest
            PKTracks tracks = tracksAvailable.tracksInfo;
            int defaultAudioTrackIndex = tracks.getDefaultAudioTrackIndex();
            int defaultTextTrackIndex = tracks.getDefaultTextTrackIndex();
            if (tracks.getAudioTracks().size() > 0) {
                Log.d(TAG, "Default Audio langae = " + tracks.getAudioTracks().get(defaultAudioTrackIndex).getLabel());
            }
            if (tracks.getTextTracks().size() > 0) {
                Log.d(TAG, "Default Text langae = " + tracks.getTextTracks().get(defaultTextTrackIndex).getLabel());
                if(tvSpinnerTitle != null && ccStyleSpinner != null) {
                    tvSpinnerTitle.setVisibility(View.VISIBLE);
                    ccStyleSpinner.setVisibility(View.VISIBLE);
                }
            }
            if (tracks.getVideoTracks().size() > 0) {
                Log.d(TAG, "Default video isAdaptive = " + tracks.getVideoTracks().get(tracks.getDefaultAudioTrackIndex()).isAdaptive() + " bitrate = " + tracks.getVideoTracks().get(tracks.getDefaultAudioTrackIndex()).getBitrate());
            }
            //player.changeTrack(tracksAvailable.tracksInfo.getVideoTracks().get(1).getUniqueId());
            //Populate Android spinner views with received data.
            populateSpinnersWithTrackInfo(tracks);
        });


        player.addListener(this, PlayerEvent.videoTrackChanged, event -> {
            Log.d(TAG, "Event VideoTrackChanged " + event.newTrack.getBitrate());

        });

        player.addListener(this, PlayerEvent.audioTrackChanged, event -> {
            Log.d(TAG, "Event AudioTrackChanged " + event.newTrack.getLanguage());

        });

        player.addListener(this, PlayerEvent.textTrackChanged, event -> {
            Log.d(TAG, "Event TextTrackChanged " + event.newTrack.getLanguage());

        });

        player.addListener(this, PlayerEvent.subtitlesStyleChanged, event -> {
            Log.d(TAG, "Event SubtitlesStyleChanged " + event.styleName);

        });

        player.addListener(this, PlayerEvent.error, event -> {
            PlayerEvent.Error playerError = event;
            if (playerError != null && playerError.error != null) {
                Log.d(TAG, "PlayerEvent.Error event  position = " + playerError.error.errorType + " errorMessage = " + playerError.error.message);
            }

        });
    }

    /**
     * Populate Android Spinners with retrieved tracks data.
     * Here we are building custom {@link TrackItem} objects, with which
     * we will populate our custom spinner adapter.
     * @param tracks - {@link PKTracks} object with all tracks data in it.
     */
    private void populateSpinnersWithTrackInfo(PKTracks tracks) {

        //Build track items that are based on videoTrack data.
        TrackItem[] videoTrackItems = buildVideoTrackItems(tracks.getVideoTracks());
        //populate spinner with this info.
        applyAdapterOnSpinner(videoSpinner, videoTrackItems, tracks.getDefaultVideoTrackIndex());

        //Build track items that are based on audioTrack data.
        TrackItem[] audioTrackItems = buildAudioTrackItems(tracks.getAudioTracks());
        //populate spinner with this info.
        applyAdapterOnSpinner(audioSpinner, audioTrackItems, tracks.getDefaultAudioTrackIndex());

        //Build track items that are based on textTrack data.
        TrackItem[] textTrackItems = buildTextTrackItems(tracks.getTextTracks());
        //populate spinner with this info.
        applyAdapterOnSpinner(textSpinner, textTrackItems, tracks.getDefaultTextTrackIndex());
    }

    /**
     * Will build array of {@link TrackItem} objects.
     * Each {@link TrackItem} object will hold the readable name.
     * In this case the width and height of the video track.
     * If {@link VideoTrack} is adaptive, we will name it "Auto".
     * We use this name to represent the track selection options.
     * Also each {@link TrackItem} will hold the unique id of the track,
     * which should be passed to the player in order to switch to the desired track.
     * @param videoTracks - the list of available video tracks.
     * @return - array with custom {@link TrackItem} objects.
     */
    private TrackItem[] buildVideoTrackItems(List<VideoTrack> videoTracks) {
        //Initialize TrackItem array with size of videoTracks list.
        TrackItem[] trackItems = new TrackItem[videoTracks.size()];

        //Iterate through all available video tracks.
        for (int i = 0; i < videoTracks.size(); i++) {
            //Get video track from index i.
            VideoTrack videoTrackInfo = videoTracks.get(i);

            //Check if video track is adaptive. If so, give it "Auto" name.
            if (videoTrackInfo.isAdaptive()) {
                //In this case, if this track is selected, the player will
                //adapt the playback bitrate automatically, based on user bandwidth and device capabilities.
                //Initialize TrackItem.
                trackItems[i] = new TrackItem("Auto", videoTrackInfo.getUniqueId());
            } else {

                //If it is not adaptive track, build readable name based on width and height of the track.
                StringBuilder nameStringBuilder = new StringBuilder();
                nameStringBuilder.append(videoTrackInfo.getBitrate());

                //Initialize TrackItem.
                trackItems[i] = new TrackItem(nameStringBuilder.toString(), videoTrackInfo.getUniqueId());
            }
        }
        return trackItems;
    }

    /**
     * Will build array of {@link TrackItem} objects.
     * Each {@link TrackItem} object will hold the readable name.
     * In this case the label of the audio track.
     * If {@link AudioTrack} is adaptive, we will name it "Auto".
     * We use this name to represent the track selection options.
     * Also each {@link TrackItem} will hold the unique id of the track,
     * which should be passed to the player in order to switch to the desired track.
     * @param audioTracks - the list of available audio tracks.
     * @return - array with custom {@link TrackItem} objects.
     */
    private TrackItem[] buildAudioTrackItems(List<AudioTrack> audioTracks) {
        //Initialize TrackItem array with size of audioTracks list.
        TrackItem[] trackItems = new TrackItem[audioTracks.size()];

        Map<Integer, AtomicInteger> channelMap = new HashMap<>();
        for (int i = 0; i < audioTracks.size(); i++) {
            if (channelMap.containsKey(audioTracks.get(i).getChannelCount())) {
                channelMap.get(audioTracks.get(i).getChannelCount()).incrementAndGet();
            } else {
                channelMap.put(audioTracks.get(i).getChannelCount(), new AtomicInteger(1));
            }
        }
        boolean addChannel = false;

        if (channelMap.keySet().size() > 0 && !(new AtomicInteger(audioTracks.size()).toString().equals(channelMap.get(audioTracks.get(0).getChannelCount()).toString()))) {
            addChannel = true;
        }


        //Iterate through all available audio tracks.
        for (int i = 0; i < audioTracks.size(); i++) {
            AudioTrack audioTrackInfo = audioTracks.get(i);
            String label = "";
            if (audioTrackInfo != null) {
                label = (audioTrackInfo.getLabel() != null) ? audioTrackInfo.getLabel() : (audioTrackInfo.getLanguage() != null) ? audioTrackInfo.getLanguage() : "";
            }
            String bitrate = (audioTrackInfo.getBitrate() > 0) ? "" + audioTrackInfo.getBitrate() : "";
            if (TextUtils.isEmpty(bitrate) && addChannel) {
                bitrate = buildAudioChannelString(audioTrackInfo.getChannelCount());
            }
            if (audioTrackInfo.isAdaptive()) {
                bitrate += " Adaptive";
            }
            trackItems[i] = new TrackItem(label + " " + bitrate, audioTrackInfo.getUniqueId());
        }
        return trackItems;
    }

    private String buildAudioChannelString(int channelCount) {
        switch (channelCount) {
            case 1:
                return "Mono";
            case 2:
                return "Stereo";
            case 6:
            case 7:
                return "Surround_5.1";
            case 8:
                return "Surround_7.1";
            default:
                return "Surround";
        }
    }


    /**
     * Will build array of {@link TrackItem} objects.
     * Each {@link TrackItem} object will hold the readable name.
     * In this case the label of the text track.
     * We use this name to represent the track selection options.
     * Also each {@link TrackItem} will hold the unique id of the track,
     * which should be passed to the player in order to switch to the desired track.
     * @param textTracks - the list of available text tracks.
     * @return - array with custom {@link TrackItem} objects.
     */
    private TrackItem[] buildTextTrackItems(List<TextTrack> textTracks) {
        //Initialize TrackItem array with size of textTracks list.
        TrackItem[] trackItems = new TrackItem[textTracks.size()];

        //Iterate through all available text tracks.
        for (int i = 0; i < textTracks.size(); i++) {

            //Get text track from index i.
            TextTrack textTrackInfo = textTracks.get(i);

            //Name TrackItem based on the text track label.
            String name = textTrackInfo.getLabel();
            trackItems[i] = new TrackItem(name, textTrackInfo.getUniqueId());
        }
        return trackItems;
    }

    /**
     * Initialize and set custom adapter to the Android spinner.
     * @param spinner - spinner to which adapter should be applied.
     * @param trackItems - custom track items array.
     */
    private void applyAdapterOnSpinner(Spinner spinner, TrackItem[] trackItems, int defaultSelectedIndex) {
        //Initialize custom adapter.
        TrackItemAdapter trackItemAdapter = new TrackItemAdapter(this, R.layout.track_items_list_row, trackItems);
        //Apply adapter on spinner.
        spinner.setAdapter(trackItemAdapter);

        if (defaultSelectedIndex > 0) {
            spinner.setSelection(defaultSelectedIndex);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (!userIsInteracting) {
            return;
        }
        //Get the selected TrackItem from adapter.
        TrackItem trackItem = (TrackItem) parent.getItemAtPosition(position);

        //Important! This will actually do the switch between tracks.
        player.changeTrack(trackItem.getUniqueId());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        userIsInteracting = true;
    }


    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        if (player != null) {
            if (playPauseButton != null) {
                playPauseButton.setText(R.string.pause_text);
            }
            player.onApplicationPaused();
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();

        if (player != null) {
            player.onApplicationResumed();
            player.play();
        }
    }

    public void loadPlaykitPlayer() {

        PlayerInitOptions playerInitOptions = new PlayerInitOptions(PARTNER_ID);
        playerInitOptions.setServerUrl(SERVER_URL);
        playerInitOptions.setSubtitleStyle(getDefaultPositionDefault());
        playerInitOptions.setAutoPlay(true);
        playerInitOptions.setAllowCrossProtocolEnabled(true);

        PlayerConfigManager.retrieve(this, TVPlayerType.ott, playerInitOptions.partnerId, playerInitOptions.serverUrl, (partnerId, config, error, freshness) -> {
            PhoenixConfigurationsResponse phoenixConfigurationsResponse = gson.fromJson(config, PhoenixConfigurationsResponse.class);
            if (phoenixConfigurationsResponse != null) {

                /*
                  //PhoenixTVPlayerParams
                  "analyticsUrl": "https://analytics.kaltura.com/api_v3/index.php"
                  "ovpServiceUrl": "https://cdnapisec.kaltura.com"
                  "ovpPartnerId": 2254732
                  "uiConfId": 44267972
                 */

                playerInitOptions.setTVPlayerParams(phoenixConfigurationsResponse.params);
            }

            player = KalturaPlayer.createOTTPlayer(MainActivity.this, playerInitOptions);
            player.setPlayerView(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            ViewGroup container = findViewById(R.id.player_root);
            container.addView(player.getPlayerView());

            OTTMediaOptions ottMediaOptions = buildOttMediaOptions();
            player.loadMedia(ottMediaOptions, (entry, loadError) -> {
                if (loadError != null) {
                    Snackbar.make(findViewById(android.R.id.content), loadError.getMessage(), Snackbar.LENGTH_LONG).show();
                } else {
                    Log.d(TAG, "OTTMedia onEntryLoadComplete  entry = " + entry.getId());
                }
            });

            //Add simple play/pause button.
            addPlayPauseButton();

            //Initialize Android spinners view.
            initializeTrackSpinners();

            //Subscribe to the event which will notify us when track data is available.
            subscribeToTracksAvailableEvent();
        });
    }

    private OTTMediaOptions buildOttMediaOptions() {
        OTTMediaOptions ottMediaOptions = new OTTMediaOptions();
        ottMediaOptions.assetId = ASSET_ID;
        ottMediaOptions.assetType = APIDefines.KalturaAssetType.Media;
        ottMediaOptions.contextType = APIDefines.PlaybackContextType.Playback;
        ottMediaOptions.assetReferenceType = APIDefines.AssetReferenceType.Media;
        ottMediaOptions.protocol = PhoenixMediaProvider.HttpProtocol.Http;
        ottMediaOptions.ks = null;
        ottMediaOptions.startPosition = START_POSITION;
        ottMediaOptions.formats = new String []{"Mobile_Main"};
        ottMediaOptions.externalSubtitles = getExternalSubtitles();

        return ottMediaOptions;
    }

    private List<PKExternalSubtitle> getExternalSubtitles() {

        List<PKExternalSubtitle> mList = new ArrayList<>();

        PKExternalSubtitle pkExternalSubtitle = new PKExternalSubtitle()
                .setUrl("http://brenopolanski.com/html5-video-webvtt-example/MIB2-subtitles-pt-BR.vtt")
                .setMimeType(PKSubtitleFormat.vtt)
                .setLabel("External_Deutsch")
                .setLanguage("deu");
        mList.add(pkExternalSubtitle);

        PKExternalSubtitle pkExternalSubtitleDe = new PKExternalSubtitle()
                .setUrl("https://mkvtoolnix.download/samples/vsshort-en.srt")
                .setMimeType(PKSubtitleFormat.srt)
                .setLabel("External_English")
                .setLanguage("eng")
                .setDefault();
        mList.add(pkExternalSubtitleDe);

        return mList;
    }

}
