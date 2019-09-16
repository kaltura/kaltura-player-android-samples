package com.kaltura.playkit.samples.audioonlybasicsetup;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.kaltura.playkit.PKDrmParams;
import com.kaltura.playkit.PKLog;
import com.kaltura.playkit.PKMediaEntry;
import com.kaltura.playkit.PKMediaFormat;
import com.kaltura.playkit.PKMediaSource;
import com.kaltura.playkit.PKPluginConfigs;
import com.kaltura.playkit.PKSubtitleFormat;
import com.kaltura.playkit.PlayerEvent;
import com.kaltura.playkit.player.PKExternalSubtitle;
import com.kaltura.playkit.player.PKTracks;
import com.kaltura.playkit.plugins.ads.AdEvent;
import com.kaltura.playkit.plugins.ima.IMAConfig;
import com.kaltura.playkit.plugins.ima.IMAPlugin;
import com.kaltura.tvplayer.KalturaBasicPlayer;
import com.kaltura.tvplayer.KalturaPlayer;
import com.kaltura.tvplayer.PlayerInitOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final PKLog log = PKLog.get("MainActivity");

    private static final Long START_POSITION = 0L; // position tp start playback in msec.

    private static final PKMediaFormat MEDIA_FORMAT = PKMediaFormat.mp3;

    private static final String SOURCE_URL = "http://www.largesound.com/ashborytour/sound/brobob.mp3";
    private static final String AD_TAG_URL = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&cmsid=496&vid=short_onecue&correlator=";
    private static final String AD_TAG_URL_PRE = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dskippablelinear&correlator=";

    private static final String LICENSE_URL = null;

    private KalturaPlayer player;
    private Button playPauseButton;
    private ImageView artworkView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        artworkView = findViewById(R.id.artwork_view);

        PKMediaEntry mediaEntry = createMediaEntry();

        loadPlaykitPlayer(mediaEntry);

        addPlayPauseButton();
    }

    private void addAdEvents() {
        player.addListener(this, AdEvent.contentPauseRequested, event -> {
            showArtworkForAudioContent(View.GONE);
        });

        player.addListener(this, AdEvent.contentResumeRequested, event -> {
            showArtworkForAudioContent(View.VISIBLE);
        });

        player.addListener(this, AdEvent.error, event -> {
            showArtworkForAudioContent(View.VISIBLE);
        });
    }

    private void setExternalSubtitles(PKMediaEntry mediaEntry) {

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

        mediaEntry.setExternalSubtitleList(mList);
    }

    /**
     * Create {@link PKMediaEntry} with minimum necessary data.
     *
     * @return - the {@link PKMediaEntry} object.
     */
    private PKMediaEntry createMediaEntry() {
        //Create media entry.
        PKMediaEntry mediaEntry = new PKMediaEntry();

        // Set External Subtitle
        setExternalSubtitles(mediaEntry);

        //Set id for the entry.
        mediaEntry.setId("testEntry");

        //Set media entry type. It could be Live,Vod or Unknown.
        //In this sample we use Vod.
        mediaEntry.setMediaType(PKMediaEntry.MediaEntryType.Vod);

        //Create list that contains at least 1 media source.
        //Each media entry can contain a couple of different media sources.
        //All of them represent the same content, the difference is in it format.
        //For example same entry can contain PKMediaSource with dash and another
        // PKMediaSource can be with hls. The player will decide by itself which source is
        // preferred for playback.
        List<PKMediaSource> mediaSources = createMediaSources();

        //Set media sources to the entry.
        mediaEntry.setSources(mediaSources);

        return mediaEntry;
    }

    /**
     * Create list of {@link PKMediaSource}.
     *
     * @return - the list of sources.
     */
    private List<PKMediaSource> createMediaSources() {

        //Create new PKMediaSource instance.
        PKMediaSource mediaSource = new PKMediaSource();

        //Set the id.
        mediaSource.setId("testSource");

        //Set the content url. In our case it will be link to hls source(.m3u8).
        mediaSource.setUrl(SOURCE_URL);

        //Set the format of the source. In our case it will be hls in case of mpd/wvm formats you have to to call mediaSource.setDrmData method as well
        mediaSource.setMediaFormat(MEDIA_FORMAT);

        // Add DRM data if required
        if (LICENSE_URL != null) {
            mediaSource.setDrmData(Collections.singletonList(
                    new PKDrmParams(LICENSE_URL, PKDrmParams.Scheme.WidevineCENC)
            ));
        }

        return Collections.singletonList(mediaSource);
    }

    /**
     * Just add a simple button which will start/pause playback.
     */
    private void addPlayPauseButton() {
        //Get reference to the play/pause button.
        playPauseButton = this.findViewById(R.id.play_pause_button);
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

    private void showArtworkForAudioContent(int visibility) {
        artworkView.setVisibility(visibility);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            if (playPauseButton != null) {
                playPauseButton.setText(R.string.pause_text);
            }
            player.onApplicationResumed();
            player.play();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.onApplicationPaused();
        }
    }

    public void loadPlaykitPlayer(PKMediaEntry pkMediaEntry) {
        PlayerInitOptions playerInitOptions = new PlayerInitOptions();
        playerInitOptions.setAutoPlay(true);

        // Audio Only setup
        playerInitOptions.setIsVideoViewHidden(true);

        // IMA Configuration
        PKPluginConfigs pkPluginConfigs = new PKPluginConfigs();
        IMAConfig adsConfig = getAdsConfig(AD_TAG_URL_PRE);
        pkPluginConfigs.setPluginConfig(IMAPlugin.factory.getName(), adsConfig);

        playerInitOptions.setPluginConfigs(pkPluginConfigs);

        player = KalturaBasicPlayer.create(MainActivity.this, playerInitOptions);
        addAdEvents();
        subscribeToTracksAvailableEvent();

        showArtworkForAudioContent(View.VISIBLE);

        player.setPlayerView(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.MATCH_PARENT);
        ViewGroup container = findViewById(R.id.player_root);
        container.addView(player.getPlayerView());
        player.setMedia(pkMediaEntry, START_POSITION);
    }

    private IMAConfig getAdsConfig(String adTagUrl) {
        List<String> videoMimeTypes = new ArrayList<>();
        videoMimeTypes.add("video/mp4");
        videoMimeTypes.add("application/x-mpegURL");
        videoMimeTypes.add("application/dash+xml");
        return new IMAConfig().setAdTagUrl(adTagUrl).setVideoMimeTypes(videoMimeTypes).enableDebugMode(true).setAlwaysStartWithPreroll(true).setAdLoadTimeOut(8);
    }

    private void subscribeToTracksAvailableEvent() {
        player.addListener(this, PlayerEvent.tracksAvailable, event -> {
            //When the track data available, this event occurs. It brings the info object with it.
            log.d("Event TRACKS_AVAILABLE");

            //Cast event to the TracksAvailable object that is actually holding the necessary data.
            PlayerEvent.TracksAvailable tracksAvailable = (PlayerEvent.TracksAvailable) event;

            //Obtain the actual tracks info from it. Default track index values are coming from manifest
            PKTracks tracks = tracksAvailable.tracksInfo;
            int defaultAudioTrackIndex = tracks.getDefaultAudioTrackIndex();
            int defaultTextTrackIndex = tracks.getDefaultTextTrackIndex();
            if (tracks.getAudioTracks().size() > 0) {
                log.d("Default Audio langae = " + tracks.getAudioTracks().get(defaultAudioTrackIndex).getLabel());
            }
            if (tracks.getTextTracks().size() > 0) {
                log.d("Default Text langae = " + tracks.getTextTracks().get(defaultTextTrackIndex).getLabel());
                if (tracks.getTextTracks().size() > 2) {
                    player.changeTrack(tracks.getTextTracks().get(2).getUniqueId());
                }
            }
            if (tracks.getVideoTracks().size() > 0) {
                log.d("Default video isAdaptive = " + tracks.getVideoTracks().get(tracks.getDefaultAudioTrackIndex()).isAdaptive() + " bitrate = " + tracks.getVideoTracks().get(tracks.getDefaultAudioTrackIndex()).getBitrate());
            }
        });
    }
}
