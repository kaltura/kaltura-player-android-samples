import com.kaltura.kalturaplayertestapp.converters.AppMediaOptions


class AppOVPMediaOptions : AppMediaOptions() {
    var entryId: String? = null
    var referenceId: String? = null
    val redirectFromEntryId: Boolean = true
    var useApiCaptions = false
}