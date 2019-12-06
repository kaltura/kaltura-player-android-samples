package com.kaltura.playkitdemo

import com.kaltura.playkit.PKMediaFormat

import java.util.ArrayList

/**
 * Created by tehilarozin on 15/11/2016.
 */

object MockParams {

    /* Basic Mock Params: */

    val BASIC_MEDIA_FORMAT = PKMediaFormat.hls
    val BASIC_SOURCE_URL = "https://cdnapisec.kaltura.com/p/2215841/sp/221584100/playManifest/entryId/1_w9zx2eti/protocol/https/format/applehttp/falvorIds/1_1obpcggb,1_yyuvftfz,1_1xdbzoa6,1_k16ccgto,1_djdf6bk8/a.m3u8"
    val BASIC_LICENSE_URL: String? = null

    /*Ott Mock params: */
    var PhoenixBaseUrl = "http://api-preprod.ott.kaltura.com/v4_4/api_v3/"
    var OttPartnerId = 198

    //result of login with : [username: albert@gmail.com, pass: 123456]
    var PnxKS = ""//"djJ8MTk4fH6bz_2197wFUNBqv2zRZ3h5YlTwiAEJZvVhJJut1pq13CMVSpgmD-NLVco4pJuthWU9b8Z_XEV7h6uvg5tpJbCj4ODWAzWhslokulcfUrgG0WISPD3wq1YWwD1lzuJ109OFrezv9Ih5Wa8qrUIsaz8=";

    val SingMediaId = "480989"
    val MediaId = "258656"//frozen
    val MediaId2 = "437800"//vild-wV
    val MediaId3 = "259295"//the salt of earth
    val SingMediaId4 = "258459"

    val MediaType = "media"

    val Format = "Mobile_Devices_Main_HD"
    val Format_HD_Dash = "Mobile_Devices_Main_HD_Dash"
    val Format_SD_Dash = "Mobile_Devices_Main_SD_Dash"
    val Format2 = "Mobile_Devices_Main_SD"
    var FrozenAssetInfo = "mock/phoenix.asset.get.258656.json"
    //---------------------------------------

    /*Ovp Mock params: */
    val OvpPartnerId = 2222401
    val OvpBaseUrl = "https://cdnapisec.kaltura.com/"
    val NonDRMEntryId = "1_xay0wjby" //works for user/anonymous
    val DRMEntryIdUsr = "1_tmomdals" //works for logged user
    val DRMEntryIdAnm = "1_ytsd86sc" //works for anonymous

    val OvpUserKS = "djJ8MjIyMjQwMXx2RAtiYX9vr3hnwdyi1rM78jFD15pr8XYnbhu9iuUy3KXt_NEQK0JV9bdRdaBpohgY5mQW88kKvKu5EC15wfDceyj_37BTG3UYd5LvVa7GbmWxr9YrEpFTxrAPpgeMvYBV-mooSL7YbDqp_kvDqKW3"

    enum class UserType {
        Ott, Ovp
    }

    object UserFactory {

        internal var ottUsers: ArrayList<UserLogin>? = null
        internal var ovpUsers: ArrayList<UserLogin>? = null

        init {
            fillWithUsers()
        }

        fun getUser(type: UserType): UserLogin {
            val users = if (type == UserType.Ott) ottUsers else ovpUsers
            val Min = 0
            val Max = users?.size!! - 1

            val index = Min + (Math.random() * (Max - Min + 1)).toInt()
            return users[index]
        }

        internal fun fillWithUsers() {
            ottUsers = ArrayList()
            ottUsers?.add(UserLogin("albert@gmail.com", "123456", 198))
            ottUsers?.add(UserLogin("betsy@gmail.com", "123456", 198))
            ottUsers?.add(UserLogin("Alfred@gmail.com", "123456", 198))
            ottUsers?.add(UserLogin("itan@b.com", "123456", 198))

            ovpUsers = ArrayList()
            ovpUsers?.add(UserLogin("kaltura.fe@icloud.com", "abcd1234*", 2222401))
        }

        fun getDrmUser(type: UserType): UserLogin? {
            when (type) {
                MockParams.UserType.Ovp -> return ovpUsers!![0]

                else -> return null
            }
        }

        class UserLogin(var username: String, var password: String, var partnerId: Int)
    }
}
