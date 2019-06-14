package com.kaltura.playkitdemo;

class BitRateRange {



    private QualityType quality;
    private long low;
    private long high;

    public enum QualityType {
        Auto("auto"),
        Low("low"),
        Mediun("mediun"),
        High("high");

        public String value;

        QualityType(String value){
            this.value = value;
        }
    }

    public BitRateRange(QualityType quality, long low, long high) {
        this.quality = quality;
        this.low = low;
        this.high = high;
    }

    public QualityType getQuality() {
        return quality;
    }

    public long getLow() {
        return low;
    }

    public long getHigh() {
        return high;
    }

    static BitRateRange getAutoQuality(){
        return new BitRateRange(QualityType.Auto, 0, 0);
    }

    static BitRateRange getLowQuality(){
        return new BitRateRange(QualityType.Low, 100000, 450000);
    }

    static BitRateRange getMedQuality(){
        return new BitRateRange(QualityType.Low, 450001, 600000);
    }

    static BitRateRange getHighQuality(){
        return new BitRateRange(QualityType.Low, 600001, 1000000);
    }

}
