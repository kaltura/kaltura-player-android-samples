package com.kaltura.kalturaplayertestapp

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.google.gson.Gson
import com.kaltura.kalturaplayertestapp.adapter.TestCaseConfigurationAdapter
import com.kaltura.kalturaplayertestapp.converters.TestDescriptor
import com.kaltura.kalturaplayertestapp.models.Configuration
import com.kaltura.kalturaplayertestapp.qrcode.BarcodeCaptureActivity
import com.kaltura.playkit.PKDeviceCapabilities
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import java.util.concurrent.ExecutionException

open class MainActivity: BaseActivity(), TestCaseConfigurationAdapter.OnJsonSelectedListener {

    private val TAG = "MainActivity"
    private val RC_BARCODE_CAPTURE = 9001
    private val ADD_DEFAULT_ITEMS = "ADD_DEFAULT_ITEMS"
    private val DEFAULT_TESTS_DESCRIPTOR = "http://externaltests.dev.kaltura.com/player/library_SDK_Kaltura_Player/KalturaPlayerApp/default_bulk_import.json"
    //private static String DEFAULT_TESTS_DESCRIPTOR = "http://externaltests.dev.kaltura.com/player/SdkKalturaPlayer/playkitApp/kalturaPlayerApp.json"; //"http://externaltests.dev.kaltura.com/player/sandBox/Kaltura/media_items.json";//"http://externaltests.dev.kaltura.com/player/library_SDK_Kaltura_Player/KalturaPlayerApp/default_bulk_import.json";

    companion object {
        @JvmField
        val KEY_NEW_CONFIGURATION_PATH: String = "key_new_configuration_path"
        @JvmField
        val LIMIT = 100
    }


    private var defaultItemsLoaded: Boolean = false
    private var mConfigurationsRecycler: RecyclerView? = null
    private var mEmptyView: TextView? = null

    private var mFirestore: FirebaseFirestore? = null
    private var collRef: CollectionReference? = null
    private var mQuery: Query? = null
    private var mAuth: FirebaseAuth? = null
    private var currentUser: FirebaseUser? = null
    private var mAdapter: TestCaseConfigurationAdapter? = null
    private var newPath: String? = null

    override fun onStart() {
        super.onStart()

        mFirestore?.collection("users")?.document(currentUser!!.uid)?.addSnapshotListener(EventListener { documentSnapshot, e ->
            if (documentSnapshot == null) {
                Log.e(TAG, "ZZZ documentSnapshot = null")
                return@EventListener
            }
            val name = documentSnapshot.getString("name")
            Log.d(TAG, "ZZZ " + name!!)

            val ref = documentSnapshot.reference.collection("configurations")
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        defaultItemsLoaded = getPreferences(Context.MODE_PRIVATE).getBoolean(ADD_DEFAULT_ITEMS, false)
        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth?.currentUser
        mConfigurationsRecycler = findViewById(R.id.recycler_configuration)
        mEmptyView = findViewById(R.id.view_empty)

        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true)

        // Firestore
        mFirestore = FirebaseFirestore.getInstance()

        //if (staticCollRef == null) {
        collRef = mFirestore?.collection("users")?.document(currentUser!!.uid)?.collection("configurations")
        //} else {
        //    collRef = staticCollRef;
        //}

        val docs = mFirestore?.collection("users")?.document(currentUser!!.uid)
        mQuery = collRef!!
                // .orderBy("avgRating", Query.Direction.DESCENDING)
                .limit(LIMIT.toLong())

        // RecyclerView
        mAdapter = object: TestCaseConfigurationAdapter(mQuery!!, this@MainActivity) {
            override fun onDataChanged() {
                // Show/hide content if the query returns empty.
                if (itemCount == 0) {
                    mConfigurationsRecycler?.visibility = View.GONE
                    mEmptyView?.visibility = View.VISIBLE
                } else {
                    mConfigurationsRecycler?.visibility = View.VISIBLE
                    mEmptyView?.visibility = View.GONE
                }
            }

            override fun onError(e: FirebaseFirestoreException) {
                // Show a snackbar on errors
                Snackbar.make(findViewById(android.R.id.content),
                        "Error: check logs for info.", Snackbar.LENGTH_LONG).show()
            }
        }

        mConfigurationsRecycler?.layoutManager = LinearLayoutManager(this)
        mConfigurationsRecycler?.setHasFixedSize(true)
        mConfigurationsRecycler?.adapter = mAdapter
        mAdapter?.startListening()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_items -> if (!defaultItemsLoaded) {
                loadTestsFromUrl(DEFAULT_TESTS_DESCRIPTOR)
                val sPEditor = getPreferences(Context.MODE_PRIVATE).edit()
                sPEditor.putBoolean(ADD_DEFAULT_ITEMS, true)
                sPEditor.apply()
                defaultItemsLoaded = true
            }
            R.id.add_items_scan -> onScanItemsClicked()
            R.id.action_delete -> {
                val sPEditor = getPreferences(Context.MODE_PRIVATE).edit()
                sPEditor.remove(ADD_DEFAULT_ITEMS)
                sPEditor.apply()
                defaultItemsLoaded = false
            }
            R.id.action_add_folder -> {
            }
            R.id.action_add_json -> {
            }
            R.id.action_remove_folder -> {
            }
            R.id.action_remove_json -> {
            }
            R.id.about_device -> {
                val report = PKDeviceCapabilities.getReport(this@MainActivity)
                openJsonDialog(getString(R.string.about_device), report)
            }
            R.id.about -> showCustomAboutDialog()
            R.id.action_logout -> {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, SignInActivity::class.java))
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openJsonDialog(title: String, json: String) {
        val dialog = Dialog(this@MainActivity, R.style.FilterDialogTheme)
        dialog.setContentView(R.layout.dialog_layout)
        dialog.setTitle(title)
        val textViewUser = dialog.findViewById<TextView>(R.id.txt)

        try {
            val jsonObject = JSONObject(json)

            val spacesToIndentEachLevel = 2
            textViewUser.text = jsonObject.toString(spacesToIndentEachLevel)
            val dialogButton = dialog.findViewById<Button>(R.id.dialogButton)
            dialogButton.setOnClickListener { dialog.dismiss() }
            dialog.show()
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    private fun showAboutDialog() {
        val alertDialog = AlertDialog.Builder(this@MainActivity)
        alertDialog.setTitle("About Test App")
        alertDialog.setIcon(R.drawable.k_image)

        var currentUser: String? = "No User LoggedIn"
        if (FirebaseAuth.getInstance().currentUser != null) {
            currentUser = FirebaseAuth.getInstance().currentUser!!.email
        }
        alertDialog.setMessage("Logged In: " + currentUser + "\n\n" +
                "App Version:" + BuildConfig.VERSION_NAME)


        alertDialog.setPositiveButton("OK") { dialog, which -> dialog.cancel() }
        alertDialog.show()
    }

    fun showCustomAboutDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.custom_about_dialog, null)
        dialogBuilder.setView(dialogView)
        dialogBuilder.setIcon(R.drawable.k_image)

        val loogedInUserView = dialogView.findViewById<TextView>(R.id.mail_view)
        loogedInUserView.text = FirebaseAuth.getInstance().currentUser!!.email

        val appVerNo = dialogView.findViewById<TextView>(R.id.version_no_view)
        appVerNo.text = BuildConfig.VERSION_NAME

        dialogBuilder.setTitle("About")
        dialogBuilder.setMessage("About App:")
        dialogBuilder.setPositiveButton(R.string.ok) { dialog, whichButton -> }
        //        dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
        //            public void onClick(DialogInterface dialog, int whichButton) {
        //                //pass
        //            }
        //        });
        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun onScanItemsClicked() {
        val intent = Intent(this, BarcodeCaptureActivity::class.java)
        //intent.putExtra(BarcodeCaptureActivity.AutoFocus, autoFocus.isChecked());
        //intent.putExtra(BarcodeCaptureActivity.UseFlash, useFlash.isChecked());

        startActivityForResult(intent, RC_BARCODE_CAPTURE)
    }

    private fun onAddItemsClicked() {
        // Add a bunch of random restaurants
        val collectionReference = mFirestore?.collection("users")?.document(currentUser!!.uid)?.collection("configurations")
        val folder = Configuration()
        folder.type = 1
        folder.title = "folder1"
        collectionReference?.add(folder)?.addOnCompleteListener { task ->
            val batch = mFirestore?.batch()
            for (i in 0..9) {
                val restRef = mFirestore?.collection("users")?.document(currentUser!!.uid)?.collection("configurations")?.document()
                val randomConfig = ConfigurationUtil.getRandom(applicationContext)
                task.result!!.collection("configurations").add(randomConfig)
            }
        }
    }


    override fun onJsonSelected(configuration: Configuration?) {
        Snackbar.make(findViewById(android.R.id.content),
                "Item Selected " + configuration?.id, Snackbar.LENGTH_SHORT).show()
        val context = this
        if (configuration?.type == Configuration.FOLDER) {
            //staticCollRef = collRef.document(configuration.getId()).collection("configurations");
            val path = collRef?.document(configuration.id!!)?.collection("configurations")?.path
            val destinationClass = JsonDetailActivity::class.java
            val folder = collRef?.document(configuration.id!!)?.collection("configurations")
            val intent = Intent(context, destinationClass)
            intent.putExtra(KEY_NEW_CONFIGURATION_PATH, path)
            startActivity(intent)
            return
        } else if (configuration?.type == Configuration.JSON) {
            val destinationClass = PlayerActivity::class.java
            val intent = Intent(context, destinationClass)
            intent.putExtra(PlayerActivity.PLAYER_CONFIG_TITLE_KEY, configuration.title)
            intent.putExtra(PlayerActivity.PLAYER_CONFIG_JSON_KEY, configuration.title)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    val barcode = data.getParcelableExtra<Barcode>(BarcodeCaptureActivity.BarcodeObject)
                    Log.d(TAG, "Barcode read: " + barcode.displayValue)
                    loadTestsFromUrl(barcode.displayValue)
                } else {
                    Log.d(TAG, "No barcode captured, intent data is null")
                }
            } else {
                Log.e(TAG, "Error, " + CommonStatusCodes.getStatusCodeString(resultCode))
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun loadTestsFromUrl(testUrl: String) {
        showProgressDialog()
        var jsonTests = ""
        try {
            jsonTests = DownloadFileFromURL().execute(testUrl).get()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }

        if (testUrl.contains("/Tests/")) { // single test
            val testDescriptorArrayList = ArrayList<TestDescriptor>()
            val testDescriptor = TestDescriptor()
            val fileName = testUrl.substring(testUrl.lastIndexOf('/') + 1, testUrl.length)
            if (TextUtils.isEmpty(fileName) || !fileName.endsWith(".json")) {
                Snackbar.make(mEmptyView!!, "Invalid json", Snackbar.LENGTH_SHORT).show()
                hideProgressDialog()
                return
            }
            val fileNameWithoutExtn = fileName.substring(0, fileName.lastIndexOf('.'))
            testDescriptor.title = fileNameWithoutExtn
            testDescriptor.url = testUrl
            testDescriptorArrayList.add(testDescriptor)
            loadTests(testDescriptorArrayList)
        } else { // multiple tests
            val gson = Gson()
            val testDescriptors = gson.fromJson(jsonTests, Array<TestDescriptor>::class.java)
            val testDescriptorArrayList = ArrayList(Arrays.asList(*testDescriptors))
            loadTests(testDescriptorArrayList)
        }
    }

    private fun loadTests(testDescriptorArray: MutableList<TestDescriptor>) {
        if (testDescriptorArray.size == 0) {
            hideProgressDialog()
            return
        }
        val testDescriptor = testDescriptorArray[0]
        testDescriptorArray.removeAt(0)
        val splittedPath = testDescriptor.url!!.split("Tests/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val testPath = ArrayList<String>()
        if (splittedPath.size > 1) {
            val testPathParts = splittedPath[1].split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (testPathParts.size > 0)
                for (i in 0 until testPathParts.size - 1) {
                    testPath.add(testPathParts[i])
                }
        }
        try {
            val jsonTest = DownloadFileFromURL().execute(testDescriptor.url).get()
            val testConfig = Configuration()
            testConfig.type = 0
            testConfig.title = testDescriptor.title
            testConfig.json = jsonTest
            val collectionReference = mFirestore?.collection("users")?.document(currentUser!!.getUid())?.collection("configurations")
            buildFoldersHirarchy(collectionReference!!, testPath, testConfig, testDescriptorArray)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }

    }

    private fun buildFoldersHirarchy(collectionReference: CollectionReference, subFolder: MutableList<String>, testConfig: Configuration, testDescriptorArray: MutableList<TestDescriptor>) {
        var folder = Configuration()
        if (subFolder.size == 0) {
            folder = testConfig
            collectionReference.add(folder)
            return
        } else {
            folder.type = 1
            val folderName = subFolder[0]
            subFolder.removeAt(0)
            folder.title = folderName
        }

        val finalFolder = folder
        val folders = collectionReference.whereEqualTo("title", folder.title)

        folders.get().addOnCompleteListener { task ->
            if (task.isSuccessful && task.result!!.size() > 0) {
                for (document in task.result!!) {
                    if (subFolder.size == 0) {
                        document.reference.collection("configurations").add(testConfig)
                        loadTests(testDescriptorArray)
                    } else {
                        //newPath =
                        document.reference.set(finalFolder).addOnSuccessListener {
                            Log.d(TAG, "DocumentSnapshot successfully written!")
                            newPath = document.reference.collection("configurations").path
                            buildFoldersHirarchy(mFirestore!!.collection(newPath!!), subFolder, testConfig, testDescriptorArray)
                        }
                    }
                    break
                }
            } else {
                Log.d(TAG, "Error getting documents: ", task.exception)
                collectionReference.add(finalFolder).addOnCompleteListener { task ->
                    if (subFolder.size == 0) {
                        task.result!!.collection("configurations").add(testConfig)
                        loadTests(testDescriptorArray)
                    } else {
                        newPath = task.result!!.collection("configurations").path
                        buildFoldersHirarchy(mFirestore!!.collection(newPath!!), subFolder, testConfig, testDescriptorArray)
                    }
                }
            }
        }

    }


}