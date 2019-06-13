package com.kaltura.playkit.samples.subtitlesideloading.tracks;

/**
 * Created by anton.afanasiev on 16/03/2017.
 */

public class TrackItem {


    private String trackName; //Readable name of the track.
    private String uniqueId; //Unique id, which should be passed to player in order to change track.

    public TrackItem(String trackName, String uniqueId) {
        this.trackName = trackName;
        this.uniqueId = uniqueId;
    }

    public String getTrackName() {
        return trackName;
    }

    public String getUniqueId() {
        return uniqueId;
    }
}
