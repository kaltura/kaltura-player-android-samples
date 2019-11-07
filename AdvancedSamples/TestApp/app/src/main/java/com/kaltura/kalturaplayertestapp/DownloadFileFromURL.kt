package com.kaltura.kalturaplayertestapp

import android.os.AsyncTask
import java.io.IOException
import java.net.URL

internal class DownloadFileFromURL : AsyncTask<String, String, String>() {

    override fun onPreExecute() {
        super.onPreExecute()
        //showDialog(progress_bar_type);
    }

    override fun doInBackground(vararg url: String): String? {
        try {
            val fileUrl = URL(url[0])
            return Utils.getResponseFromHttpUrl(fileUrl)
        } catch (ex: IOException) {
            return ""
        }

    }

    override fun onProgressUpdate(vararg progress: String) {
        // setting progress percentage
        //pDialog.setProgress(Integer.parseInt(progress[0]));
    }

    override fun onPostExecute(fileContent: String) {

        //Log.d("XXX" , fileContent);
        //dismissDialog(progress_bar_type);
    }
}