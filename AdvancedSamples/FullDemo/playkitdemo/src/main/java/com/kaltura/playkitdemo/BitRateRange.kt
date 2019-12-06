package com.kaltura.playkitdemo

class BitRateRange(val quality: QualityType, val low: Long, val high: Long) {

    enum class QualityType private constructor(var value: String) {
        Auto("auto"),
        Low("low"),
        Mediun("mediun"),
        High("high")
    }

    companion object {

        val autoQuality: BitRateRange
            get() = BitRateRange(QualityType.Auto, 0, 0)

        val lowQuality: BitRateRange
            get() = BitRateRange(QualityType.Low, 100000, 450000)

        val medQuality: BitRateRange
            get() = BitRateRange(QualityType.Low, 450001, 600000)

        val highQuality: BitRateRange
            get() = BitRateRange(QualityType.Low, 600001, 1000000)
    }
}
