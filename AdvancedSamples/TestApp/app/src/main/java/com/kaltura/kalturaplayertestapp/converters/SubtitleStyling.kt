package com.kaltura.kalturaplayertestapp.converters

import android.graphics.Color
import android.text.Layout
import com.kaltura.playkit.player.SubtitleStyleSettings

class SubtitleStyling {
    var subtitleStyleName: String? = null
    var config: Config? = null
}

class Config {
    var subtitleTextColor: String? = null
    var subtitleBackgroundColor: String? = null
    var subtitleWindowColor: String? = null
    var subtitleEdgeColor: String? = null
    var subtitleTextSizeFraction: String? = null
    var subtitleStyleTypeface: String? = null
    var subtitleEdgeType: String? = null
    var overrideInlineCueConfig: Boolean = false
    var verticalPositionPercentage: Int? = null
    var horizontalPositionPercentage: Int? = null
    var horizontalAlignment: String? = null

    fun getStringToColor(color: String?): Int {
        return if (color != null) {
            Color.parseColor(color)
        } else {
            Color.RED
        }
    }

    fun getSubtitleTextSizeFraction(): SubtitleStyleSettings.SubtitleTextSizeFraction {
        subtitleTextSizeFraction?.let { fraction ->
            if (SubtitleStyleSettings.SubtitleTextSizeFraction.values().map { it.name }.contains(fraction)) {
                return SubtitleStyleSettings.SubtitleTextSizeFraction.valueOf(fraction)
            }
        }
        return SubtitleStyleSettings.SubtitleTextSizeFraction.SUBTITLE_FRACTION_100
    }

    fun getSubtitleStyleTypeface(): SubtitleStyleSettings.SubtitleStyleTypeface {
        subtitleStyleTypeface?.let { typeface ->
            if (SubtitleStyleSettings.SubtitleStyleTypeface.values().map { it.name }.contains(typeface)) {
                return SubtitleStyleSettings.SubtitleStyleTypeface.valueOf(typeface)
            }
        }
        return SubtitleStyleSettings.SubtitleStyleTypeface.DEFAULT
    }

    fun getSubtitleEdgeType(): SubtitleStyleSettings.SubtitleStyleEdgeType {
        subtitleEdgeType?.let { edgeType ->
            if (SubtitleStyleSettings.SubtitleStyleTypeface.values().map { it.name }.contains(edgeType)) {
                return SubtitleStyleSettings.SubtitleStyleEdgeType.valueOf(edgeType)
            }
        }
        return SubtitleStyleSettings.SubtitleStyleEdgeType.EDGE_TYPE_NONE
    }

    fun getHorizontalAlignment(): Layout.Alignment {
        horizontalAlignment?.let { alignment ->
            if (Layout.Alignment.values().map { it.name }.contains(alignment)) {
                return Layout.Alignment.valueOf(alignment)
            }
        }
        return Layout.Alignment.ALIGN_CENTER
    }
}
