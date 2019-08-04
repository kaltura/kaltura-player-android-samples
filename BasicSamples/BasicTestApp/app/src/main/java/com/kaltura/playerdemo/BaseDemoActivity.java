package com.kaltura.playerdemo;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.kaltura.netkit.utils.ErrorElement;
import com.kaltura.playkit.PKDrmParams;
import com.kaltura.playkit.PKLog;
import com.kaltura.playkit.PKMediaEntry;
import com.kaltura.playkit.PKMediaFormat;
import com.kaltura.playkit.PKMediaSource;
import com.kaltura.playkit.PKPluginConfigs;
import com.kaltura.playkit.player.MediaSupport;
import com.kaltura.tvplayer.KalturaPlayer;
import com.kaltura.tvplayer.PlayerInitOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.kaltura.playkit.PKMediaEntry.MediaEntryType.Unknown;

public abstract class BaseDemoActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, KalturaPlayer.OnEntryLoadListener {

    public static final String PLAYER = "player";
    public static final String AUDIO_LANG = "audioLanguage";
    public static final String TEXT_LANG = "textLanguage";
    public static final String PLAYBACK = "playback";
    public static final String OFF = "off";
    public static final String AUTOPLAY = "autoPlay";
    public static final String PRELOAD = "preload";
    //public static final String START_TIME = "startTime";
    public static final String CONFIG = "config";
    public static final String PLUGINS = "plugins";
    public static final String AUTO = "auto";
    public static final String OPTIONS = "options";
    public static final String UICONF_ID ="uiConfId";
    public static final String PARTNER_ID = "partnerId";
    public static final String REFERRER = "referrer";
    public static final String KS = "ks";
    public static final String SERVER_URL = "serverUrl";
    public static final String ALLOW_CROSS_PROTOCOL_ENABLED = "allowCrossProtocolEnabled";
    public static final String STREAM_PRIORITY = "streamPriority";


    private static final PKLog log = PKLog.get("BaseDemoActivity");
    public static final String PLAYER_CONFIG = "playerConfig";
    public static final String UICONF ="uiConf";

    protected final Context context = this;
    protected JsonObject playerConfigUiConfJson;

    protected PlayerInitOptions initOptions;
    String ks;
    DemoItem[] items;
    private ViewGroup contentContainer;
    private NavigationView navigationView;
    private ListView itemListView;

    protected abstract DemoItem[] items();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initDrm();

        loadConfigFile();

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        contentContainer = findViewById(R.id.content_container);
        contentContainer.addView(getItemListView());
        navigationView.setCheckedItem(R.id.nav_gallery);
    }

    protected void parseInitOptions(JsonObject json) {
        final Integer partnerId = safeInteger(json, PARTNER_ID);
        if (partnerId == null) {
            throw new IllegalArgumentException("partnerId must not be null");
        }
        if (json.has(PLAYER_CONFIG)) {
            JsonObject playerConfigJasonObject = safeObject(json, PLAYER_CONFIG);
            final PlayerInitOptions options = new PlayerInitOptions(partnerId);
            if (initOptions == null) {
                options.setAutoPlay(safeBoolean(playerConfigJasonObject, AUTOPLAY))
                        .setPreload(safeBoolean(playerConfigJasonObject, PRELOAD))
                        .setKs(safeString(playerConfigJasonObject, KS))
                        .setPluginConfigs(parsePluginConfigs(json.get(PLUGINS)))
                        .setAllowCrossProtocolEnabled(safeBoolean(playerConfigJasonObject, ALLOW_CROSS_PROTOCOL_ENABLED))
                        .setReferrer(safeString(playerConfigJasonObject, REFERRER));

            }
            initOptions = options;
        }
    }

    private PKPluginConfigs parsePluginConfigs(JsonElement json) {
        PKPluginConfigs configs = new PKPluginConfigs();
        if (json != null && json.isJsonObject()) {
            final JsonObject obj = json.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                final String pluginName = entry.getKey();
                final JsonElement value = entry.getValue();
                configs.setPluginConfig(pluginName, value);
            }
        }
        return configs;
    }

    protected JsonObject safeObject(JsonObject json, String key) {
        final JsonElement jsonElement = json.get(key);
        if (jsonElement != null && jsonElement.isJsonObject()) {
            return jsonElement.getAsJsonObject();
        }
        return null;
    }

    protected String safeString(JsonObject json, String key) {
        final JsonElement jsonElement = json.get(key);
        if (jsonElement != null && jsonElement.isJsonPrimitive()) {
            return jsonElement.getAsString();
        }
        return null;
    }

    protected Boolean safeBoolean(JsonObject json, String key) {
        final JsonElement jsonElement = json.get(key);
        if (jsonElement != null && jsonElement.isJsonPrimitive()) {
            return jsonElement.getAsBoolean();
        }
        return null;
    }

    protected Integer safeInteger(JsonObject json, String key) {
        final JsonElement jsonElement = json.get(key);
        if (jsonElement != null && jsonElement.isJsonPrimitive()) {
            return jsonElement.getAsInt();
        }
        return null;
    }

    protected abstract void loadConfigFile();

    void initDrm() {
        MediaSupport.initializeDrm(this, new MediaSupport.DrmInitCallback() {
            @Override
            public void onDrmInitComplete(Set<PKDrmParams.Scheme> supportedDrmSchemes, boolean provisionPerformed, Exception provisionError) {
                if (provisionPerformed) {
                    if (provisionError != null) {
                        log.e("DRM Provisioning failed", provisionError);
                    } else {
                        log.d("DRM Provisioning succeeded");
                    }
                }
                log.d("DRM initialized; supported: " + supportedDrmSchemes);

                // Now it's safe to look at `supportedDrmSchemes`
            }
        });
    }

    protected void parseCommonOptions(JsonObject json) {
        parseInitOptions(safeObject(json, "initOptions"));

        if (initOptions != null) {
            ks = initOptions.ks;
        }
        final JsonArray jsonItems = json.get("items").getAsJsonArray();
        List<DemoItem> itemList = new ArrayList<>(jsonItems.size());
        for (JsonElement item : jsonItems) {
            final JsonObject object = item.getAsJsonObject();
            itemList.add(parseItem(object));
        }

        items = itemList.toArray(new DemoItem[itemList.size()]);
    }

    protected void parseBasicCommonOptions(JsonObject json) {
        parseInitOptions(safeObject(json, "initOptions"));

        if (initOptions != null) {
            ks = initOptions.ks;
        }
        final JsonArray jsonItems = json.get("items").getAsJsonArray();
        List<DemoItem> itemList = new ArrayList<>(jsonItems.size());
        for (JsonElement item : jsonItems) {
            final JsonObject object = item.getAsJsonObject();
            PKMediaEntry pkMediaEntry = MockMediaParser.parseMedia(object);
            itemList.add(new DemoItem(pkMediaEntry.getName(), pkMediaEntry.getId(), pkMediaEntry));
        }

        items = itemList.toArray(new DemoItem[itemList.size()]);
    }

    static class MockMediaParser {

        static PKMediaEntry parseMedia(JsonObject mediaObject) throws JsonSyntaxException {

            PKMediaEntry mediaEntry = new Gson().fromJson(mediaObject, PKMediaEntry.class);
            if (mediaEntry.getMediaType() == null) {
                mediaEntry.setMediaType(Unknown);
            }
            List<PKMediaSource> mediaSources = mediaEntry.getSources();
            for (PKMediaSource mediaSource : mediaSources) {
                PKMediaFormat format = PKMediaFormat.valueOfUrl(mediaSource.getUrl());
                if (format == null) {
                    String mimeType = getMimeTypeFromJson(mediaObject);
                    if (mimeType != null) {
                        if (mimeType.equals(PKMediaFormat.dash.mimeType)) {
                            format = PKMediaFormat.dash;
                        } else if (mimeType.equals(PKMediaFormat.hls.mimeType)) {
                            format = PKMediaFormat.hls;
                        } else if (mimeType.equals(PKMediaFormat.wvm.mimeType)) {
                            format = PKMediaFormat.wvm;
                        } else if (mimeType.equals(PKMediaFormat.mp4.mimeType)) {
                            format = PKMediaFormat.mp4;
                        } else if (mimeType.equals(PKMediaFormat.mp3.mimeType)) {
                            format = PKMediaFormat.mp3;
                        }
                    }
                }
                mediaSource.setMediaFormat(format);
            }
            return mediaEntry;
        }
    }

    @Nullable
    private static String getMimeTypeFromJson(JsonObject mediaObject) {
        String mimeType = null;
        try {
            JSONObject jsonObj = new JSONObject(mediaObject.toString());
            JSONArray sources = jsonObj.getJSONArray("sources");
            if (sources != null && sources.length() > 0) {
                JSONObject sourcesJson = sources.getJSONObject(0);
                mimeType = sourcesJson.getString("mimeType");
                return mimeType;
            }
        } catch (JSONException e) {
            //e.printStackTrace();
            log.d("Sources does not contain mime type in it - hope url extension is valid...");
        }
        return mimeType;
    }

    @NonNull
    protected abstract DemoItem parseItem(JsonObject object);

    protected int partnerId() {
        if (initOptions == null || initOptions.tvPlayerParams == null) {
            return 0;
        }
        return initOptions.tvPlayerParams.partnerId;
    }

    protected abstract void loadItem(DemoItem item);

    @Override
    public void onEntryLoadComplete(PKMediaEntry entry, ErrorElement error) {
        if (error != null) {
            Log.d("onEntryLoadComplete", " error: " + error);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        TextView partnerIdTextView = findViewById(R.id.partnerIdTextView);
        partnerIdTextView.setText("Partner: " + partnerId());

        TextView demoNameTextView = findViewById(R.id.demoNameTextView);
        demoNameTextView.setText(demoName());

        ImageView icon = findViewById(R.id.imageView);
        icon.setColorFilter(android.R.color.holo_green_dark);

        return true;
    }

    protected abstract String demoName();

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        contentContainer.removeAllViews();

        switch (item.getItemId()) {
            case R.id.nav_gallery:
                contentContainer.addView(getItemListView());
                break;
            case R.id.nav_downloads:
            case R.id.nav_plugins:
                // TODO
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private ListView getItemListView() {
        if (itemListView != null) {
            return itemListView;
        }

        ArrayAdapter<DemoItem> itemArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        itemArrayAdapter.addAll(items());


        itemListView = new ListView(this);

        itemListView.setAdapter(itemArrayAdapter);

        itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(context)
                        .setTitle(getString(R.string.select_action))
                        .setItems(new String[]{getString(R.string.play_stream)}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    loadItem(((DemoItem) parent.getItemAtPosition(position)));
                                }
                            }
                        }).show();
            }
        });

        return itemListView;
    }
}