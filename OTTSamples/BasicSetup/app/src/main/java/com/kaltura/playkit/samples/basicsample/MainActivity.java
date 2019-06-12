package com.kaltura.playkit.samples.basicsample;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.kaltura.playkit.PKLog;
import com.kaltura.playkit.providers.api.phoenix.APIDefines;
import com.kaltura.playkit.providers.ott.PhoenixMediaProvider;
import com.kaltura.tvplayer.KalturaPlayer;
import com.kaltura.tvplayer.OTTMediaOptions;
import com.kaltura.tvplayer.PlayerInitOptions;
import com.kaltura.tvplayer.config.player.UiConf;



public class MainActivity extends AppCompatActivity {

    private static final PKLog log = PKLog.get("MainActivity");

    private static final Long START_POSITION = 0L; // position tp start playback in msec.

    private static final int PLAYER_HEIGHT = 600;

    private static final String SERVER_URL = "https://api-preprod.ott.kaltura.com/v4_7/api_v3/";
    private static final String ASSET_ID = "480989";
    private static final int PARTNER_ID = 198;
    private static final int UICONF_ID = 41188731;
    private static final int UICONF_PARTNER_ID = 2215841;

    private KalturaPlayer player;
    private Button playPauseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadPlaykitPlayer();

        addPlayPauseButton();

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

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
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

    public void loadPlaykitPlayer() {

        PlayerInitOptions playerInitOptions = new PlayerInitOptions(PARTNER_ID, new UiConf(UICONF_ID, UICONF_PARTNER_ID));
        playerInitOptions.setServerUrl(SERVER_URL);
        playerInitOptions.setAutoPlay(true);

        player = KalturaPlayer.createOTTPlayer(MainActivity.this, playerInitOptions);
        player.setPlayerView(FrameLayout.LayoutParams.WRAP_CONTENT, PLAYER_HEIGHT);
        ViewGroup container = findViewById(R.id.player_root);
        container.addView(player.getPlayerView());

        OTTMediaOptions ottMediaOptions = buildOttMediaOptions();
        player.loadMedia(ottMediaOptions, (entry, error) -> {
            if (error != null) {
                Snackbar.make(findViewById(android.R.id.content), error.getMessage(), Snackbar.LENGTH_LONG).show();
            } else {
                log.d("OTTMedia onEntryLoadComplete  entry = " + entry.getId());
            }
        });
    }

    private OTTMediaOptions buildOttMediaOptions() {
        OTTMediaOptions ottMediaOptions = new OTTMediaOptions();
        ottMediaOptions.assetId = ASSET_ID;
        ottMediaOptions.assetType = APIDefines.KalturaAssetType.Media;
        ottMediaOptions.contextType = APIDefines.PlaybackContextType.Playback;
        ottMediaOptions.assetReferenceType = APIDefines.AssetReferenceType.Media;
        ottMediaOptions.protocol = PhoenixMediaProvider.HttpProtocol.Https;
        ottMediaOptions.ks = null;
        ottMediaOptions.startPosition = START_POSITION;
        //  ottMediaOptions.formats = new String []{"Tablet Main"};

        return ottMediaOptions;
    }
}
