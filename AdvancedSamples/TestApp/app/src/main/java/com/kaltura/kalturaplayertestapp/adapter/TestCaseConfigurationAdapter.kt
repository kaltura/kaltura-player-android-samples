package com.kaltura.kalturaplayertestapp.adapter

import android.app.Dialog
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.kaltura.kalturaplayertestapp.R
import com.kaltura.kalturaplayertestapp.models.Configuration
import com.kaltura.playkit.PKLog
import org.json.JSONException
import org.json.JSONObject

/**
 * RecyclerView adapter for a list of Restaurants.
 */
open class TestCaseConfigurationAdapter(query: Query, var mListener: OnJsonSelectedListener) : FirestoreAdapter<TestCaseConfigurationAdapter.ViewHolder>(query) {
    private var context: Context? = null

    fun removeItem(adapterPosition: Int) {
        snapshots[adapterPosition].reference.delete().addOnSuccessListener { log.d("Document Snapshot successfully deleted!") }
    }

    interface OnJsonSelectedListener {

        fun onJsonSelected(configuration: Configuration?)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(context!!, inflater.inflate(R.layout.item_json, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getSnapshot(position), mListener)
    }

    inner class ViewHolder(private val context: Context, itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val jsonTitle: TextView
        private val folderIcon: ImageView
        private val infoIcon: ImageView

        init {
            this.jsonTitle = itemView.findViewById(R.id.json_title)
            this.folderIcon = itemView.findViewById(R.id.right_symbol)
            this.infoIcon = itemView.findViewById(R.id.info_symbol)
            infoIcon.setOnClickListener { }
        }

        fun bind(snapshot: DocumentSnapshot,
                 listener: OnJsonSelectedListener?) {

            val configuration = snapshot.toObject(Configuration::class.java)
            configuration!!.id = snapshot.id
            //Resources resources = itemView.getResources();
            log.d("Test Title = " + configuration.title!!)
            jsonTitle.text = configuration.title
            if (configuration.type == Configuration.FOLDER) {
                folderIcon.visibility = View.VISIBLE
                infoIcon.visibility = View.GONE
            } else {
                folderIcon.visibility = View.GONE
                infoIcon.visibility = View.VISIBLE
                infoIcon.setOnClickListener { openJsonDialog(configuration.title, configuration.json) }
            }


            // Click listener
            itemView.setOnClickListener {
                listener?.onJsonSelected(configuration)
            }
        }

        private fun openJsonDialog(title: String?, json: String?) {
            val dialog = Dialog(context, R.style.FilterDialogTheme)
            dialog.setContentView(R.layout.dialog_layout)
            dialog.setTitle(title)
            val textViewUser = dialog.findViewById<TextView>(R.id.txt)

            try {
                val spacesToIndentEachLevel = 2
                val jsonObject: JSONObject
                if (!TextUtils.isEmpty(json)) {
                    jsonObject = JSONObject(json)
                    textViewUser.text = jsonObject.toString(spacesToIndentEachLevel)
                } else {
                    textViewUser.text = "Json is not valid!"
                }

                val dialogButton = dialog.findViewById<Button>(R.id.dialogButton)
                dialogButton.setOnClickListener { dialog.dismiss() }
                dialog.show()
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }
    }

    companion object {
        private val log = PKLog.get("TestCaseConfigurationAdapter")
    }
}
