package com.kaltura.kalturaplayertestapp

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.kaltura.kalturaplayertestapp.adapter.TestCaseConfigurationAdapter
import com.kaltura.kalturaplayertestapp.models.Configuration

class JsonDetailActivity: BaseActivity(), TestCaseConfigurationAdapter.OnJsonSelectedListener {

    private var mFirestore: FirebaseFirestore? = null
    private var mConfigurationsRecycler: RecyclerView? = null
    private var currenConfigurationRef: CollectionReference? = null
    private var mQuery: Query? = null
    private var mEmptyView: TextView? = null
    private var mAdapter: TestCaseConfigurationAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_json_detail)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)


        val configuatioPath = intent.extras!!.getString(MainActivity.KEY_NEW_CONFIGURATION_PATH)
                ?: throw IllegalArgumentException("Must pass extra " + MainActivity.KEY_NEW_CONFIGURATION_PATH)
        mConfigurationsRecycler = findViewById(R.id.details_recycler_configuration)

        val itemTouchHelperCallback = object: ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val configuration = mAdapter!!.snapshots[viewHolder.adapterPosition].toObject(Configuration::class.java)

                val snackbar = Snackbar.make(findViewById(android.R.id.content), configuration!!.title!! + " will be removed!", Snackbar.LENGTH_LONG)
                snackbar.setAction("Approve") {
                    //remove test using swipe
                    //mAdapter.removeItem(viewHolder.getAdapterPosition());
                }
                snackbar.setActionTextColor(Color.YELLOW)
                snackbar.show()
            }

            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {}
        }

        // attaching the touch helper to recycler view
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mConfigurationsRecycler)

        mFirestore = FirebaseFirestore.getInstance()
        currenConfigurationRef = mFirestore!!.collection(configuatioPath)
        mQuery = currenConfigurationRef!!.limit(MainActivity.LIMIT.toLong())
        mEmptyView = findViewById(R.id.view_empty)
        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true)

        // RecyclerView
        mAdapter = object: TestCaseConfigurationAdapter(mQuery!!, this@JsonDetailActivity) {
            override fun onDataChanged() {
                // Show/hide content if the query returns empty.
                if (itemCount == 0) {
                    mConfigurationsRecycler!!.visibility = View.GONE
                    mEmptyView!!.visibility = View.VISIBLE
                } else {
                    mConfigurationsRecycler!!.visibility = View.VISIBLE
                    mEmptyView!!.visibility = View.GONE
                }
            }

            override fun onError(e: FirebaseFirestoreException) {
                // Show a snackbar on errors
                Snackbar.make(findViewById(android.R.id.content), "Error: check logs for info.", Snackbar.LENGTH_LONG).show()
            }
        }
        mConfigurationsRecycler!!.layoutManager = LinearLayoutManager(this)
        mConfigurationsRecycler!!.setHasFixedSize(true)
        mConfigurationsRecycler!!.adapter = mAdapter
        mAdapter!!.startListening()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    public override fun onStart() {
        super.onStart()
    }

    public override fun onStop() {
        super.onStop()

        // Remove post value event listener
        //        if (mPostListener != null) {
        //            mPostReference.removeEventListener(mPostListener);
        //        }

    }

    override fun onJsonSelected(configuration: Configuration?) {
        //Snackbar.make(findViewById(android.R.id.content), "Item Selected " + configuration.getId(), Snackbar.LENGTH_SHORT).show();
        val context = this
        if (configuration?.type == Configuration.FOLDER) {
            //staticCollRef = collRef.document(configuration.getId()).collection("configurations");
            val path = currenConfigurationRef!!.document(configuration.id!!).collection("configurations").path
            val destinationClass = JsonDetailActivity::class.java
            val folder = currenConfigurationRef!!.document(configuration.id!!).collection("configurations")
            val intent = Intent(context, destinationClass)
            intent.putExtra(MainActivity.KEY_NEW_CONFIGURATION_PATH, path)
            startActivity(intent)
            return
        } else if (configuration?.type == Configuration.JSON) {
            val destinationClass = PlayerActivity::class.java
            val intent = Intent(context, destinationClass)
            intent.putExtra(PlayerActivity.PLAYER_CONFIG_TITLE_KEY, configuration.title)
            intent.putExtra(PlayerActivity.PLAYER_CONFIG_JSON_KEY, configuration.json)
            startActivity(intent)
        }

    }
}
