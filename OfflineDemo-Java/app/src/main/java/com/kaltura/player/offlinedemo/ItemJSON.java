package com.kaltura.player.offlinedemo;

public class ItemJSON {

    private String id;
    private String title;
    private Integer partnerId;
    private String ks;
    private String env;
    private String url;
    private OptionsJSON options;
    private ExpectedValues expected;
    private boolean ott;
    private ItemOTTParamsJSON ottParams;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(int partnerId) {
        this.partnerId = partnerId;
    }

    public String getKs() {
        return ks;
    }

    public void setKs(String ks) {
        this.ks = ks;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public OptionsJSON getOptions() {
        return options;
    }

    public void setOptions(OptionsJSON options) {
        this.options = options;
    }

    public ExpectedValues getExpected() {
        return expected;
    }

    public void setExpected(ExpectedValues expected) {
        this.expected = expected;
    }

    public Boolean getOtt() {
        return ott;
    }

    public void setOtt(Boolean ott) {
        this.ott = ott;
    }

    public ItemOTTParamsJSON getOttParams() {
        return ottParams;
    }

    public void setOttParams(ItemOTTParamsJSON ottParams) {
        this.ottParams = ottParams;
    }

    public Item toItem() {
        if (partnerId != null) {
            if (this.ott) {
                // OTT
                return new OTTItem(partnerId, this.id, env, ottParams.getFormat(), options.toPrefs(), title);
            } else {
                // OVP
                return new OVPItem(partnerId, id, env, options.toPrefs(), title);
            }
        } else {
            return new BasicItem(id, url, options.toPrefs(), title);
        }
    }
}
